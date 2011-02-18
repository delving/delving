package org.oclc.oai.harvester.app;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.oclc.oai.harvester.verb.ListRecords;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A worker thread that hits a site for harvesting
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class HarvestWorker implements Runnable {
    private Logger log;
    private List<HarvestTask> tasks = new CopyOnWriteArrayList<HarvestTask>();
    private Boss boss;
    private String baseUrl;
    private File outputDirectory;
    private Thread thread;

    public HarvestWorker(Boss boss, String baseUrl, File outputDirectory) {
        this.log = Logger.getLogger(baseUrl);
        this.boss = boss;
        this.baseUrl = baseUrl;
        this.outputDirectory = outputDirectory;
    }

    public void add(HarvestTask task) {
        if (tasks.indexOf(task) >= 0) {
            log.warn("Added a task that is already in the worker: " + task);
        }
        task.pending();
        boolean wasEmpty = tasks.isEmpty();
        tasks.add(task);
        if (wasEmpty && thread == null) {
            log.info("Starting worker for " + baseUrl + " with " + tasks.size() + " tasks to perform.");
            thread = new Thread(this);
            thread.start();
        }
    }

    public Thread abort() {
        Thread killed = thread;
        thread = null;
        return killed;
    }

    public void run() {
        try {
            while (!tasks.isEmpty() && thread != null) {
                log.info("Worker for " + baseUrl + " starting task " + tasks.size());
                HarvestTask task = tasks.remove(0);
                if (task.getStatus() == HarvestTask.Status.ABORTED) {
                    log.info("Aborting " + task + " before processing it");
                    task.finishedUnsuccessfully();
                    continue;
                }
                task.processing();
                File outputFile = new File(outputDirectory, task.getOutput());
                File logFile = new File(outputDirectory, task.getOutput() + ".log");
                logFile.delete();
                FileAppender appender = new FileAppender(
                        new PatternLayout("%d{HH:mm:ss} %-5p %m%n"),
                        logFile.getAbsolutePath()
                );
                log.addAppender(appender);
                log.info("Processing " + task + ", creating file " + outputFile.getAbsolutePath());
                Writer writer = new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8");
                writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
                writer.write("<harvest>\n");
                log.info("Starting request series for " + task);
                try {
                    ListRecords listRecords;
                    boolean continueFlag = false;
                    int hit = 0;
                    int totalRecordCount = 0;
                    if (task.hasLastToken()) {
                        listRecords = new ListRecords(
                                task.getBaseUrl(),
                                task.getLastHarvestString(),
                                null,
                                task.getSpec(),
                                task.getPrefix()
                        );
                    }
                    else {
                        listRecords = new ListRecords(
                                task.getBaseUrl(),
                                task.getLastToken(),
                                task.getSpec(),
                                task.getPrefix());
//                        log.info("First:\n"+listRecords);
//                        listRecords = null;
//                        continueFlag = true;
                    }
                    while (listRecords != null || continueFlag) {
                        if (continueFlag && listRecords != null) {
                            continueFlag = false;
                        }
                        NodeList errors = listRecords.getErrors();
                        if (errors != null && errors.getLength() > 0) {
                            log.warn("Found errors");
                            int length = errors.getLength();
                            for (int i = 0; i < length; ++i) {
                                Node item = errors.item(i);
                                log.warn(item);
                            }
                            log.info("Error record: " + listRecords.toString());
                            task.abort();
                            break;
                        }
                        hit++;
                        String token = "";
                        String records = listRecords.toString();
                        int recordCount = countRecordOccurrences(records);
                        totalRecordCount += recordCount;
                        log.info("Records created: " + recordCount + ", total is " + totalRecordCount);
                        writer.write(records);
                        writer.write("\n");

                        token = listRecords.getResumptionToken();
                        if (token == null) {
                            token = task.getLastToken();
                        }
                        if (token == null || token.trim().isEmpty()) {
                            token = null;
                            continueFlag = false;
                        }
                        if (token == null || token.isEmpty()) {
                            listRecords = null;
                        }
                        else if (task.getStatus() != HarvestTask.Status.PROCESSING || thread == null) {
                            log.info("Aborting " + task);
                            task.abort();
                            listRecords = null;
                        }
                        else {
                            log.info("Resuming " + task + ", hit number " + hit + " with token " + token);
                            task.saveToken(token);
                            if (token != null) {
                                continueFlag = true;
                            }
                            try {
                                listRecords = new ListRecords(
                                        task.getBaseUrl(),
                                        task.getLastToken(),
                                        task.getSpec(),
                                        task.getPrefix());
                            }
                            catch (Exception e) {
                                log.info("Exception caught...: " + e);
                                //Nothing...
                            }
                        }
                    }
                    writer.write("</harvest>\n");
                    writer.close();
                }
                catch (IOException e) {
                    log.warn("Problem accessing " + baseUrl, e);
                    task.abort();
                }
                if (task.getStatus() == HarvestTask.Status.PROCESSING) {
                    log.info("Successfully finished " + task);
                    task.finishedSuccessfully();
                }
                else {
                    log.info("Terminating " + task + ", removing file - INACTIVATED / The file remains because Italian OAI is stupid - " + outputFile.getAbsolutePath());
                    //outputFile.delete();
                    task.finishedUnsuccessfully();
                }
                boss.saveConfiguration(); // "successful" and maybe "lastHarvest" has changed
                log.removeAllAppenders();
                appender.close();
            }
            log.info("Finished " + baseUrl);
        }
        catch (Exception e) {
            log.error("Problem harvesting " + baseUrl, e);
        }
        finally {
            thread = null;
        }
    }

    private int countRecordOccurrences(String records) {
        int count = 0;
        int pos = 0;
        while ((pos = records.indexOf("<record>", pos)) >= 0) {
            count++;
            pos++;
        }
        return count;
    }

    public String toString() {
        return baseUrl;
    }

    public interface Boss {
        void saveConfiguration();
    }
}