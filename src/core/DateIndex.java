package core;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import util.Env;

public class DateIndex {

    public final int MaxDate = 5083;

    public static void clearDateIndex()
    {
        char[] dateMarks;
        dateMarks = new char[6000];

        for(int i=0; i<dateMarks.length; i++)
        {
            dateMarks[i] = '0';
        }
        Env.WriteEnv("DATEMARKS", new String(dateMarks));
    }

    private char[] dateMarks;

    public DateIndex()
    {
        String dateMarks = Env.GainEnv("DATEMARKS");
        this.dateMarks = dateMarks.toCharArray();
    }

    private final int StandardYear = 2000;
    private final int StandardMonth = 1;
    private final int StandardDay = 1;
    private final String StandardDate = "2000-01-01";

    public int dateStringToIndex(String date)
    {
        long diffDays;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        try{
            String standard = this.StandardDate;
            Date dStandard = format.parse(standard);
            Date dDate = format.parse(date);
            long diff= dDate.getTime() - dStandard.getTime();
            diffDays = diff / 1000 / 60 / 60 / 24;

            System.out.println("DATEDIFF = INDEX = " + diffDays);
        } catch(Exception e)
        {
            return -1;
        }

        return (int) diffDays;
    }

    public String indexToDateString(int index)
    {
        if(index <0 || index > MaxDate) return "";
        Calendar c = Calendar.getInstance();
        c.set(this.StandardYear, this.StandardMonth-1, this.StandardDay);
        c.add(Calendar.DATE, index);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

        return format.format(c.getTime());
    }

    public Calendar dateStringToDate(String date)
    {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        try
        {
            c.setTime(format.parse(date));
        } catch(Exception e)
        {
            e.printStackTrace();
            return null;
        }
        return c;
    }

    public int getMinIndexNoData()
    {

        for(int i=0; i<MaxDate; i++)
        {
            if(this.dateMarks[i] == '0')
            {
                return i;
            }
        }

        return MaxDate;
    }

    public void writeDateMarks()
    {
        Env.WriteEnv("DATEMARKS", new String(this.dateMarks));
    }

    public void closeDateMark(int index)
    {
        dateMarks[index] = '1';
    }

}
