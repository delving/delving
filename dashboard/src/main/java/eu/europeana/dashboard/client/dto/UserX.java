package eu.europeana.dashboard.client.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.util.Date;

/**
 * Carry the relevant info over a user to the client
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

public class UserX implements IsSerializable {
    private Long id;
    private String userName;
    private String email;
    private String firstName;
    private String lastName;
    private String languages;
    private String projectId;
    private String providerId;
    private boolean newsletter;
    private Date registrationDate;
    private Date lastLogin;
    private RoleX role;
    private boolean enabled;

    public UserX() {
    }

    public UserX(
            Long id,
            String userName,
            String email,
            String firstName,
            String lastName,
            String languages,
            String projectId,
            String providerId,
            boolean newsletter,
            Date registrationDate,
            Date lastLogin,
            RoleX role,
            boolean enabled
    ) {
        this.id = id;
        this.userName = userName;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.languages = languages;
        this.projectId = projectId;
        this.providerId = providerId;
        this.newsletter = newsletter;
        this.registrationDate = registrationDate;
        this.lastLogin = lastLogin;
        this.role = role;
        this.enabled = enabled;
    }

    public Long getId() {
        return id;
    }

    public String getUserName() {
        return userName;
    }

    public String getEmail() {
        return email;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getLanguages() {
        return languages;
    }

    public String getProjectId() {
        return projectId;
    }

    public String getProviderId() {
        return providerId;
    }

    public boolean isLanguageAllowed(String languageCode, boolean defaultYes) {
        boolean blank = languages == null || languages.trim().length() == 0;
        if (blank) {
            return defaultYes;
        }
        return languages.indexOf(languageCode) >= 0;
    }

    public boolean isNewsletter() {
        return newsletter;
    }

    public Date getRegistrationDate() {
        return registrationDate;
    }

    public Date getLastLogin() {
        return lastLogin;
    }

    public RoleX getRole() {
        return role;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
