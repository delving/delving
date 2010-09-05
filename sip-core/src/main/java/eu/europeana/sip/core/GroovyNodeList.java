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

package eu.europeana.sip.core;

import groovy.lang.DelegatingMetaClass;
import groovy.lang.GroovySystem;
import groovy.lang.MetaClass;
import groovy.xml.QName;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * A simpler adaptation of groovy.util.NodeList serving GroovyNode
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class GroovyNodeList extends ArrayList<Object> {
    static {
        // wrap the standard MetaClass with the delegate
        setMetaClass(GroovyNodeList.class, GroovySystem.getMetaClassRegistry().getMetaClass(GroovyNodeList.class));
    }

    public GroovyNodeList() {
    }

    public GroovyNodeList(Collection<Object> collection) {
        super(collection);
    }

    public GroovyNodeList(int size) {
        super(size);
    }

    protected static void setMetaClass(final Class nodelistClass, final MetaClass metaClass) {
        final MetaClass newMetaClass = new DelegatingMetaClass(metaClass) {
            @Override
            public Object getAttribute(final Object object, final String attribute) {
                GroovyNodeList nl = (GroovyNodeList) object;
                Iterator it = nl.iterator();
                List<Object> result = new ArrayList<Object>();
                while (it.hasNext()) {
                    GroovyNode node = (GroovyNode) it.next();
                    result.add(node.attributes().get(attribute));
                }
                return result;
            }

            @Override
            public void setAttribute(final Object object, final String attribute, final Object newValue) {
                GroovyNodeList nl = (GroovyNodeList) object;
                for (Object aNl : nl) {
                    GroovyNode node = (GroovyNode) aNl;
                    node.attributes().put(attribute, (String) newValue);
                }
            }

            @Override
            public Object getProperty(Object object, String property) {
                if (object instanceof GroovyNodeList) {
                    GroovyNodeList nl = (GroovyNodeList) object;
                    return nl.getAt(property);
                }
                return super.getProperty(object, property);
            }
        };
        GroovySystem.getMetaClassRegistry().setMetaClass(nodelistClass, newMetaClass);
    }

    /**
     * Provides lookup of elements by non-namespaced name.
     *
     * @param name the name or shortcut key for nodes of interest
     * @return the nodes of interest which match name
     */
    public GroovyNodeList getAt(String name) {
        GroovyNodeList answer = new GroovyNodeList();
        for (Iterator iter = iterator(); iter.hasNext();) {
            Object child = iter.next();
            if (child instanceof GroovyNode) {
                GroovyNode childNode = (GroovyNode) child;
                Object temp = childNode.get(name);
                if (temp instanceof Collection) {
                    answer.addAll((Collection<Object>)temp);
                }
                else {
                    answer.add(temp);
                }
            }
        }
        return answer;
    }

    /**
     * Provides lookup of elements by QName.
     *
     * @param name the name or shortcut key for nodes of interest
     * @return the nodes of interest which match name
     */
    public GroovyNodeList getAt(QName name) {
        GroovyNodeList answer = new GroovyNodeList();
        for (Iterator iter = iterator(); iter.hasNext();) {
            Object child = iter.next();
            if (child instanceof GroovyNode) {
                GroovyNode childNode = (GroovyNode) child;
                GroovyNodeList temp = childNode.getAt(name);
                answer.addAll(temp);
            }
        }
        return answer;
    }

    /**
     * Returns the text value of all of the elements in the collection.
     *
     * @return the text value of all the elements in the collection or null
     */
    public String text() {
        String previousText = null;
        StringBuffer buffer = null;
        for (Iterator iter = this.iterator(); iter.hasNext();) {
            Object child = iter.next();
            String text = null;
            if (child instanceof String) {
                text = (String) child;
            }
            else if (child instanceof GroovyNode) {
                text = ((GroovyNode) child).text();
            }
            if (text != null) {
                if (previousText == null) {
                    previousText = text;
                }
                else {
                    if (buffer == null) {
                        buffer = new StringBuffer();
                        buffer.append(previousText);
                    }
                    buffer.append(text);
                }
            }
        }
        if (buffer != null) {
            return buffer.toString();
        }
        if (previousText != null) {
            return previousText;
        }
        return "";
    }
}
