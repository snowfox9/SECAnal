package core;
import core.log.SimpleLogger;
import util.FTPConn;
import java.io.*;

public class EdgarEntity {

    public boolean isValid;
    public String formType;
    public String companyName;
    public String CIK;
    public String dateFiled;
    public String fileName;
    private FTPConn x;

    private String ContentToSave;

    public EdgarEntity(String edgarIdxString, FTPConn x)
    {
        if(edgarIdxString.length() < 98 )
        {
            isValid = false;
        } else {
            this.x = x;
            formType = edgarIdxString.substring(0, 12).trim();
            companyName = edgarIdxString.substring(12, 68).trim().replace("/", "_");
            CIK = edgarIdxString.substring(74, 86).trim();
            dateFiled = edgarIdxString.substring(86, 98).trim();
            fileName = edgarIdxString.substring(98).trim();
        }
        try{
            Integer.parseInt(CIK);
            isValid = true;
        } catch(Exception e) {
            isValid = false;
        }

    }

    public void downloadEdgarAndSaveUsingCompanyName() throws Exception
    {

        if(this.isValid && (this.formType.equals("8-K") || this.formType.equals("8-K/A")))
        {
            x.setWorkingDirectory("/edgar/data/" + CIK);
            this.ContentToSave = x.getFile(fileName.substring(11 + CIK.length() + 1) , false);

            // save such EDGAR File To /data/COMPANYNAME/FILEDDATE_ORGNAME

            if(!this.ContentToSave.equals(""))
            {
                File f= new File("data/edgar/" + this.companyName);
                if(!f.exists())
                {
                    f.mkdir();
                }

                SimpleLogger.writeActionLog("SAVING TO " + "data/edgar/" + this.companyName + "/" + this.dateFiled + "_" + fileName.substring(11 + CIK.length() + 1));
                f = new File("data/edgar/" + this.companyName + "/" + this.dateFiled + "_" + fileName.substring(11 + CIK.length() + 1));
                if(!f.exists())
                {
                    f.createNewFile();
                }
                BufferedWriter br = new BufferedWriter(new FileWriter(f));
                br.write(this.ContentToSave);

                File f2 = new File("data/edgar/" + this.companyName + "/" + this.dateFiled + "_" + fileName.substring(11 + CIK.length() + 1).replaceFirst("txt", "htm"));
                if(!f2.exists())
                {
                    f2.createNewFile();
                }
                BufferedWriter br2 = new BufferedWriter(new FileWriter(f2));
                br2.write(this.ContentToSave);

                br.close();
                br2.close();
            }
        }
    }

    public void restoreEdgarIdxPath(String dir) throws Exception
    {
        x.setWorkingDirectory(dir);

    }

}
