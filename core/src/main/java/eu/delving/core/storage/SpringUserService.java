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

package eu.delving.core.storage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Link spring security with our UserRepo implementation
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class SpringUserService implements UserDetailsService {

    @Autowired
    private UserRepo userRepo;

    public interface UserHolder {
        User getUser();
    }

    @Override
    public org.springframework.security.core.userdetails.UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        try {
            User user = userRepo.byEmail(email);
            if (user == null) {
                throw new UsernameNotFoundException("Never heard of " + email);
            }
            user.setLastLogin(new Date());
            user.save();
            return new UserDetails(user);
        }
        catch (Exception e) {
            throw new UsernameNotFoundException("Persistence problem", e);
        }
    }

    private static class UserDetails implements org.springframework.security.core.userdetails.UserDetails, UserHolder {
        private static final long serialVersionUID = 1581860745489819018L;
        private User user;
        private List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();

        private UserDetails(User user) {
            this.user = user;
            switch (user.getRole()) {
                case ROLE_GOD:
                    addRole(User.Role.ROLE_USER);
                    addRole(User.Role.ROLE_ADMINISTRATOR);
                    addRole(User.Role.ROLE_RESEARCH_USER);
                    addRole(User.Role.ROLE_GOD);
                    break;
                case ROLE_ADMINISTRATOR:
                    addRole(User.Role.ROLE_USER);
                    addRole(User.Role.ROLE_RESEARCH_USER);
                    addRole(User.Role.ROLE_ADMINISTRATOR);
                    break;
                case ROLE_RESEARCH_USER:
                    addRole(User.Role.ROLE_USER);
                    addRole(User.Role.ROLE_RESEARCH_USER);
                    break;
                case ROLE_USER:
                    addRole(User.Role.ROLE_USER);
                    break;
                default:
                    throw new IllegalStateException("switch statment must be expanded to include: " + user.getRole());
            }
        }

        private void addRole(User.Role role) {
            authorities.add(new PersonAuthority(role));
        }

        @Override
        public User getUser() {
            return user;
        }

        @Override
        public Collection<GrantedAuthority> getAuthorities() {
            return authorities;
        }

        @Override
        public String getPassword() {
            return user.getHashedPassword();
        }

        @Override
        public String getUsername() {
            return user.getEmail();
        }

        @Override
        public boolean isAccountNonExpired() {
            return true;
        }

        @Override
        public boolean isAccountNonLocked() {
            return true;
        }

        @Override
        public boolean isCredentialsNonExpired() {
            return true;
        }

        @Override
        public boolean isEnabled() {
            return user.isEnabled();
        }

        public String toString() {
            return "User: " + user.getFirstName() + " " + user.getLastName();
        }
    }

    private static class PersonAuthority implements GrantedAuthority, Comparable<PersonAuthority> {
        private static final long serialVersionUID = -534970263836323349L;
        private User.Role role;

        private PersonAuthority(User.Role role) {
            this.role = role;
        }

        @Override
        public String getAuthority() {
            return role.toString();
        }

        @Override
        public int compareTo(PersonAuthority personAuthority) {
            return role.compareTo(personAuthority.role);
        }
    }
}