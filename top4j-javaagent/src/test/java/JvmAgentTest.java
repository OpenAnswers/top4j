import io.top4j.javaagent.config.Constants;
import io.top4j.javaagent.controller.Agent;
import io.top4j.javaagent.test.MultiThreadedTest;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import javax.management.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.util.*;
import java.util.logging.Logger;

public class JvmAgentTest {

    private class AssertionData {

        public String statsType;
        public String attributeName;
        public String comparator;
        public String attributeType;
        public String assertionDatum;
    }

    private final String NUM_THREADS = "500";
    private final String NUM_ITERATIONS = "15";
    private final String PAUSE_TIME = "5";
    private final String[] multiThreadedTestArgs = { NUM_THREADS, NUM_ITERATIONS, PAUSE_TIME };
    private final String assertionDataFile = "/assertion-data.csv";
    private List<AssertionData> assertionDataList = new ArrayList<>();
    private MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
    private Map<String, ObjectName> mbeanObjectNames = new HashMap<>();

    private static final Logger LOGGER = Logger.getLogger(JvmAgentTest.class.getName());

    public JvmAgentTest ( ) throws Exception {
        // set logger format
        String loggerFormat = "%4$s  %1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS Top4J JvmAgentTest: %5$s%6$s%n";
        System.setProperty("java.util.logging.SimpleFormatter.format", loggerFormat);
        // run JVM Agent
        Agent.premain(null, null);
        // load assertion data
        loadAssertionData();
        // init Map of MBean ObjectNames
        try {
            initMBeanObjectNames();
        } catch (Exception e) {
            throw new Exception( "Failed to initialise MBean object names due to: " + e.getMessage() );
        }
    }

    @Test
    public void multiThreadedTest() throws Exception {
        // run MultiThreadedTest
        MultiThreadedTest.main( multiThreadedTestArgs );
        // run MBean assertions
        try {
            runMBeanAssertions();
        } catch (Exception e) {
            throw new Exception( "Failed to run MBean assertions due to: " + e.getMessage() );
        }
    }

    private void loadAssertionData( ) {

        BufferedReader br = null;
        String line = "";
        String fieldDelimiter = ",";
        try {
            br = new BufferedReader(new InputStreamReader( getClass().getResourceAsStream(assertionDataFile) ) );
            while ((line = br.readLine()) != null) {
                if (line.startsWith("#")) continue;
                String[] data = line.split(fieldDelimiter);
                // create new assertionData object
                AssertionData assertionData = new AssertionData();
                assertionData.statsType = data[0];
                assertionData.attributeName = data[1];
                assertionData.comparator = data[2];
                assertionData.attributeType = data[3];
                assertionData.assertionDatum = data[4];
                // add assertionData object to assertionDataList
                assertionDataList.add(assertionData);
            }
        } catch (IOException e) {
            LOGGER.severe("ERROR: Unable to load assertion data from CSV file: " + assertionDataFile );
        }

    }

    private void initMBeanObjectNames( ) throws Exception {

        ObjectName top4jStatsName = null;
        try {
        	top4jStatsName = new ObjectName(Constants.DOMAIN + ":type=" + Constants.JVM_STATS_TYPE + ",*");
		} catch (MalformedObjectNameException e) {
            throw new Exception( "JMX MalformedObjectNameException: " + e.getMessage() );
		}
		Set<ObjectName> top4jMBeans = mbs.queryNames(top4jStatsName, null);
        for (ObjectName top4jMbean : top4jMBeans) {
            // get this MBean key property list
    		String keyPropertyList = top4jMbean.getKeyPropertyListString();
            // extract statsType from keyPropertyList, e.g. type=JVM,statsType=ThreadStats
    		String statsType = keyPropertyList.split(",")[1].split("=")[1];
            // store MBean ObjectName
            mbeanObjectNames.put(statsType, top4jMbean);
        }
    }

    private void runMBeanAssertions( ) throws Exception {

        for (AssertionData data : assertionDataList) {

            String failureMessage = "Failed to assert " + data.statsType + "." + data.attributeName + " attribute value";
            // get MBean attribute value for this statsType attributeName
            Object mbeanAttribute = null;
            try {
					mbeanAttribute = mbs.getAttribute(mbeanObjectNames.get(data.statsType), data.attributeName);
				} catch (AttributeNotFoundException e) {
                    throw new Exception( "JMX AttributeNotFoundException for MBean " + data.statsType + ": " + e.getMessage() );
				} catch (InstanceNotFoundException e) {
                    throw new Exception( "JMX InstanceNotFoundException for MBean " + data.statsType + ": " + e.getMessage() );
				} catch (MBeanException e) {
                    throw new Exception( "JMX MBeanException for MBean " + data.statsType + ": " + e.getMessage() );
				} catch (ReflectionException e) {
                    throw new Exception( "JMX ReflectionException for MBean " + data.statsType + ": " + e.getMessage() );
				}
            switch (data.attributeType) {

                case "double":
                    assertDouble(failureMessage, Double.valueOf(data.assertionDatum), (Double)mbeanAttribute, data.comparator);
                    break;

                case "long":
                    assertLong(failureMessage, Long.valueOf(data.assertionDatum), (Long)mbeanAttribute, data.comparator);
                    break;

                default:
                    fail("Unexpected attributeType - " + data.attributeType + " - found within assertionDataFile - " + assertionDataFile);
                    break;

            }
        }
    }

    private void assertDouble( String message, Double expectedValue, Double actualValue, String comparator ) {

        String errorMessage;

        switch (comparator) {

            case "eq":
                errorMessage = message + " -> actual value: " + actualValue + " != expected value: " + expectedValue;
                assertEquals(errorMessage, expectedValue, actualValue);
                break;

            case "gt":
                errorMessage = message + " -> actual value: " + actualValue + " < expected value: " + expectedValue;
                assertTrue(errorMessage, actualValue > expectedValue);
                break;

            case "lt":
                errorMessage = message + " -> actual value: " + actualValue + " > expected value: " + expectedValue;
                assertTrue(errorMessage, actualValue < expectedValue);
                break;

            default:
                fail("Unexpected comparator - " + comparator + " - passed to assertDouble() method.");
                break;

        }
    }

    private void assertLong( String message, Long expectedValue, Long actualValue, String comparator ) {

        String errorMessage;

        switch (comparator) {

            case "eq":
                errorMessage = message + " -> actual value: " + actualValue + " != expected value: " + expectedValue;
                assertEquals(errorMessage, expectedValue, actualValue);
                break;

            case "gt":
                errorMessage = message + " -> actual value: " + actualValue + " < expected value: " + expectedValue;
                assertTrue(errorMessage, actualValue > expectedValue);
                break;

            case "lt":
                errorMessage = message + " -> actual value: " + actualValue + " > expected value: " + expectedValue;
                assertTrue(errorMessage, actualValue < expectedValue);
                break;

            default:
                fail("Unexpected comparator - " + comparator + " - passed to assertLong() method.");
                break;

        }
    }
}
