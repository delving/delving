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
 * @author Serkan Demirel <serkan@blackbuilt.nl>
 */

public class FileHandler {
    private static final Logger logger = Logger.getLogger(FileHandler.class);

    public interface Listener {

        void success(List<Statistics> list);

        void failure(Exception exception);

        void finished();
    }

    public static void getStatistics(File file, Listener listener) {
        File statisticsFile = createStatisticsFile(file);
        if (statisticsFile.exists()) {
            loadStatistics(file, listener);
        }
        else {
//            compileStatistics();
        }
    }

    public static void loadStatistics(File file, Listener listener) {
        Thread thread = new Thread(new StatisticsLoader(file, listener));
        thread.start();
    }

    public static void compileStatistics(File file, File statisticsFile, int counterListSize, Listener listener) {
        Thread thread = new Thread(new AnalysisRunner(file, statisticsFile, listener, counterListSize));
        thread.start();
    }

    public static void loadStatistics(File file, Listener listener, AnalysisParser.Listener analysisListener) {
        Thread thread = new Thread(new StatisticsLoader(file, listener, analysisListener));
        thread.start();
    }

    public static void compileStatistics(File file, File statisticsFile, int counterListSize, Listener listener, AnalysisParser.Listener analysisListener) {
        Thread thread = new Thread(new AnalysisRunner(file, statisticsFile, listener, counterListSize, analysisListener));
        thread.start();
    }


    private static File createStatisticsFile(File file) {
        return new File(file.getParentFile(), file.getName() + ".statistics");
    }

    private static File createMappingFile(File file) { // todo: writing and reading of this
        return new File(file.getParentFile(), file.getName() + ".mapping");
    }

    private static class StatisticsLoader implements Runnable {
        private File file;
        private Listener listener;
        private AnalysisParser.Listener analysisListener;

        private StatisticsLoader(File file, Listener listener) {
            this.file = file;
            this.listener = listener;
        }

        private StatisticsLoader(File file, Listener listener, AnalysisParser.Listener analysisListener) {
            this.file = file;
            this.listener = listener;
            this.analysisListener = analysisListener;
        }

        @Override
        public void run() {
            try {
                logger.info("Loading statistics from [" + file.getAbsolutePath() + "]");
                final List<Statistics> statisticsList = loadStatisticsList(file);
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        listener.success(statisticsList);
                    }
                });
            }
            catch (final Exception e) {
                file.delete();
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        listener.failure(e);
                    }
                });
            }
            finally {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        listener.finished();
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
        private Listener listener;
        private int counterListSize;

        private AnalysisRunner(File file, File statisticsFile, Listener listener, int counterListSize) {
            this.file = file;
            this.statisticsFile = statisticsFile;
            this.listener = listener;
            this.counterListSize = counterListSize;
        }

        private AnalysisRunner(File file, File statisticsFile, Listener listener, int counterListSize,  AnalysisParser.Listener analysisListener) {
            this.file = file;
            this.statisticsFile = statisticsFile;
            this.counterListSize = counterListSize;
            this.listener = listener;
            parser.setAnalysisListener(analysisListener);
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
                        listener.success(statisticsList);
                    }
                });
            }
            catch (final Exception e) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        listener.failure(e);
                    }
                });
            }
            finally {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        listener.finished();
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

