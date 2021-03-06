package core;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import util.FTPConn;
import util.MongoConnector;

import java.net.SocketException;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.StringTokenizer;

public class IndexProcessor {

    private Date d;
    private FTPConn ftpConn;

    public IndexProcessor(Date d, FTPConn ftpConn)
    {
        // create index processor with date=d
        this.d = d;
        this.ftpConn = ftpConn;
    }

    public String toString()
    {
        return new SimpleDateFormat("yyyy-MM-dd").format(d);
    }

    public String getDailyIndexPath()
    {
        int quarter = (Integer.parseInt(new SimpleDateFormat("MM").format(d)) + 2) / 3;
        // "/edgar/daily-index/" + date.substring(0,4) + "/QTR" + qtr
        return new SimpleDateFormat("'/edgar/daily-index/'yyyy'/QTR'" + quarter +"'/form.'yyyyMMdd'.idx'").format(d);
    }

    public boolean getEdgarIndexAndSaveInDB()
    {
        String fileContent = "";
        try
        {
            fileContent = ftpConn.getFile(getDailyIndexPath(), true);
            if(fileContent.equals("")) throw new NullPointerException("");
        } catch(NullPointerException e)
        {
            try
            {
                fileContent = ftpConn.getFileGZ(getDailyIndexPath(), true);
            } catch(Exception e2)
            {
                if(e2 instanceof SocketException)
                {
                    ftpConn.Close();
                    ftpConn.isReady();
                    return false;
                }
                e2.printStackTrace();
                return false;
            }
        } catch(Exception e)
        {
            e.printStackTrace();
            return false;
        }

        /* got right fileContent */
        StringTokenizer stringTokenizer = new StringTokenizer(fileContent, "\r\n");
        String token;
        String formType="";
        String companyName="";
        String CIK="";
        String dateFiled="";
        String fileName="";

        for(int i=0; i<7; i++)
        {
            try
            {
                stringTokenizer.nextToken();
            } catch (Exception e)
            {
                e.printStackTrace();
                return false;
            }
        }

        int insertCount =0;

        while(stringTokenizer.hasMoreTokens())
        {
            try {
                token = stringTokenizer.nextToken();
                formType = token.substring(0, 12).trim();
                companyName = token.substring(12, 68).replaceAll("[:\\\\/*?|<>]", "_").trim();
                CIK = token.substring(74, 86).trim();
                dateFiled = token.substring(86, 98).trim();
                fileName = token.substring(98).trim();
                String insertID = MongoConnector.queryInsert("form", new BasicDBObject().append("formType", formType)
                        .append("companyName", companyName).append("CIK", CIK).append("dateFiled", dateFiled).append("fileName", fileName)
                        , null);
                if(insertID == null)
                {
                    // try once more
                    MongoConnector.queryInsert("form", new BasicDBObject().append("formType", formType)
                            .append("companyName", companyName).append("CIK", CIK).append("dateFiled", dateFiled).append("fileName", fileName)
                            , null);
                }
                insertCount++;
                if(insertCount%10 == 0) System.out.print("#");
            } catch(StringIndexOutOfBoundsException e)
            {
                // e.printStackTrace();
            }catch (Exception e)
            {
                e.printStackTrace();
            }

        }
        System.out.println();

        return true; // empty for index create failure
    }



}
