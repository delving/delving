/*
 * Copyright 2010 DELVING BV
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

package eu.delving.sip;

import javax.swing.Action;
import javax.swing.ProgressMonitor;
import javax.swing.SwingUtilities;

/**
 * Ties a process to a ProgressMonitor
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public interface ProgressListener {
    long PATIENCE = 250;

    void setTotal(int total);

    boolean setProgress(int progress);

    void finished();

    public class Adapter implements ProgressListener {
        private long lastProgress;
        private ProgressMonitor progressMonitor;
        private Action launchAction;
        private Runnable finishedRunnable;

        public Adapter(ProgressMonitor progressMonitor) {
            this(progressMonitor, (Action) null);
        }

        public Adapter(ProgressMonitor progressMonitor, Action launchAction) {
            this.progressMonitor = progressMonitor;
            this.launchAction = launchAction;
            if (launchAction != null) {
                launchAction.setEnabled(false);
            }
        }

        public Adapter(ProgressMonitor progressMonitor, Runnable finishedRunnable) {
            this.progressMonitor = progressMonitor;
            this.finishedRunnable = finishedRunnable;
        }

        @Override
        public void setTotal(final int total) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    progressMonitor.setMaximum(total);
                }
            });
        }

        @Override
        public boolean setProgress(final int progress) {
            if (System.currentTimeMillis() > lastProgress + PATIENCE) { // not too many events
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        progressMonitor.setProgress(progress);
                    }
                });
                lastProgress = System.currentTimeMillis();
            }
            boolean cancelled = progressMonitor.isCanceled();
            if (cancelled) {
                if (launchAction != null) {
                    launchAction.setEnabled(true);
                }
            }
            return !cancelled;
        }

        @Override
        public void finished() {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    progressMonitor.close();
                    if (launchAction != null) {
                        launchAction.setEnabled(true);
                    }
                    if (finishedRunnable != null) {
                        finishedRunnable.run();
                    }
                }
            });
        }
    }

}
