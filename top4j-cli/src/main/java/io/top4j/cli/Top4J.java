package io.top4j.cli;

import io.top4j.javaagent.config.Configurator;
import io.top4j.javaagent.controller.Controller;
import jline.console.ConsoleReader;
import org.cyclopsgroup.jmxterm.JavaProcess;
import org.cyclopsgroup.jmxterm.JavaProcessManager;
import org.cyclopsgroup.jmxterm.jdk6.Jdk6JavaProcessManager;
import org.cyclopsgroup.jmxterm.pm.JConsoleClassLoaderFactory;

import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by ryan on 02/07/16.
 */
public class Top4J {

    public static void main( String[] args ) throws IOException, MalformedObjectNameException, NoSuchMethodException, ClassNotFoundException {

        ConsoleReader consoleReader = new ConsoleReader();

        Display display = new Display();

        if (args.length != 1) {
            System.err.println("USAGE: java -jar top4j-cli.jar <jvm-pid>");
            System.exit(-1);
        }
        //JavaProcessManager javaProcessManager = new Jdk6JavaProcessManager(ClassLoader.getSystemClassLoader());
        ClassLoader classLoader = JConsoleClassLoaderFactory.getClassLoader();
        JavaProcessManager javaProcessManager = new Jdk6JavaProcessManager(classLoader);
        List<JavaProcess> jvms = javaProcessManager.list();
        for (JavaProcess javaProcess : jvms) {
            System.out.println("Display Name = " + javaProcess.getDisplayName());
            System.out.println("PID = " + javaProcess.getProcessId());
            javaProcess.startManagementAgent();
            System.out.println("Is Manageable = " + javaProcess.isManageable());
        }

        JavaProcess jvm = javaProcessManager.get(Integer.parseInt(args[0]));
        jvm.startManagementAgent();

        String connectorAddr = jvm.toUrl();
        System.out.println("Connector URL = " + connectorAddr );

        JMXServiceURL serviceURL = new JMXServiceURL(connectorAddr);
        JMXConnector connector = JMXConnectorFactory.connect(serviceURL);
        MBeanServerConnection mbsc = connector.getMBeanServerConnection();

        // set topThreadCount
        int topThreadCount = 10;

        // define Top4J config overrides
        String configOverrides = "collector.poll.frequency=15000," +
                "dispatcher.poll.frequency=15000," +
                "log.properties.on.startup=true," +
                "stats.logger.enabled=false," +
                "top.thread.count=" + topThreadCount;
        // initialise configurator
		Configurator config = new Configurator( mbsc, configOverrides );

		// create and start controller thread
		Controller controller = new Controller( config );
		controller.start();

		System.out.println("Top4J: Java agent activated.");

        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();

        // create new TimerTask to run ConsoleController
        TimerTask consoleController = new ConsoleController(consoleReader, display, mbsc, topThreadCount);
        // create new Timer to schedule ConsoleController
        Timer timer = new Timer("Top4J Console Controller", true);
        // run Top4J Console Controller at fixed interval
        timer.scheduleAtFixedRate(consoleController, 0, 3000);

        while (true) {
            //String input = consoleReader.readLine();
            Integer input = consoleReader.readCharacter();
            display.setText(input.toString());
        }

    }
}
