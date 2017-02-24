package com.jc.multipong.bootstrap;

import com.jc.multipong.bootstrap.nio.ServerThread;
import com.jc.multipong.bootstrap.threads.WorkerThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by jchagas on 22/02/2017.
 */
public class Bootstrap {


    private static final Logger logger = LoggerFactory.getLogger(Bootstrap.class);

    public static void main(String args[]) {
        logger.info("starting server...");
        Thread serverThread = new Thread(ServerThread.getInstance());
        serverThread.setDaemon(false);
        serverThread.start();

        Thread workerThread = new Thread(WorkerThread.getInstance());
        workerThread.setDaemon(false);
        workerThread.start();
    }

}
