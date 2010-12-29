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

import eu.delving.core.storage.UserRepo;

/**
 * Somebody modified an annotation that a new one depends on
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 * @author Christian Sadilek <christian.sadilek@gmail.com>
 */

public class AnnotationNotOwnedException extends Exception {
	private static final long serialVersionUID = 5592064922743864884L;

	public AnnotationNotOwnedException(Long id, UserRepo.Person user) {
        super("Annotation not owned by user "+user.getEmail()+". ID="+id);
    }
}