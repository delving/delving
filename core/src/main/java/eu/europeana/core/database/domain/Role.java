package eu.europeana.core.database.domain;

/**
 * Enumerates the roles
 */

public enum Role {
    ROLE_USER,  // general portal user, no dashboard access
    ROLE_RESEARCH_USER, // to be used for "museometrie"
    ROLE_ADMINISTRATOR, //   all rights, except record editor
    ROLE_GOD  // all rights
}
