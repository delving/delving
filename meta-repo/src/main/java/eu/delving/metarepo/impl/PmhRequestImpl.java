package eu.delving.metarepo.impl;

import eu.delving.metarepo.core.MetaRepo;

/**
* @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
* @since Jun 20, 2010 10:16:34 PM
*/

public class PmhRequestImpl implements MetaRepo.PmhRequest {

    private MetaRepo.PmhVerb verb;
    private String set;
    private String from;
    private String until;
    private String metadataPrefix;
    private String identifier;

    public PmhRequestImpl(MetaRepo.PmhVerb verb, String set, String from, String until, String metadataPrefix, String identifier) {
        this.verb = verb;
        this.set = set;
        this.from = from;
        this.until = until;
        this.metadataPrefix = metadataPrefix;
        this.identifier = identifier;
    }

    public MetaRepo.PmhVerb getVerb() {
        return verb;
    }

    public String getSet() {
        return set;
    }

    public String getFrom() {
        return from;
    }

    public String getUntil() {
        return until;
    }

    public String getMetadataPrefix() {
        return metadataPrefix;
    }

    public String getIdentifier() {
        return identifier;
    }
}
