package ui;

import runnable.EdgarGetter;

public class ContentDownloader {

    public volatile static int numDocument = 1000000;

    public static void main(String[] args)
    {
        int numThreads;

        Thread[] threads = new Thread[20];

        for(int i=0; i<ThreadCounter.MAX_THREAD; i++)
        {
            Runnable r = new EdgarGetter();
            Thread t = new Thread(r);
            t.start();
        }

        while(true)
        {
            try
            {
                Thread.sleep(5000); // 5 sec
            } catch (InterruptedException e)
            {
                e.printStackTrace(); // shall not happen
            }

            if(!threads[ThreadCounter.currentNum].isAlive())
            {
                Runnable r = new EdgarGetter();
                Thread t = new Thread(r);
                threads[ThreadCounter.currentNum] = t;
                t.start();
            };

            ThreadCounter.add();
            if(ContentDownloader.numDocument <= 0) break;
        }

    }

    public static class ThreadCounter {

        protected static final int MAX_THREAD = 20;
        public static int currentNum = 0;

        public synchronized static void add()
        {
            currentNum++;
            if(currentNum == MAX_THREAD) currentNum = 0;
        }

        public synchronized static void deduct()
        {
            currentNum--;
        }

    }


}
