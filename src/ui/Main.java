package ui;

import util.FTPConn;
import util.Env;
import java.io.*;
import core.*;
import core.log.SimpleLogger;

public class Main {

    /**
     * @param args
     */
    public static void main(String[] args) {
        // TODO Auto-generated method stub
        Main.TestRunConfig();
        SimpleLogger.InitLogger();
        SimpleLogger.writeActionLog("Init Successful");


        FTPConn x = new FTPConn(Env.hostName, Env.serverPort, Env.userName, Env.userPass);
        int reply = x.isReady();
        if(reply < 0)
        {
            SimpleLogger.writeNetLog("Connction fail");
            System.exit(-1);
        }
        SimpleLogger.writeNetLog("Connection Successful");

        Inspector i = new Inspector(x);

        try
        {
            while(!i.initiateDownloadOneDay().equals("Finish"))
            {
                i.finializeOneDay();
                Thread.sleep(1000);
            }

        } catch(InterruptedException e)
        {
            e.printStackTrace();
        } catch(Exception e)
        {
            e.printStackTrace();
        }

        x.Close();
        SimpleLogger.writeNetLog("Disconnect Successful");
        SimpleLogger.CloseLogger();
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

        String envFirstRun = Env.GainEnv("FIRSTRUN");
        if(envFirstRun.equals(""))
        {
            DateIndex.clearDateIndex();
        } else {
            // do nothing
        }

        Env.WriteEnv("FIRSTRUN", "0");

        return 0; //for normal exit for test run configuration
    }

}
