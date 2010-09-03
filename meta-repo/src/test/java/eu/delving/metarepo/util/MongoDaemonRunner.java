package eu.delving.metarepo.util;

import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Run the daemon
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class MongoDaemonRunner extends Thread {
    private static final Logger LOG = Logger.getLogger(MongoDaemonRunner.class);
    private static final File MONGO = new File("meta-repo/target/mongo");
    private Process process;
    private boolean running = false;
    private boolean killed;

    @Override
    public void run() {
        try {
            if (MONGO.exists()) {
                for (File file : MONGO.listFiles()) {
                    if (!file.delete()) {
                        throw new IOException("couldn't delete "+file.getAbsolutePath());
                    }
                    LOG.info("Deleted "+file.getAbsolutePath());
                }
                if (!MONGO.delete()) {
                    throw new IOException("couldn't delete "+MONGO.getAbsolutePath());
                }
                LOG.info("Deleted "+MONGO.getAbsolutePath());
            }
            if (!MONGO.mkdirs()) {
                throw new IOException("couldn't make directories from "+MONGO.getAbsolutePath());
            }
            LOG.info("Firing up the daemon");
            process = Runtime.getRuntime().exec(new String[]{
                    "/opt/local/bin/mongod",
                    "--dbpath",
                    MONGO.getAbsolutePath()
            });
            BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                if (line.contains("waiting for connections")) {
                    running = true;
                }
                LOG.info(line);
            }
            LOG.info("Daemon is dead");
        }
        catch (IOException e) {
            if (!killed) {
                e.printStackTrace();
            }
        }
    }

    public void waitUntilRunning() {
        try {
            int tries = 100; // ten seconds
            while (!running && (tries-- > 0)) {
                Thread.sleep(100);
            }
            if (!running) {
                throw new RuntimeException("Timeout trying to run daemon");
            }
            else {
                LOG.info("Daemon running!");
            }
        }
        catch (InterruptedException e) {
            // nothing
        }
    }

    public void kill() {
        killed = true;
        process.destroy();
    }

    public static void main(String[] args) {
        final MongoDaemonRunner runner = new MongoDaemonRunner();
        runner.start();
        runner.waitUntilRunning();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                runner.kill();
            }
        });
    }
}
