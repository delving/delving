/*
 * Copyright 2007 EDL FOUNDATION
 *
 *  Licensed under the EUPL, Version 1.0 or as soon they
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

package eu.europeana.web.util;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.*;
import java.util.Locale;
import java.util.Map;

/**
 * Some utility stuff to help testing with freemarker
 */
public class FreemarkerUtil {

    public static String processResource(String templateName, Map model) throws IOException, TemplateException {
        StringWriter out = new StringWriter();
        getResourceTemplate(templateName).process(model,out);
        return out.toString();
    }

    public static String processWebInf(String templateName, Map model) throws IOException, TemplateException {
        StringWriter out = new StringWriter();
        getWebInfTemplate(templateName).process(model,out);
        return out.toString();
    }

    private static Template getResourceTemplate(String fileName) throws IOException {
        return getTemplate(fileName, new InputStreamReader(FreemarkerUtil.class.getResourceAsStream(fileName)));
    }

    public static Template getWebInfTemplate(String fileName) throws IOException {
        return getTemplate(fileName, new FileReader("./portal-full/src/main/webapp/WEB-INF/templates/"+fileName));
    }

    private static Template getTemplate(String name, Reader reader) throws IOException {
        Configuration configuration = new Configuration();
        configuration.setLocale(new Locale("nl"));
        configuration.setObjectWrapper(new DefaultObjectWrapper());
        return new Template(name,reader,configuration);
    }
}
