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

package eu.europeana.sip.xml;

import eu.delving.sip.FileStoreException;
import eu.delving.sip.ProgressListener;
import eu.europeana.sip.core.GroovyCodeResource;
import eu.europeana.sip.model.SipModel;
import groovy.lang.GroovyClassLoader;
import groovy.xml.MarkupBuilder;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Analyze records to gather some statistics
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class RecordAnalyzer implements Runnable {
    private SipModel sipModel;
    private ProgressAdapter progressAdapter;
    private Listener listener;
    private Object groovyInstance;
    private Method consumeRecordMethod;
    private Method produceHtmlMethod;
    private volatile boolean running = true;

    public interface Listener {
        void finished(String html);
    }

    public RecordAnalyzer(
            SipModel sipModel,
            GroovyCodeResource groovyCodeResource,
            ProgressListener progressListener,
            Listener listener
    ) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        this.sipModel = sipModel;
        this.progressAdapter = new ProgressAdapter(progressListener);
        this.listener = listener;
        setupGroovyClass(groovyCodeResource);
    }

    private void setupGroovyClass(GroovyCodeResource groovyCodeResource) throws NoSuchMethodException, InstantiationException, IllegalAccessException {
        Class groovyClass = new GroovyClassLoader(getClass().getClassLoader()).parseClass(groovyCodeResource.getRecordAnalysisCode());
        this.consumeRecordMethod = groovyClass.getMethod("consumeRecord", Object.class);
        this.produceHtmlMethod = groovyClass.getMethod("produceHtml", Object.class);
        this.groovyInstance = groovyClass.newInstance();
    }

    public void run() {
        try {
            MetadataParser parser = new MetadataParser(
                    sipModel.getDataSetStore().createXmlInputStream(),
                    sipModel.getRecordRoot(),
                    sipModel.getRecordCount(),
                    progressAdapter
            );
            Object record;
            while ((record = parser.nextRecord()) != null && running) {
                consumeRecordMethod.invoke(groovyInstance, record);
            }
        }
        catch (XMLStreamException e) {
            abort();
            sipModel.getUserNotifier().tellUser("XML Problem", e);
        }
        catch (IOException e) {
            abort();
            sipModel.getUserNotifier().tellUser("IO Problem", e);
        }
        catch (FileStoreException e) {
            abort();
            sipModel.getUserNotifier().tellUser("Datastore Problem", e);
        }
        catch (MetadataParser.AbortException e) {
            abort();
            sipModel.getUserNotifier().tellUser("Aborted", e);
        }
        catch (IllegalAccessException e) {
            abort();
            sipModel.getUserNotifier().tellUser("Class Problem", e);
        }
        catch (InvocationTargetException e) {
            abort();
            sipModel.getUserNotifier().tellUser("Class Problem", e);
        }
        finally {
            progressAdapter.finished();
            if (!running) { // aborted, so metadataparser will not call finished()
                listener.finished("<html><h1>Analysis Aborted</h1>");
            }
            else {
                StringWriter html = new StringWriter();
                MarkupBuilder markup = new MarkupBuilder(html);
                try {
                    produceHtmlMethod.invoke(groovyInstance, markup);
                }
                catch (Exception e) {
                    sipModel.getUserNotifier().tellUser("Problem producing HTML", e);
                }
                listener.finished(html.toString());
            }
        }
    }

    private void abort() {
        running = false;
    }

    // just so we receive the cancel signal

    private class ProgressAdapter implements ProgressListener {
        private ProgressListener progressListener;

        private ProgressAdapter(ProgressListener progressListener) {
            this.progressListener = progressListener;
        }

        @Override
        public void setTotal(int total) {
            progressListener.setTotal(total);
        }

        @Override
        public boolean setProgress(int progress) {
            boolean proceed = progressListener.setProgress(progress);
            if (!proceed) {
                running = false;
            }
            return running && proceed;
        }

        @Override
        public void finished() {
            progressListener.finished();
        }
    }
}
