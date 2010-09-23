/**
 * Copyright 2006 OCLC, Online Computer Library Center
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.oclc.oai.harvester.app;

import javax.swing.*;
import org.apache.log4j.Logger;

import java.io.IOException;
import static java.lang.System.out;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Harvester {
    private static Logger LOG = Logger.getLogger(Harvester.class);

    private HarvestConfig harvestConfig;
    private boolean prepared;
    private boolean saveHarvestConfig;
    private HarvestWorker.Boss boss = new HarvestWorker.Boss() {
        public void saveConfiguration() {
            saveHarvestConfig = true;
        }
    };
    private Map<String,HarvestWorker> workers = new ConcurrentHashMap<String,HarvestWorker>();

    private Harvester() throws IOException {
        harvestConfig = HarvestConfig.load();
    }

    private synchronized void prepareHarvest() {
        LOG.info("preparing for harvest: shutdown hook, and config file saver daemon");
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() {
                LOG.info("Program terminated");
                for (HarvestWorker worker : workers.values()) {
                    Thread thread = worker.abort();
                    if (thread == null) {
                        continue;
                    }
                    LOG.info("Terminating worker: "+worker);
                    try {
                        thread.join();
                    }
                    catch (InterruptedException e) {
                        LOG.warn("Interrupted during thread.join",e);
                    }
                }
                saveConfig();
            }
        }));
        Thread saveThread = new Thread(new Runnable() {
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(500);
                    }
                    catch (InterruptedException e) { /* ignore */ }
                    if (!saveConfig()) {
                        break;
                    }
                }
            }
        });
        saveThread.setDaemon(true);
        saveThread.start();
        harvestConfig.outputDirectory.mkdirs();
        prepared = true;
    }

    private void list() {
        out.println("List of Harvests:");
        out.println("============");
        int count = 1;
        for (HarvestConfig.Harvest harvest : harvestConfig.harvests) {
            out.println(count + ": [" + harvest + "]");
            count++;
        }
        out.println("============");
        out.println("Run this program with these numbers as arguments to run the specific harvests.");
        out.println("The argument 'all' initiates all the harvests.");
    }

    private void addTask(HarvestTask task) {
        if (!prepared) {
            prepareHarvest();
        }
        HarvestWorker worker = workers.get(task.getBaseUrl());
        if (worker == null) {
            worker = new HarvestWorker(boss,task.getBaseUrl(),harvestConfig.outputDirectory);
            workers.put(task.getBaseUrl(),worker);
        }
        worker.add(task);
    }

    private void harvest(Set<Integer> harvestNumbers) {
        for (Integer number : harvestNumbers) {
            if (number < 1 || number > harvestConfig.harvests.size()) {
                out.println("Illegal harvest number: " + number);
                list();
                return;
            }
            addTask(new HarvestTask(null, harvestConfig.harvests.get(number - 1)));
        }
    }

    private void harvestAll() {
        for (HarvestConfig.Harvest harvest : harvestConfig.harvests) {
            addTask(new HarvestTask(null, harvest));
        }
    }

    private boolean saveConfig() {
        if (saveHarvestConfig) {
            try {
                HarvestConfig.save(harvestConfig);
                LOG.info("Saved config");
            }
            catch (IOException e) {
                LOG.error("Unable to save config!", e);
                return false;
            }
            saveHarvestConfig = false;
        }
        return true;
    }

    private JFrame creatGUI() {
        return new HarvesterGUI(harvestConfig, new Control() {
            public void add(HarvestTask task) {
                addTask(task);
            }
        });
    }

    public interface Control {
        void add(HarvestTask task);
    }

    public static void main(String[] args) throws Exception {
        Harvester harvester = new Harvester();
        if (args.length == 0) {
            harvester.creatGUI().setVisible(true);
        }
        else if (args.length == 1 && "list".equalsIgnoreCase(args[0])) {
            harvester.list();
        }
        else if (args.length == 1 && "all".equalsIgnoreCase(args[0])) {
            harvester.harvestAll();
        }
        else {
            Set<Integer> harvests = new HashSet<Integer>();
            for (String arg : args) {
                try {
                    harvests.add(new Integer(arg));
                }
                catch (NumberFormatException e) {
                    out.println("Don't understand argument [" + arg + "]");
                    harvester.list();
                }
            }
            harvester.harvest(harvests);
        }
    }

}