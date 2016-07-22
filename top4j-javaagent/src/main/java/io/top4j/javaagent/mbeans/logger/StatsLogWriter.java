package io.top4j.javaagent.mbeans.logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Logger;

/**
 * Created by ryan on 28/01/16.
 */
public class StatsLogWriter {

    private PrintWriter printWriter;
    private FileOutputStream fileOutputStream;
    private String fileName;

    private static final Logger LOGGER = Logger.getLogger(StatsLogWriter.class.getName());

    public StatsLogWriter( String fileName ) throws Exception {

        // init new PrintWriter
        init( fileName, null );
    }

    public StatsLogWriter( String fileName, String header ) throws Exception {

        // init new PrintWriter
        init( fileName, header );
    }

    private void init( String fileName, String header ) throws Exception {

        PrintWriter printWriter = null;

        File file = new File(fileName);
        try {
            if (!file.exists()) {
                // create new file
                LOGGER.finer("Creating new stats log file " + fileName);
                if (header != null) {
                    printWriter = createNewFile( file, header );
                }
                else {
                    printWriter = createNewFile( file, null );
                }
            }
            else {
                // create new printWriter
                LOGGER.finer("Stats log file " + fileName + " already exists.");
                fileOutputStream = new FileOutputStream(fileName, true);
                printWriter = new PrintWriter( fileOutputStream );
            }

        } catch (IOException ioe) {
            //LOGGER.severe("Unable to create stats log file " + fileName + " due to: " + ioe.getMessage() );
            throw new Exception("Unable to create stats log file " + fileName + " due to: " + ioe.getMessage());
        }

        // store print writer
        this.printWriter = printWriter;

        // store file name
        this.fileName = fileName;

    }

    private PrintWriter createNewFile( File file, String header ) throws Exception {

        PrintWriter printWriter = null;
        try {
            file.createNewFile();
            printWriter = new PrintWriter(new FileOutputStream(file.getAbsolutePath(), true));
            if (header != null) {
                printWriter.println(header);
            }
        } catch (IOException ioe) {
            //LOGGER.severe("Unable to create stats log file " + file.getName() + " due to: " + ioe.getMessage() );
            throw new Exception("Unable to create stats log file " + fileName + " due to: " + ioe.getMessage());
        } finally {
            if (printWriter != null) {
                printWriter.flush();
            }
        }
        return printWriter;
    }

    public void println( String logEntry ) {

        if (printWriter != null) {
            // write logEntry to stats log file via printWriter
            printWriter.println(logEntry);
            // and flush to disk
            printWriter.flush();
        }
    }

    public void close( ) {

        if (printWriter != null) {
            // close the printWriter
            printWriter.close();
        }
        if (fileOutputStream != null) {
            // and the fileOutputStream
            try {
                fileOutputStream.close();
            } catch (IOException ioe) {
                LOGGER.severe("Unable to close file output stream for stats log file " + fileName + " due to: " + ioe.getMessage());
            }
        }
    }

}
