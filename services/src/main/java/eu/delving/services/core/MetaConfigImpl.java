package eu.delving.services.core;

import org.springframework.beans.factory.annotation.Value;

/**
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since Jun 26, 2010 8:55:40 AM
 */
public class MetaConfigImpl implements MetaRepo.MetaConfig {

    @Value("#{launchProperties['services.pmh.repositoryName']}")
    String repositoryName;

    @Value("#{launchProperties['services.pmh.adminEmail']}")
    String adminEmail;

    @Value("#{launchProperties['services.pmh.earliestDateStamp']}")
    String earliestDateStamp;

    @Value("#{launchProperties['services.pmh.repositoryIdentifier']}")
    private String repositoryIdentifier;

    @Value("#{launchProperties['services.pmh.sampleIdentifier']}")
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
