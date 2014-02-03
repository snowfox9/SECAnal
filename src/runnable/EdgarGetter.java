package runnable;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import core.log.SimpleLogger;
import util.Env;
import util.FTPConn;
import util.MongoConnector;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.net.Socket;
import java.net.SocketException;

public class EdgarGetter implements Runnable {

    FTPConn ftpConn;


    @Override
    public void run() {

        ftpConn = new FTPConn(Env.hostName, Env.serverPort, Env.userName, Env.userPass);
        ftpConn.isReady();

        DBObject object;
        String _id;
        String companyName;
        String dateFiled;
        String CIK;
        String fileName;

        synchronized (MongoConnector.class)
        {
            object = MongoConnector.queryFindOne("formType", new BasicDBObject()
                   .append("isDone", new BasicDBObject().append("$exists", false))
                  .append("formType", "8-K")
            );

            if(object == null)
            {
                return;
            }

            _id = (String) object.get("_id");
            companyName = (String) object.get("companyName");
            dateFiled = (String) object.get("dateFiled");
            CIK = (String) object.get("CIK");
            fileName = (String) object.get("fileName");

            MongoConnector.queryUpdate("form", new BasicDBObject().append("_id", _id), new BasicDBObject()
                    .append("$set", new BasicDBObject("isDone", -1))
                    , true, true, null);
        }

        try
        {
            String filePath = (String) object.get("fileName");
            String fContent = ftpConn.getFile(filePath, false);

            // check if specified terms are written


            File f;

            if(fContent.equals(""))
            {
                MongoConnector.queryUpdate("form", new BasicDBObject().append("_id", _id),
                        new BasicDBObject().append("$set", new BasicDBObject().append("isDone", 2)), true, true, null);
            }
            else {
                SimpleLogger.writeActionLog("SAVING TO " + "data/edgar/" + companyName + "/" + dateFiled + "_" + fileName.substring(11 + CIK.length() + 1));
                f = new File("data/edgar/" + companyName + "/" + dateFiled + "_" + fileName.substring(11 + CIK.length() + 1));
                if(!f.exists())
                {
                    f.createNewFile();
                    BufferedWriter br = new BufferedWriter(new FileWriter(f));
                    br.write(fContent);
                } else {
                    // do nothing as file has been already downloaded
                    assert(true);
                }

                MongoConnector.queryUpdate("form", new BasicDBObject().append("_id", _id),
                        new BasicDBObject().append("$set", new BasicDBObject().append("isDone", 1)), true, true, null);
            }

        } catch(SocketException e)
        {
            e.printStackTrace();
            ftpConn.Close();
            ftpConn.isReady();
        } catch (Exception e)
        {
            e.printStackTrace();

        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();    //To change body of overridden methods use File | Settings | File Templates.
    }
}
