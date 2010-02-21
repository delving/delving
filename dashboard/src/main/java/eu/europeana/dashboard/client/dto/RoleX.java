package eu.europeana.dashboard.client.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * This is intended to perfectly mirror eu.europeana.core.database.domain.Role
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

public enum RoleX implements IsSerializable {
    ROLE_USER,  // general portal user, no dashboard access
    ROLE_CONTENT_TESTER, // a user of the special sandbox version of the dashboard
    ROLE_TRANSLATOR, // translation tab only
    ROLE_EDITOR, // translation + carousel + proposed search terms + partner Tab
    ROLE_PACTA, // only pacta editor
    ROLE_CARROUSEL, // only carrousel editory
    ROLE_ADMINISTRATOR, //   all rights, except record editor
    ROLE_GOD  // all rights
}