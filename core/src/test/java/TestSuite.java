import eu.delving.core.storage.TestStaticRepo;
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
        TestStaticRepo.class,
        TestUserRepo.class
})
public class TestSuite {
}
