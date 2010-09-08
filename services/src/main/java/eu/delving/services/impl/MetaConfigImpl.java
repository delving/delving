package eu.delving.services.impl;

import eu.delving.services.core.MetaRepo;
import org.springframework.beans.factory.annotation.Value;

/**
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since Jun 26, 2010 8:55:40 AM
 */
public class MetaConfigImpl implements MetaRepo.MetaConfig {

    @Value("#{metaRepoProperties['repositoryName']}")  
    String repositoryName;

    @Value("#{metaRepoProperties['adminEmail']}")
    String adminEmail;

    @Value("#{metaRepoProperties['earliestDateStamp']}")
    String earliestDateStamp;

    @Value("#{metaRepoProperties['repositoryIdentifier']}")
    private String repositoryIdentifier;

    @Value("#{metaRepoProperties['sampleIdentifier']}")
    private String sampleIdentifier;

    @Override
    public String getRepositoryName() {
        return repositoryName;
    }

    @Override
    public String getAdminEmail() {
        return adminEmail;
    }

    @Override
    public String getEarliestDateStamp() {
        return earliestDateStamp;
    }

    @Override
    public String getRepositoryIdentifier() {
        return repositoryIdentifier;
    }

    @Override
    public String getSampleIdentifier() {
        return sampleIdentifier;
    }
}
