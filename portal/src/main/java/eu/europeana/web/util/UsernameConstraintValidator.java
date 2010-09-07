/*
 * Copyright 2007 EDL FOUNDATION
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

package eu.europeana.web.util;

import eu.europeana.core.database.UserDao;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Serve the NonexistentUser
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

public class UsernameConstraintValidator implements ConstraintValidator<UsernameConstraint, String> {

    @Autowired
    private UserDao userDao;

    @Override
    public void initialize(UsernameConstraint usernameConstraint) {
    }

    @Override
    public boolean isValid(String userName, ConstraintValidatorContext context) {
        for (int i = 0; i < userName.length(); i++) {
            char c = userName.charAt(i);
            if ( ! (Character.isLetterOrDigit(c) || c == ' ' || c == '_')) {
                context.buildConstraintViolationWithTemplate("illegal.characters").addConstraintViolation();
                return false;
            }
        }
        if (userDao.userNameExists(userName)) {
            context.buildConstraintViolationWithTemplate("exists").addConstraintViolation();
            return false;
        }
        return true;
    }
}
