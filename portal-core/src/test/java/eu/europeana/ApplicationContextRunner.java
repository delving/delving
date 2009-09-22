package eu.europeana;

import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class ApplicationContextRunner {
    public static void main(String[] args) throws Exception {
        new ClassPathXmlApplicationContext(new String [] {
                "/database-application-context.xml",
                "/core-application-context.xml"
        });
    }
}