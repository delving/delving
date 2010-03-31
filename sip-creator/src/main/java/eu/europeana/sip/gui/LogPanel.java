/*
 * Copyright 2007 EDL FOUNDATION
 *
 *  Licensed under the EUPL, Version 1.0 or? as soon they
 *  will be approved by the European Commission - subsequent
 *  versions of the EUPL (the "Licence");
 *  you may not use this work except in compliance with the
 *  Licence.
 *  You may obtain a copy of the Licence at:
 *
 *  http://ec.europa.eu/idabc/eupl
 *
 *  Unless required by applicable law or agreed to in
 *  writing, software distributed under the Licence is
 *  distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *  express or implied.
 *  See the Licence for the specific language governing
 *  permissions and limitations under the Licence.
 */

package eu.europeana.sip.gui;

import org.apache.log4j.Appender;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Layout;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Gerald de Jong <geralddejong@gmail.com>
 */
public class LogPanel extends JPanel {
    private static final int MAX_LOG_LINES = 300;
    private static final long UPDATE_DELAY = 200;
    private JTextArea logTextArea = new JTextArea();
    private LogAppender logAppender;
    private int logCount;

    public LogPanel() {
        super(new BorderLayout());
        logTextArea.setEditable(false);
        add(new JScrollPane(logTextArea,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS), BorderLayout.CENTER);
    }

    public Appender createAppender(Layout layout) {
        return logAppender = new LogAppender(layout);
    }

    public void flush() {
        logAppender.close();
    }

    private class LogAppender extends AppenderSkeleton {
        private TextAreaAppender textAreaAppender = new TextAreaAppender();
        private long lastDump;

        private LogAppender(Layout layout) {
            setLayout(layout);
        }

        @Override
        public void append(LoggingEvent loggingEvent) {
            if (System.currentTimeMillis() - lastDump > UPDATE_DELAY) {
                SwingUtilities.invokeLater(textAreaAppender);
                textAreaAppender = new TextAreaAppender();
                lastDump = System.currentTimeMillis();
            }
            textAreaAppender.addMessage(layout.format(loggingEvent));
            ThrowableInformation ti = loggingEvent.getThrowableInformation();
            if (ti != null) {
                for (String string : ti.getThrowableStrRep()) {
                    textAreaAppender.addMessage(string+"\n");
                }
            }
        }

        @Override
        public boolean requiresLayout() {
            return true;
        }

        @Override
        public void close() {
            SwingUtilities.invokeLater(textAreaAppender);
            textAreaAppender = new TextAreaAppender();
        }
    }

    private class TextAreaAppender implements Runnable {
        private List<String> messages = new ArrayList<String>();

        public void addMessage(String message) {
            messages.add(message);
        }

        @Override
        public void run() {
            if (logCount++ >= MAX_LOG_LINES) {
                String text = logTextArea.getText();
                int split = text.length() / 2;
                split = text.indexOf('\n', split);
                text = text.substring(split);
                logTextArea.setText(text);
                logCount = 0;
            }
            StringBuilder out = new StringBuilder();
            for (String outString : messages) {
                out.append(outString);
            }
            logTextArea.append(out.toString());
            logTextArea.setCaretPosition(logTextArea.getText().length());
        }
    }
}
