package runnable;

import core.log.SimpleLogger;
import util.MongoConnector;

public class Terminate extends Thread {

    @Override
    public void run() {
        MongoConnector.close();
        SimpleLogger.CloseLogger();
    }

}
