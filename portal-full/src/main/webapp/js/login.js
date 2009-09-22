/*
 * Copyright 2007 EDL FOUNDATION
 *
 * Licensed under the EUPL, Version 1.1 or as soon they
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

$().ready(function() {
    $("#loginForm").validate({
            rules: {j_username: "required",j_password: "required"},
            messages: {j_username: "",j_password: ""}
     });
    $("#forgotemailForm").validate({
            rules: {email: "required"},
            messages: {email: ""}
     });
    $("#registrationForm").validate({
            rules: {email: "required",iagree: "required"},
            messages: {email: "",iagree: msgRequired }
            //msgRequired is generated in inc_header.ftl and
            // set as a javascript variable (its a spring message
            // so cannot be generated in this js file
     });
});