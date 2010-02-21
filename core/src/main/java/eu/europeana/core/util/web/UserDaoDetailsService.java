/*
 * Copyright 2007 EDL FOUNDATION
 *
 * Licensed under the EUPL, Version 1.0 or - as soon they
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

package eu.europeana.core.util.web;

import eu.europeana.core.database.UserDao;
import eu.europeana.core.database.domain.Role;
import eu.europeana.core.database.domain.User;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Link spring security with our User dao for authentication
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class UserDaoDetailsService implements UserDetailsService {

    private UserDao userDao;

    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    public interface UserHolder {
        User getUser();

        void setUser(User user);
    }

    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException, DataAccessException {
        try {
            User user = userDao.fetchUserByEmail(email);
            if (user == null) {
                throw new UsernameNotFoundException("Never heard of " + email);
            }
            user.setLastLogin(new Date());
            userDao.updateUser(user);
            return new DaoUserDetails(user);
        }
        catch (Exception e) {
            throw new DataRetrievalFailureException("UserDao problem", e);
        }
    }

    private static class DaoUserDetails implements UserDetails, UserHolder {
        private static final long serialVersionUID = 1581860745489819018L;
        private User user;
        private List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();

        private DaoUserDetails(User user) {
            this.user = user;
            switch (user.getRole()) {
                case ROLE_GOD:
                    addRole(Role.ROLE_USER);
                    addRole(Role.ROLE_TRANSLATOR);
                    addRole(Role.ROLE_EDITOR);
                    addRole(Role.ROLE_ADMINISTRATOR);
                    addRole(Role.ROLE_GOD);
                    break;
                case ROLE_TRANSLATOR:
                    addRole(Role.ROLE_USER);
                    addRole(Role.ROLE_TRANSLATOR);
                    break;
                case ROLE_ADMINISTRATOR:
                    addRole(Role.ROLE_USER);
                    addRole(Role.ROLE_ADMINISTRATOR);
                    break;
                case ROLE_EDITOR:
                    addRole(Role.ROLE_USER);
                    addRole(Role.ROLE_EDITOR);
                    addRole(Role.ROLE_PACTA);
                    addRole(Role.ROLE_CARROUSEL);
                    break;
                case ROLE_PACTA:
                    addRole(Role.ROLE_USER);
                    addRole(Role.ROLE_PACTA);
                    break;
                case ROLE_CARROUSEL:
                    addRole(Role.ROLE_USER);
                    addRole(Role.ROLE_CARROUSEL);
                    break;
                case ROLE_CONTENT_TESTER:
                    addRole(Role.ROLE_USER);
                    addRole(Role.ROLE_CONTENT_TESTER);
                case ROLE_USER:
                    addRole(Role.ROLE_USER);
                    break;
                default:
                    throw new IllegalStateException("switch statment must be expanded to include: " + user.getRole());
            }
        }

        private void addRole(Role role) {
            authorities.add(new DaoGrantedAuthority(role));
        }

        public User getUser() {
            return user;
        }

        public void setUser(User user) {
            this.user = user;
        }

        public Collection<GrantedAuthority> getAuthorities() {
            return authorities;
        }

        public String getPassword() {
            return user.getHashedPassword();
        }

        public String getUsername() {
            return user.getEmail();
        }

        public boolean isAccountNonExpired() {
            return true;
        }

        public boolean isAccountNonLocked() {
            return true;
        }

        public boolean isCredentialsNonExpired() {
            return true;
        }

        public boolean isEnabled() {
            return user.isEnabled();
        }

        public String toString() {
            return "User: " + user.getFirstName() + " " + user.getLastName();
        }
    }

    private static class DaoGrantedAuthority implements GrantedAuthority, Comparable<DaoGrantedAuthority> {
        private static final long serialVersionUID = -534970263836323349L;
        private Role role;

        private DaoGrantedAuthority(Role role) {
            this.role = role;
        }

        public String getAuthority() {
            return role.toString();
        }

        @Override
        public int compareTo(DaoGrantedAuthority daoGrantedAuthority) {
            return role.compareTo(daoGrantedAuthority.role);
        }
    }
}