package core;

import util.FTPConn;

import java.text.SimpleDateFormat;
import java.util.*;
import core.log.*;

public class Inspector {

    private static final int STATUS_CONN = -1;
    private static final int STATUS_DOWNLOAD_READY = 0;
    @SuppressWarnings("unused")
    private static final int STATUS_INDEX_READY = 1;
    @SuppressWarnings("unused")
    private static final int STATUS_DOWNLOADING = 2;
    @SuppressWarnings("unused")
    private static final int STATUS_FINISH = 3;

    private FTPConn x;
    private DateIndex di;
    private int status;
    private Calendar c;
    private int currentIndexWorking;
    private boolean wdChange;

    public Inspector(FTPConn x)
    {
        this.x = x;
        di = new DateIndex();
        this.wdChange = true;
        this.status = Inspector.STATUS_CONN;
    }

    public String initiateDownloadOneDay() throws Exception
    {
        if(this.status != Inspector.STATUS_CONN)
        {
            return "";
        }

        c = this.getMinDayData();

        if(c == null)
        {
            return "Finish";
        }

        String wd = this.DateToEdgarDirectory(c);
        String fn = this.getIndexFromEdgarOneDay(c);

        x.setWorkingDirectory(wd);
        this.status = Inspector.STATUS_DOWNLOAD_READY;

        String idxContent = x.getFile(fn, true);
        if(idxContent.equals("No File"))
        {
            idxContent = x.getFileGZ(fn, false);
        }

        String[] lines = idxContent.split("\n");
        for(String aline : lines)
        {
            EdgarEntity e = new EdgarEntity(aline, x);
            if(e.isValid)
            {
                e.downloadEdgarAndSaveUsingCompanyName();
            }
        }

        x.setWorkingDirectory(wd);

        return "";
    }

    public void finializeOneDay()
    {
        this.status = Inspector.STATUS_CONN;

        SimpleLogger.writeDataLog("Closing Date : " + di.indexToDateString(this.currentIndexWorking));
        di.closeDateMark(this.currentIndexWorking);
        di.writeDateMarks();
    }

    private Calendar getMinDayData()
    {
        int minIndexNoData = di.getMinIndexNoData();
        if(minIndexNoData == di.MaxDate)
        {
            return null;
        }

        this.currentIndexWorking = minIndexNoData;
        String minIndexDate = di.indexToDateString(minIndexNoData);
        Calendar c = di.dateStringToDate(minIndexDate);
        return c;
    }

    private String oldEdgarDir = "";

    private String DateToEdgarDirectory(Calendar c)
    {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
        String date = format.format(c.getTime());
        int qtr = (c.get(Calendar.MONTH)-1) / 3 + 1;

        String edgarDir = "/edgar/daily-index/" + date.substring(0,4) + "/QTR" + qtr;

        if(this.oldEdgarDir.equals(edgarDir))
        {
            this.wdChange = false;
        } else {
            this.wdChange = true;
        }
        this.oldEdgarDir = edgarDir;

        return edgarDir;
    }

    public String getIndexFromEdgarOneDay(Calendar c)
    {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
        String date = format.format(c.getTime());

        return "form." + date + ".idx";
    }

    public void Close()
    {
        x.Close();
        di.writeDateMarks();
    }

}