/*
 * Copyright (c) 2019 Open Answers Ltd. https://www.openanswers.co.uk
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.top4j.cli;

import io.top4j.javaagent.config.Configurator;
import io.top4j.javaagent.controller.Controller;
import io.top4j.vm.Jdk9JavaProcessManager;
import jline.console.ConsoleReader;
import org.apache.commons.cli.*;
import org.apache.commons.lang3.JavaVersion;
import org.apache.commons.lang3.SystemUtils;
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
import java.util.Scanner;
import java.util.Timer;
import java.util.logging.Logger;

public class Top4J {

    // set default console refresh period - how frequently in milliseconds the Top4J console screen is refreshed
    private static int consoleRefreshPeriod = 3000;
    // enable thread usage cache by default
    private static boolean threadCacheEnabled = true;
    // set default thread usage cache size
    private static int threadCacheSize = 500;
    // set default thread usage cache time-to-live
    private static int threadCacheTTL = 15000;
    // set default start up verbosity
    private static boolean verbose = false;

    private static final Logger LOGGER = Logger.getLogger(Top4J.class.getName());

    public static void main(String[] args) throws Exception {

        // run Top4J CLI
        System.exit(new Top4J().execute(args));
    }

    /**
     * Run Top4J CLI
     *
     * @param args Command line arguments
     * @return Exit code
     * @throws Exception Allow any exceptions
     */
    private int execute(String[] args) throws Exception {

        // instantiate new consoleReader
        ConsoleReader consoleReader = new ConsoleReader();

        // instantiate new userInput used to share user entered input between the consoleReader and the consoleController
        UserInput userInput = new UserInput();

        // instantiate javaProcessManager
        JavaProcessManager javaProcessManager;
        if (isJava9Plus()) {
            javaProcessManager = new Jdk9JavaProcessManager();
        } else {
            ClassLoader classLoader = JConsoleClassLoaderFactory.getClassLoader();
            javaProcessManager = new Jdk6JavaProcessManager(classLoader);
        }

        // initialise jvmPid variable used to store JVM process ID
        int jvmPid = 0;
        // initialise jvmDisplayName variable used to store JVM display name
        String jvmDisplayName = "";
        int displayThreadCount = 10;

        // define command-line args
        Options options = defineOptions();

        // parse command-line args
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = null;
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            LOGGER.severe("ERROR: There was a problem parsing command-line arguments. Reason: " + e.getMessage());
            return 1;
        }

        // interrogate command-line args
        if (cmd.hasOption("h")) {
            // automatically generate the help statement
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("top4j", options);
            return 0;
        }
        if (cmd.hasOption("d")) {
            // user has provided a screen refresh delay interval via the command-line
            String userProvidedDelayInterval = cmd.getOptionValue("d", "3");
            if (!isNumeric(userProvidedDelayInterval)) {
                // user provided delay interval is *not* a number - return usage message and exit with error code
                LOGGER.severe("ERROR: Delay interval provided via command-line argument is not a number: " + userProvidedDelayInterval);
                return 1;
            }
            // override default consoleRefreshPeriod
            consoleRefreshPeriod = Integer.parseInt(userProvidedDelayInterval) * 1000;
        }
        if (cmd.hasOption("t")) {
            String userThreadCount = cmd.getOptionValue("t");
            if (!isNumeric(userThreadCount)) {
                // user provided JVM PID is *not* a number - return usage message and exit with error code
                LOGGER.severe("ERROR: -t thread count provided via command-line argument is not a number: " + userThreadCount);
                return 1;
            }
            // user has provided a command-line arg and it's a valid number - use the arg as the jvmPid
            displayThreadCount = Integer.parseInt(userThreadCount);
        }
        if (cmd.hasOption("v")) {
            // enable verbose start up messages
            verbose = true;
        }
        if (cmd.hasOption("D")) {
            // user has requested that the thread usage cache is disabled
            threadCacheEnabled = false;
        }
        if (cmd.hasOption("C")) {
            // user has requested that the thread usage cache is enabled
            threadCacheEnabled = true;
        }
        if (cmd.hasOption("S")) {
            // user has provided a thread usage cache size via the command-line
            String userProvidedThreadCacheSize = cmd.getOptionValue("S");
            if (!isNumeric(userProvidedThreadCacheSize)) {
                // user provided thread usage cache size is *not* a number - return usage message and exit with error code
                LOGGER.severe("ERROR: Thread usage cache size provided via command-line argument is not a number: " + userProvidedThreadCacheSize);
                return 1;
            }
            // override default threadCacheSize
            threadCacheSize = Integer.parseInt(userProvidedThreadCacheSize);
        }
        if (cmd.hasOption("T")) {
            // user has provided a thread usage cache TTL via the command-line
            String userProvidedThreadCacheTTL = cmd.getOptionValue("T");
            if (!isNumeric(userProvidedThreadCacheTTL)) {
                // user provided thread usage cache TTL is *not* a number - return usage message and exit with error code
                LOGGER.severe("ERROR: Thread usage cache TTL provided via command-line argument is not a number: " + userProvidedThreadCacheTTL);
                return 1;
            }
            // override default threadCacheTTL
            threadCacheTTL = Integer.parseInt(userProvidedThreadCacheTTL) * 1000;
        }
        if (cmd.hasOption("p")) {
            // user has provided a JVM PID via the command-line
            String userProvidedJvmPid = cmd.getOptionValue("p");
            if (!isNumeric(userProvidedJvmPid)) {
                // user provided JVM PID is *not* a number - return usage message and exit with error code
                LOGGER.severe("ERROR: JVM PID provided via command-line argument is not a number: " + userProvidedJvmPid);
                return 1;
            }
            // user has provided a command-line arg and it's a valid number - use the arg as the jvmPid
            jvmPid = Integer.parseInt(userProvidedJvmPid);
        } else {
            // user has not provided a JVM PID via command-line args - attempt to detect running JVMs and provide list of PIDs to select from
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
            } else {
                System.out.print("Please select a JVM number between 0 and " + (jvmCount - 1) + ": ");
            }
            // initialise jvmNumber used to store user input as an Integer
            int jvmNumber;
            // try reading jvmNumber from stdin
            try {
                if (jvmCount > 10) {
                    // use Java Text Scanner to read multi-digit text string
                    Scanner in = new Scanner(System.in, "utf-8");
                    jvmNumber = in.nextInt();
                } else {
                    // use System Console to read single digit character
                    String input = System.console().readLine();
                    jvmNumber = Integer.parseInt(input);
                }
                System.out.println(jvmNumber);
                // validate user input
                if (!(jvmNumber >= 0 && jvmNumber <= jvmCount - 1)) {
                    // user has entered an out-of-bounds jvmNumber - return error message and exit with error code
                    LOGGER.severe("ERROR: Please enter a JVM number between 0 and " + (jvmCount - 1));
                    return 1;
                }
                // set jvmPid according to user selection
                jvmPid = jvms.get(jvmNumber).getProcessId();
                // set jvmDisplayName according to user selection
                jvmDisplayName = jvms.get(jvmNumber).getDisplayName();
            } catch (Exception e) {
                // user has entered an invalid jvmNumber - return error message and exit with error code
                LOGGER.severe("ERROR: Please enter a JVM number between 0 and " + (jvmCount - 1));
                return 1;
            }
        }

        // use javaProcessManager to attach to jvmPid
        System.out.println();
        LOGGER.info("Attempting to attach to JVM PID " + jvmPid + "....");
        JavaProcess jvm = javaProcessManager.get(jvmPid);
        // check jvm exists
        if (jvm == null) {
            LOGGER.severe("ERROR: JVM not found with PID " + jvmPid);
            LOGGER.severe("HINT: If the JVM is running, check that top4j is running as the JVM process owner");
            return 1;
        }
        // start JMX management agent within target jvm
        jvm.startManagementAgent();

        // get JMX connector URL
        String connectorAddr = jvm.toUrl();
        // use JMX connector URL to connect to JMX service and establish MBean server connection
        JMXServiceURL serviceURL = new JMXServiceURL(connectorAddr);
        JMXConnector connector = JMXConnectorFactory.connect(serviceURL);
        MBeanServerConnection mbsc = connector.getMBeanServerConnection();
        LOGGER.info("Successfully connected to target JVM JMX MBean Server.");

        // define Top4J config overrides
        String configOverrides = "collector.poll.frequency=" + consoleRefreshPeriod + "," +
                "log.properties.on.startup=" + verbose + "," +
                "stats.logger.enabled=false," +
                "hot.method.profiling.enabled=false," +
                "thread.usage.cache.enabled=" + Boolean.toString(threadCacheEnabled) + "," +
                "thread.usage.cache.size=" + threadCacheSize + "," +
                "thread.usage.cache.ttl=" + threadCacheTTL + "," +
                "top.thread.count=" + displayThreadCount + "," +
                "blocked.thread.count=" + displayThreadCount;
        // initialise Top4J configurator
        Configurator config = new Configurator(mbsc, configOverrides);

        // create and start Top4J controller thread
        LOGGER.info("Top4J: Initialising Java agent.");
        Controller controller = new Controller(config);
        controller.start();
        LOGGER.info("Top4J: Java agent activated.");

        // create new DisplayConfig to pass to ConsoleController
        DisplayConfig displayConfig = new DisplayConfig(displayThreadCount, jvmPid, jvmDisplayName);
        // create new TimerTask to run Top4J CLI ConsoleController thread
        //ConsoleController consoleController = new ConsoleController(consoleReader, userInput, mbsc, displayConfig);
        // create new Timer to schedule ConsoleController thread
        //Timer timer = new Timer("Top4J Console Controller", true);
        // run Top4J Console Controller at fixed interval
        //timer.scheduleAtFixedRate(consoleController, 0, consoleRefreshPeriod);


        boolean extendedMode = displayThreadCount > 10; // handle multi-digit numbers?
        StringBuilder number = new StringBuilder();
        boolean parsingNumber = false;
        Timer timer = null;
        ConsoleController consoleController = new ConsoleController(consoleReader, userInput, mbsc, displayConfig);
        ConsoleControllerTask consoleControllerTask = null;
        while (true) {
            if (!parsingNumber) {
                // display is more responsive if we schedule/re-schedule around user-input; previously
                // after entering key we had to wait for next screen update cycle...
                consoleControllerTask = new ConsoleControllerTask(consoleController);
                timer = new Timer("Top4J Console Controller", true);
                timer.scheduleAtFixedRate(consoleControllerTask, 0, consoleRefreshPeriod);
            }
            Integer input = consoleReader.readCharacter();
            if (!parsingNumber)
                timer.cancel(); // suspend display while we handle user input
            Character inputChar = (char) Integer.valueOf(input).intValue();
            String inputText = inputChar.toString();
            if (inputText.equals("q")) {
                try {
                    consoleReader.println("Exiting....");
                    consoleReader.println();
                    consoleReader.flush();
                } catch (IOException e) {
                    return 1;
                }
                // exit Top4J
                return 0;
            }
            if (Character.isDigit(inputChar)) {
                if (extendedMode) {
                    try {
                        userInput.consoleLock.lock(); // ensure ConsoleController not updating screen
                        consoleController.pause(true);
                        number.append(inputChar);
                        parsingNumber = true;
                        consoleReader.print(String.valueOf(inputChar));
                        consoleReader.flush();
                    } finally {
                        // nb ConsoleController is set to paused so will not update screen when we now unlock
                        userInput.consoleLock.unlock();
                    }
                } else {
                    userInput.setIsDigit(true);
                    userInput.setText(inputText);
                }
                continue;
            } else if (parsingNumber) {
                try {
                    if (inputText.equals("\r") || inputText.equals("\n")) {
                        userInput.setText(number.toString());
                        userInput.setIsDigit(true);
                        continue;
                    }
                    // else extended number handling aborted - will fall through to non-number handling below
                } finally {
                    parsingNumber = false;
                    number = new StringBuilder();
                    consoleController.pause(false);
                }
            }

            // non-number handling (no numbers will get this far)
            if (inputText.equals("m")) {
                userInput.setIsDigit(false);
            } else {
                userInput.setIsDigit(false);
                userInput.setScreenId(inputText);
            }
            userInput.setText(inputText);
        }

    }

    /*
        Verify string is a number, i.e. can be parsed into an Integer
     */
    private static boolean isNumeric(String stringNum) {
        try {
            Integer.parseInt(stringNum);
        } catch (NumberFormatException | NullPointerException nfe) {
            return false;
        }
        return true;
    }

    /*
        Define command-line options
     */
    private static Options defineOptions() {

        // instantiate new command-line options
        Options options = new Options();

        // add d option
        options.addOption("d", "delay", true, "Delay time interval (in seconds)");

        // add h option
        options.addOption("h", "help", false, "Print this message");

        // add p option
        options.addOption("p", "pid", true, "Monitor PID");

        // add t option 
        options.addOption("t", "threads", true, "Number of top threads to display");

        // add v option
        options.addOption("v", "verbose", false, "Print configuration properties on start up");

        // add C option
        options.addOption("C", "cache-enabled", false, "Enable thread usage cache (enabled by default)");

        // add D option
        options.addOption("D", "cache-disabled", false, "Disable thread usage cache (enabled by default)");

        // add S option
        options.addOption("S", "cache-size", true, "Thread usage cache size (" + threadCacheSize + " by default)");

        // add T option
        options.addOption("T", "cache-ttl", true, "Thread usage cache time-to-live (in seconds) - " + threadCacheTTL / 1000 + " secs by default");

        // return command-line options
        return options;

    }

    private static boolean isJava9Plus() {
        return SystemUtils.isJavaVersionAtLeast(JavaVersion.JAVA_9);
    }
}
