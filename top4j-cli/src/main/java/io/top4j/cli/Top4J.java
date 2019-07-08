package io.top4j.cli;

import io.top4j.javaagent.config.Configurator;
import io.top4j.javaagent.controller.Controller;
import jline.console.ConsoleReader;
import org.cyclopsgroup.jmxterm.JavaProcess;
import org.cyclopsgroup.jmxterm.JavaProcessManager;
import org.cyclopsgroup.jmxterm.jdk6.Jdk6JavaProcessManager;
import org.cyclopsgroup.jmxterm.pm.JConsoleClassLoaderFactory;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Scanner;

/**
 * Created by ryan on 02/07/16.
 */
public class Top4J {

    public static void main( String[] args ) throws IOException, NoSuchMethodException, ClassNotFoundException {

        // set console refresh period - how frequently in milliseconds the Top4J console screen is refreshed
        int consoleRefreshPeriod = 3000;

        // set Top4J JavaAgent collector poll frequency - how frequently in milliseconds the performance metrics gathered by the Top4J JavaAgent are updated
        int collectorPollFrequency = consoleRefreshPeriod * 5;

        // instantiate new consoleReader
        ConsoleReader consoleReader = new ConsoleReader();

        // instantiate new userInput used to share user entered input between the consoleReader and the consoleController
        UserInput userInput = new UserInput();

        // instantiate javaProcessManager
        ClassLoader classLoader = JConsoleClassLoaderFactory.getClassLoader();
        JavaProcessManager javaProcessManager = new Jdk6JavaProcessManager(classLoader);

        // initialise jvmPid variable used to store JVM process ID
        int jvmPid = 0;

        // check args
        if (args.length == 1 && !isNumeric(args[0])) {
            // user has provided a command-line arg but it's *not* a number - return usage message and exit with error code
            System.err.println("USAGE: java -jar top4j-cli.jar <jvm-pid>");
            System.exit(-1);
        }
        else if (args.length == 1 && isNumeric(args[0])){
            // user has provided a command-line arg and it's a valid number - use the arg as the jvmPid
            jvmPid = Integer.parseInt(args[0]);
        }
        else if (args.length == 0) {
            // user has provided no command-line args - attempt to detect running JVMs and provide list of PIDs to select from
            // generate list of running Java processes
            List<JavaProcess> jvms = javaProcessManager.list();
            int jvmCount = jvms.size();
            // initialise jvmCounter
            int jvmCounter = 0;
            // list available JVMs
            System.out.println();
            for (JavaProcess javaProcess : jvms) {
                // print JVM details
                System.out.println(jvmCounter + ") " + javaProcess.getDisplayName() + " [PID=" + javaProcess.getProcessId() + "]");
                // increment jvmCounter
                jvmCounter++;
            }
            System.out.println();
            // prompt user to enter a number
            if (jvmCount >= 10) {
                System.out.print("Please type a JVM number between 0 and " + (jvmCount - 1) + " and hit enter: ");
            }
            else {
                System.out.print("Please select a JVM number between 0 and " + (jvmCount - 1) + ": ");
            }
            // initialise jvmNumber used to store user input as an Integer
            int jvmNumber;
            // try reading jvmNumber from stdin
            try {
                if (jvmCount >= 10) {
                    // use Java Text Scanner to read multi-digit text string
                    Scanner in = new Scanner(System.in);
                    jvmNumber = in.nextInt();
                }
                else {
                    // use System Console to read single digit character
                    String input = System.console().readLine();
                    jvmNumber = Integer.parseInt( input );
                }
                System.out.println(jvmNumber);
                // validate user input
                if (!(jvmNumber >= 0 && jvmNumber <= jvmCount-1)) {
                    // user has entered an out-of-bounds jvmNumber - return error message and exit with error code
                    System.err.println("ERROR: Please enter a JVM number between 0 and " + (jvmCount-1));
                    System.exit(-1);
                }
                // set jvmPid according to user selection
                jvmPid = jvms.get(jvmNumber).getProcessId();
            }
            catch (Exception e) {
                // user has entered an invalid jvmNumber - return error message and exit with error code
                System.err.println("ERROR: Please enter a JVM number between 0 and " + (jvmCount-1));
                System.exit(-1);
            }
        }
        else {
            // user has provided some unexpected command-line args - return usage message and exit with error code
            System.err.println("USAGE: java -jar top4j-cli.jar <jvm-pid>");
            System.exit(-1);
        }

        // use javaProcessManager to attach to jvmPid
        System.out.println();
        System.out.println("Attempting to attach to JVM PID " + jvmPid + "....");
        JavaProcess jvm = javaProcessManager.get(jvmPid);
        // check jvm exists
        if (jvm == null) {
            System.err.println("ERROR: JVM not found with PID " + jvmPid);
            System.err.println("HINT: If the JVM is running, check that top4j is running as the same user as the JVM process owner");
            System.exit(-1);
        }
        // start JMX management agent within target jvm
        jvm.startManagementAgent();

        // print JMX connector URL
        String connectorAddr = jvm.toUrl();
        System.out.println("....using Connector URL = " + connectorAddr );

        // use JMX connector URL to connect to JMX service and establish MBean server connection
        JMXServiceURL serviceURL = new JMXServiceURL(connectorAddr);
        JMXConnector connector = JMXConnectorFactory.connect(serviceURL);
        MBeanServerConnection mbsc = connector.getMBeanServerConnection();
        System.out.println("Successfully connected to target JVM JMX MBean Server.");

        // set displayThreadCount
        int displayThreadCount = 10;

        // define Top4J config overrides
        String configOverrides = "collector.poll.frequency=" + collectorPollFrequency + "," +
                "log.properties.on.startup=true," +
                "stats.logger.enabled=false," +
                "hot.method.profiling.enabled=false," +
                "top.thread.count=" + displayThreadCount + "," +
                "blocked.thread.count=" + displayThreadCount;
        // initialise Top4J configurator
		Configurator config = new Configurator( mbsc, configOverrides );

		// create and start Top4J controller thread
		Controller controller = new Controller( config );
		controller.start();
		System.out.println("Top4J: Java agent activated.");

        // create new TimerTask to run Top4J CLI ConsoleController thread
        TimerTask consoleController = new ConsoleController(consoleReader, userInput, mbsc, displayThreadCount);
        // create new Timer to schedule ConsoleController thread
        Timer timer = new Timer("Top4J Console Controller", true);
        // run Top4J Console Controller at fixed interval
        timer.scheduleAtFixedRate(consoleController, 0, consoleRefreshPeriod);

        while (true) {
            //String input = consoleReader.readLine();
            Integer input = consoleReader.readCharacter();
            Character inputChar = (char) Integer.valueOf(input).intValue();
            String inputText = inputChar.toString();
            if (inputText.equals("q")) {
                try {
                    consoleReader.println("Exiting....");
                    consoleReader.println();
                    consoleReader.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                    System.exit(1);
                }
                // exit Top4J
                System.exit(0);
            }
            if (Character.isDigit( inputChar )) {
                userInput.setIsDigit(true);
            }
            else {
                userInput.setIsDigit(false);
            }
            userInput.setText(inputText);
        }

    }

    /*
        Verify string is a number, i.e. can be parsed into an Integer
     */
    private static boolean isNumeric(String stringNum) {
        try {
            int number = Integer.parseInt(stringNum);
        } catch (NumberFormatException | NullPointerException nfe) {
            return false;
        }
        return true;
    }
}
