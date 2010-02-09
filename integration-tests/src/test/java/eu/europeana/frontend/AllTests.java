package eu.europeana.frontend;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	StartServer.class,
	CarouselTest.class,
	MyEuropeanaTest.class,
	RegistrationTest.class,
	SearchTest.class,
	TokenBasedAuthenticationTest.class,
	ForgotPasswordTest.class,
	StopServer.class
})
/**
 * Runner of all Europeana Frontend tests.
 * 
 * @author Borys Omelayenko
 */
public class AllTests {

	// why on earth I need this class, I have no idea! 
	
}
