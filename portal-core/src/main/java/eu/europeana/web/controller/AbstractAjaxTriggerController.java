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

package eu.europeana.web.controller;

import javax.servlet.http.HttpServletRequest;

/**
 * Controller for checkbox.
 *
 * @author Borys Omelayenko
 */

public abstract class AbstractAjaxTriggerController extends AbstractAjaxController {
 
	public boolean handleAjax(HttpServletRequest request) throws Exception {
        String className = request.getParameter("className");
        String idString = request.getParameter("id");
        if (className == null || idString == null) {
            throw new IllegalArgumentException("Expected 'className' and 'id' parameters!");
        }
        Long id = Long.valueOf(idString);
        Class clazz = Class.forName("eu.europeana.database.domain."+className);
        String requestedStatus = request.getParameter("status");

        prepareHandling(id, clazz);
        
        if ("true".equals(requestedStatus)) {
            // user wants to check the item
        		return handleCheck(id, clazz);
        }

        if ("false".equals(requestedStatus)) {
            // user wants to uncheck the item
            return handleUnCheck(id, clazz);
        }
        throw new Exception("Frontend error: unknown status " + requestedStatus);
    }

    public abstract boolean handleCheck(Long id, Class clazz) throws Exception;
    public abstract boolean handleUnCheck(Long id, Class clazz) throws Exception;
    public abstract void prepareHandling(Long id, Class clazz) throws Exception;
}