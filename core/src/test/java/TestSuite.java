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
import eu.delving.core.storage.TestStaticRepo;
import eu.delving.core.storage.TestTokenRepo;
import eu.delving.core.storage.TestUserRepo;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Gather tests together
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

@RunWith(Suite.class)
@Suite.SuiteClasses({
        TestTokenRepo.class,
        TestStaticRepo.class,
        TestUserRepo.class
})
public class TestSuite {
}
