/*
 * Copyright 2007 EDL FOUNDATION
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they
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
package eu.delving.core.storage.annotation;

/**
 * Somebody modified an annotation that a new one depends on
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 * @author Christian Sadilek <christian.sadilek@gmail.com>
 */

public class UserNotFoundException extends Exception {
	private static final long serialVersionUID = 7993738854683850975L;

	public UserNotFoundException() {
        super("User not found in session");
    }

    public UserNotFoundException(Long id) {
        super("User not found. ID="+id);
    }
}