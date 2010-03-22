package eu.europeana.sip.io;

import java.io.File;
import java.io.IOException;

/**
 * Saving and reading Groovy snippets
 *
 * @author Serkan Demirel <serkan@blackbuilt.nl>
 */
public interface GroovyPersistor {

    /**
     * Save the groovy snippet to a file
     *
     * @param file          Save to this file
     * @param groovySnippet Save this snippet
     * @throws IOException Something went wrong during saving
     */
    public void save(File file, StringBuffer groovySnippet) throws IOException;

    /**
     * Save the groovy snippet to a file
     *
     * @param groovySnippet Save this snippet
     * @throws IOException Something went wrong during saving
     */
    public void save(StringBuffer groovySnippet) throws IOException;

    /**
     * Read the snippet from the specified file
     *
     * @param file The specified file
     * @return The queried Groovy snippet
     * @throws IOException Something went wrong during reading
     */
    public String read(File file) throws IOException;

    /**
     * Read a snipped
     *
     * @return The queried Groovy snippet
     * @throws IOException Something went wrong during reading
     */
    public String read() throws IOException;
}
    