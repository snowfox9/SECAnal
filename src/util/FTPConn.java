package util;

import java.io.*;
import java.util.zip.*;


import org.apache.commons.net.ftp.*;

import core.log.SimpleLogger;

public class FTPConn {

    String username;
    String password;
    String host;
    int serverPort;
    FTPClient ftpClient;
    boolean isConnected = false;

    public FTPConn(String host, int serverPort, String username, String password)
    {
        this.username = username;
        this.password = password;
        this.host = host;
        this.serverPort = serverPort;
    }

    private void showServerReply(FTPClient ftpClient)
    {
        String[] replies = ftpClient.getReplyStrings();

        if(replies != null && replies.length>0)
        {
            for(String aReply : replies)
            {
                System.out.println("SERVER: " + aReply);
            }
        }
    }

    public void setWorkingDirectory(String dir) throws Exception
    {
        SimpleLogger.writeNetLog("Setting Working Directory to " + dir);
        ftpClient.changeWorkingDirectory(dir);
    }


    FTPFile[] listFiles;
    private boolean checkExistence(String filename, boolean isNewDir) throws IOException
    {
        if(isNewDir)
        {
            SimpleLogger.writeNetLog("Downloading File List...");
            listFiles = ftpClient.listFiles();
        }

        SimpleLogger.writeNetLog("Checking Existence " + filename);
        for(FTPFile aFile : listFiles)
        {
            if(aFile.getName().equals(filename))
            {
                return true;
            }
        }

        return false;
    }

    public String getFileGZ(String filename, boolean forceDownload) throws Exception
    {
        String filenameGZ = filename + ".gz";

        SimpleLogger.writeNetLog("Starting File Download " + filenameGZ);

        ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
        FileOutputStream out = new FileOutputStream("data/tmp");
        if(!ftpClient.retrieveFile(filenameGZ, out))
        {
            SimpleLogger.writeNetLog("Attempting To Download " + filenameGZ + " Failed");
            return "";
        }

        out.close();

        BufferedInputStream br = new BufferedInputStream(new FileInputStream("data/tmp"));
        showServerReply(ftpClient);
        int fileSize = (int) new File("data/tmp").length();
        byte[] fileContent = new byte[fileSize];
        byte buffer[] = new byte[0x10000]; // buffer with size 64k
        int readCount;
        int length = 0;

        while( (readCount = br.read(buffer)) > 0  ) {
            System.arraycopy(buffer, 0, fileContent, length, readCount);
            length += readCount;
        }

        br.close();

        File f = new File("data/tmp");

        FileOutputStream fout = new FileOutputStream(f);
        fout.write(fileContent);
        fout.close();

        // download complete and byte[] result has data
        FileInputStream fi = new FileInputStream(f);
        GZIPInputStream gzin = new GZIPInputStream(fi);
        length = 0;
        while( (readCount = gzin.read(buffer)) > 0)
        {
            int preLength = length;
            length += readCount;
            byte[] temp = new byte[fileContent.length];
            System.arraycopy(fileContent, 0, temp, 0, fileContent.length);
            fileContent = new byte[length];
            System.arraycopy(temp, 0, fileContent, 0, preLength);
            System.arraycopy(buffer, 0,  fileContent, preLength, readCount);
            /* we are not sure the file size when unzipped */
        }

        System.out.println();
        gzin.close();

        return new String(fileContent, 0, fileContent.length); // as all file contents are known as string
    }

    public boolean checkExistenceIndex(String fileName) throws Exception
    {
        return ftpClient.mlistFile(fileName) == null;
    }

    public String getFile(String filename, boolean forceDownload) throws Exception
    {
        SimpleLogger.writeNetLog("Starting File Download " + filename);
        ftpClient.setFileType(FTP.ASCII_FILE_TYPE);
        FTPFile f = ftpClient.mlistFile(filename);
        long fileSize = f.getSize();
        if(fileSize >= 0x100000 /* file size bigger than 1MB */ && !forceDownload)
        {
            return ""; // do not download
        }

        byte[] fileContent = new byte[(int) (fileSize/0x10000 + 1) * 0x10000];

        InputStream in = ftpClient.retrieveFileStream(filename);
        BufferedInputStream br = new BufferedInputStream(in);
        showServerReply(ftpClient);
        byte buffer[] = new byte[0x10000]; // buffer with size 64k
        int readCount;
        int currentCount = 0;
        int sharpCount = 0;

        while( (readCount = br.read(buffer)) > 0  ) {
            System.arraycopy(buffer, 0, fileContent, currentCount, readCount);
            currentCount += readCount;
            sharpCount++;

            if(sharpCount % 2 == 0)
            {
                System.out.print("#"); // print Sharp on every 128k
            }

        }
        System.out.println();

        br.close();
        in.close();

        boolean msg = ftpClient.completePendingCommand();

        if(!msg)
        {
            throw new Exception("Download Fail");
        }

        return new String(fileContent, 0, fileContent.length); // as all file contents are known as string
    }

    public int isReady()
    {
        ftpClient = new FTPClient();
        try
        {
            ftpClient.connect(host, serverPort);
            showServerReply(ftpClient);
            int replyCode = ftpClient.getReplyCode();
            if(!FTPReply.isPositiveCompletion(replyCode))
            {
                ftpClient.disconnect();
                System.out.println("connection fail : " + replyCode);
                return -1;
            } else {
                System.out.println("connection successful : " + replyCode);
            }
            boolean isLogin = ftpClient.login(username, password);
            showServerReply(ftpClient);
            if(!isLogin)
            {
                ftpClient.disconnect();
                System.out.println("login fail : ");
                return -2;
            } else {
                System.out.println("login successful");
                ftpClient.enterLocalPassiveMode();

                this.isConnected = true;
            }
        } catch(IOException e)
        {
            System.out.println(e.getMessage());
            return -1;

        }
        return 0; // normal exit
    }

    public int Close()
    {
        if(!isConnected) return -1;
        try
        {
            ftpClient.logout();
            ftpClient.disconnect();
        } catch(IOException e)
        {
            System.out.println(e.getMessage());
        }

        return 0;
    }

}
