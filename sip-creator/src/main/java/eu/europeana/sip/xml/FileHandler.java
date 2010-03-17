package eu.europeana.sip.xml;

import eu.europeana.sip.mapping.Statistics;
import org.apache.log4j.Logger;

import javax.swing.SwingUtilities;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

/**
 * Load and save statistics and report back
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

public class FileHandler {
    private static final Logger logger = Logger.getLogger(FileHandler.class);

    public interface LoadListener {

        void success(List<Statistics> list);

        void failure(Exception exception);

        void finished();
    }

    public static void loadStatistics(File file, LoadListener loadListener) {
        Thread thread = new Thread(new StatisticsLoader(file, loadListener));
        thread.start();
    }

    public static void compileStatistics(File file, File statisticsFile, int counterListSize, LoadListener loadListener) {
        Thread thread = new Thread(new AnalysisRunner(file, statisticsFile, loadListener, counterListSize));
        thread.start();
    }

    private static class StatisticsLoader implements Runnable {
        private File file;
        private LoadListener loadListener;

        private StatisticsLoader(File file, LoadListener loadListener) {
            this.file = file;
            this.loadListener = loadListener;
        }

        @Override
        public void run() {
            try {
                logger.info("Loading statistics from [" + file.getAbsolutePath() + "]");
                final List<Statistics> statisticsList = loadStatisticsList(file);
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        loadListener.success(statisticsList);
                    }
                });
            }
            catch (final Exception e) {
                file.delete();
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        loadListener.failure(e);
                    }
                });
            }
            finally {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        loadListener.finished();
                    }
                });
            }
        }

        @SuppressWarnings("unchecked")
        private List<Statistics> loadStatisticsList(File file) {
            try {
                ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file)));
                List<Statistics> statisticsList = (List<Statistics>) ois.readObject();
                ois.close();
                return statisticsList;
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static class AnalysisRunner implements Runnable {
        private AnalysisParser parser = new AnalysisParser();
        private File file, statisticsFile;
        private LoadListener loadListener;
        private int counterListSize;

        private AnalysisRunner(File file, File statisticsFile, LoadListener loadListener, int counterListSize) {
            this.file = file;
            this.statisticsFile = statisticsFile;
            this.loadListener = loadListener;
            this.counterListSize = counterListSize;
        }

        @Override
        public void run() {
            try {
                logger.info("Gathering statistics from [" + file.getAbsolutePath() + "]");
                InputStream is = new FileInputStream(file);
                parser.parseFile("Kick", is);
                final List<Statistics> statisticsList = parser.getStatistics(counterListSize);
                logger.info("Going to save statistics to [" + statisticsFile.getAbsolutePath() + "]");
                saveStatisticsList(statisticsList, statisticsFile);
                logger.info("Saved statistics to [" + statisticsFile.getAbsolutePath() + "]");
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        loadListener.success(statisticsList);
                    }
                });
            }
            catch (final Exception e) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        loadListener.failure(e);
                    }
                });
            }
            finally {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        loadListener.finished();
                    }
                });
            }
        }

        private void saveStatisticsList(List<Statistics> list, File file) throws IOException {
            ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
            oos.writeObject(list);
            oos.close();
        }

    }

}

