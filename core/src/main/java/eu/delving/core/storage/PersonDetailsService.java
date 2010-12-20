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
 * Link spring security with our PersonStorage implementation
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class PersonDetailsService implements UserDetailsService {

    private PersonStorage personStorage;

    public void setPersonStorage(PersonStorage personStorage) {
        this.personStorage = personStorage;
    }

    public interface PersonHolder {
        PersonStorage.Person getPerson();
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException, DataAccessException {
        try {
            PersonStorage.Person person = personStorage.byEmail(email);
            if (person == null) {
                throw new UsernameNotFoundException("Never heard of " + email);
            }
            person.setLastLogin(new Date());
            person.save();
            return new DaoUserDetails(person);
        }
        catch (Exception e) {
            throw new DataRetrievalFailureException("UserDao problem", e);
        }
    }

    private static class DaoUserDetails implements UserDetails, PersonHolder {
        private static final long serialVersionUID = 1581860745489819018L;
        private PersonStorage.Person person;
        private List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();

        private DaoUserDetails(PersonStorage.Person person) {
            this.person = person;
            switch (person.getRole()) {
                case ROLE_GOD:
                    addRole(PersonStorage.Role.ROLE_USER);
                    addRole(PersonStorage.Role.ROLE_ADMINISTRATOR);
                    addRole(PersonStorage.Role.ROLE_RESEARCH_USER);
                    addRole(PersonStorage.Role.ROLE_GOD);
                    break;
                case ROLE_ADMINISTRATOR:
                    addRole(PersonStorage.Role.ROLE_USER);
                    addRole(PersonStorage.Role.ROLE_RESEARCH_USER);
                    addRole(PersonStorage.Role.ROLE_ADMINISTRATOR);
                    break;
                case ROLE_RESEARCH_USER:
                    addRole(PersonStorage.Role.ROLE_USER);
                    addRole(PersonStorage.Role.ROLE_RESEARCH_USER);
                    break;
                case ROLE_USER:
                    addRole(PersonStorage.Role.ROLE_USER);
                    break;
                default:
                    throw new IllegalStateException("switch statment must be expanded to include: " + person.getRole());
            }
        }

        private void addRole(PersonStorage.Role role) {
            authorities.add(new DaoGrantedAuthority(role));
        }

        @Override
        public PersonStorage.Person getPerson() {
            return person;
        }

        @Override
        public Collection<GrantedAuthority> getAuthorities() {
            return authorities;
        }

        @Override
        public String getPassword() {
            return person.getHashedPassword();
        }

        @Override
        public String getUsername() {
            return person.getEmail();
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
            return person.isEnabled();
        }

        public String toString() {
            return "Person: " + person.getFirstName() + " " + person.getLastName();
        }
    }

    private static class DaoGrantedAuthority implements GrantedAuthority, Comparable<DaoGrantedAuthority> {
        private static final long serialVersionUID = -534970263836323349L;
        private PersonStorage.Role role;

        private DaoGrantedAuthority(PersonStorage.Role role) {
            this.role = role;
        }

        @Override
        public String getAuthority() {
            return role.toString();
        }

        @Override
        public int compareTo(DaoGrantedAuthority daoGrantedAuthority) {
            return role.compareTo(daoGrantedAuthority.role);
        }
    }
}