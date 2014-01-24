package core.log;

import java.io.*;
import java.sql.Timestamp;

public class SimpleLogger {
    private static String loggerPath = "data/log/log.txt";
    private static File f;
    private static BufferedWriter br;

    private static String now()
    {
        java.util.Date t = new java.util.Date();
        return new Timestamp(t.getTime()).toString();
    }

    public static int InitLogger()
    {
        File f = new File("data");
        if(f.exists() && f.isDirectory())
        {
            // fine
        } else {
            f.mkdir();
        }

        f = new File("data/log");
        if(f.exists() && f.isDirectory())
        {
            // fine
        } else {
            f.mkdir(); // no LOG folder
        }

        f = new File(SimpleLogger.loggerPath);
        try{
            if(!f.exists())
            {
                if(!f.createNewFile())
                {
                    return -2; // file to create data/log/log.txt
                } else {
                    // file create successful
                }
            }

            br = new BufferedWriter(new FileWriter(f, true));
            br.write(String.format("%s Logger Started  : ", now())); br.newLine();
            System.out.println(String.format("%s Logger Started  : ", now()));
            br.flush();

        } catch(Exception e) {
            e.printStackTrace();
            return -1; // error when opening log file
        }

        return 0;
    }

    public static int CloseLogger()
    {
        try
        {
            SimpleLogger.writeActionLog("Logger Terminate");
            br.close();
        } catch(IOException e)
        {
            e.printStackTrace();
            return -1;
        }

        return 0;
    }

    public synchronized static int writeNetLog(String log)
    {
        try{

            br.write(String.format("%s Network Info    : %s", now(), log));
            br.newLine();
            System.out.println(String.format("%s Network Info    : %s", now(), log));
            br.flush();

        } catch(Exception e)
        {
            e.printStackTrace();
            return -1;
        }

        return 0; // normal return
    }

    public synchronized static int writeDataLog(String log)
    {
        try{

            br.write(String.format("%s Data Inspection : %s", now(), log));
            br.newLine();
            System.out.println(String.format("%s Data Inspection : %s", now(), log));
            br.flush();

        } catch(Exception e)
        {
            e.printStackTrace();
            return -1;
        }
        return 0;
    }

    public synchronized static int writeActionLog(String log)
    {
        try{

            br.write(String.format("%s New Action      : %s", now(), log));
            br.newLine();
            System.out.println(String.format("%s New Action      : %s", now(), log));
            br.flush();

        } catch(Exception e)
        {
            e.printStackTrace();
            return -1;
        }
        return 0; // normal return
    }

}
