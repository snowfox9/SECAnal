package ui;

import core.IndexProcessor;
import core.log.SimpleLogger;
import runnable.Terminate;
import sun.java2d.pipe.SpanShapeRenderer;
import util.Env;
import util.FTPConn;
import util.MongoConnector;

import java.io.File;
import java.net.SocketException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class IndexProfiler {

    public static void main(String[] args)
    {
        IndexProfiler.TestRunConfig();
        SimpleLogger.InitLogger();
        MongoConnector.initiate();

        FTPConn ftpConn = new FTPConn(Env.hostName, Env.serverPort, Env.userName, Env.userPass);
        ftpConn.isReady();
        // prepare server open

        Calendar c = Calendar.getInstance();
        c.set(2005, Calendar.MAY, 25);

        Calendar upTo = Calendar.getInstance();
        upTo.set(2014, Calendar.JANUARY, 1);

        while(c.getTimeInMillis() < upTo.getTimeInMillis())
        {
            boolean isDone=true;
            try
            {
                IndexProcessor indexProcessor = new IndexProcessor(c.getTime(), ftpConn);
                isDone = indexProcessor.getEdgarIndexAndSaveInDB();

                System.out.println("Processing " + new SimpleDateFormat("yyyy-MM-dd").format(c.getTime()) + " Complete");
            }
            catch (Exception e)
            {
                System.out.println("Processing " + new SimpleDateFormat("yyyy-MM-dd").format(c.getTime()) + " raised Error");
                e.printStackTrace();
            }

            if(isDone)
            {
                c.add(Calendar.DATE, 1);
            }

        }


        // server close
        ftpConn.Close();
        Runtime.getRuntime().addShutdownHook(new Terminate());
    }

    protected static int TestRunConfig()
    {
        // test data folder
        File f = new File("data");
        if(f.exists() && f.isDirectory())
        {
            // fine
        } else {
            f.mkdir();
        }

        // test ENV folder
        f = new File("data/env");
        if(f.exists() && f.isDirectory())
        {
            // fine
        } else {
            f.mkdir(); // no ENV folder
        }

        // test LOG folder
        f = new File("data/log");
        if(f.exists() && f.isDirectory())
        {
            // fine
        } else {
            f.mkdir(); // no LOG folder
        }

        f= new File("data/edgar");
        if(f.exists() && f.isDirectory())
        {
            // fine
        } else {
            f.mkdir(); // no LOG folder
        }

        return 0; //for normal exit for test run configuration
    }

}
