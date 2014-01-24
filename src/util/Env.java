package util;

import java.io.*;

public class Env {

    public static String hostName = "ftp.sec.gov";
    public static int serverPort = 21;
    public static String userName = "anonymous";
    public static String userPass = "smartlove@business.kaist.ac.kr";

    public static int WriteEnv(String varName, String content) {

        try {

            File f = new File("data/env/" + varName);
            if(!f.exists())
            {
                f.createNewFile();
            }

            BufferedWriter br = new BufferedWriter(new FileWriter(f));
            br.write(content);
            br.close();

        } catch (Exception e) {
            e.printStackTrace();
            return -1; // write Fail
        }

        return 0;
    }

    public static String GainEnv(String varName) {
        String data;

        try {

            File f = new File("data/env/" + varName);
            if(!f.exists())
            {
                return ""; // file does not exist
            }
            BufferedReader br = new BufferedReader(new FileReader(f));
            data = br.readLine();
            br.close();

        } catch (Exception e) {
            e.printStackTrace();
            return ""; // errors are assumed as no data
        }
        return data;
    }

    public static boolean hasEnoughHDD()
    {
        File f = new File(Env.class.getProtectionDomain().getCodeSource().getLocation().getPath());
        float freeSpace = f.getFreeSpace();
        if(freeSpace / 1024 / 1024 / 1024 < 4 /* if size of local HDD is lower than 4GB */)
        {
            return false;
        }

        return true;
    }

}
