/*
 * Copyright 2007 EDL FOUNDATION
 *
 * Licensed under the EUPL, Version 1.1 or as soon they
 * will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * you may not use this work except in compliance with the
 * Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the Licence is
 * distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the Licence for the specific language governing
 * permissions and limitations under the Licence.
 */

package eu.europeana.translation;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import eu.europeana.database.StaticInfoDao;
import eu.europeana.database.domain.Contributor;
import eu.europeana.database.domain.Country;
import eu.europeana.database.domain.Partner;
import eu.europeana.database.domain.PartnerSector;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * move the existing message bundle to the database
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

@Deprecated
public class ContributorFileLoader {
    private static final Logger LOG = Logger.getLogger(ContributorFileLoader.class);

    public static void main(String[] args) throws IOException {
        fileLoader();
        if (args.length == 0 || !args[0].equalsIgnoreCase("true")) {
            System.exit(0);
        }
    }

    public static void fileLoader() throws UnsupportedEncodingException, FileNotFoundException {
        ApplicationContext context = new ClassPathXmlApplicationContext(new String[]{
                "/database-application-context.xml",
                "/application-context.xml"
        });
        StaticInfoDao staticInfoDao = (StaticInfoDao) context.getBean("staticInfoDao");
        String inputFile = "portal/src/test/resources/Europeana_source_ver2-8_Relaunch20090317.xml";
        BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile), "UTF-8"));
        XStream stream = new XStream(new DomDriver());
        stream.processAnnotations(Providers.class);
        Providers providers = (Providers) stream.fromXML(in);
        List<Providers.Provider> list = providers.getProviders();
        for (Providers.Provider provider : list) {
            Contributor contributor = new Contributor();
            contributor.setProviderId(provider.getId());
            contributor.setOriginalName(provider.getOriginal_name());
            contributor.setEnglishName(provider.getEnglish_name());
            contributor.setCountry(Country.get(provider.getCountry()));
            contributor.setUrl(provider.getUrl());
            contributor.setNumberOfPartners(provider.getPartners());
            staticInfoDao.saveContributor(contributor);
            LOG.info("saving contributor: " + provider.getOriginal_name());
        }
        LOG.info("Finished Loading Providers.");

        LOG.info("Start Loading Partners.");
        inputFile = "portal/src/test/resources/partners.xml";
        in = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile), "UTF-8"));
        providers = (Providers) stream.fromXML(in);
        list = providers.getProviders();
        for (Providers.Provider provider : list) {
            Partner partner = new Partner();
            partner.setName(provider.getOriginal_name());
            partner.setUrl(provider.getUrl());
            partner.setSector(PartnerSector.get(provider.getCountry().toString()));
            staticInfoDao.savePartner(partner);
            LOG.info("saving partner: " + partner.getName());
        }
    }

}