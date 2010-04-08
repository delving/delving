package eu.europeana.sip.gui;

import java.awt.event.KeyEvent;
import java.util.List;

/**
 * @author Serkan Demirel <serkan@blackbuilt.nl>
 */
public interface AutoComplete {

    /**
     * Start completion after DEFAULT_OFFSET count of characters
     */
    final static int DEFAULT_OFFSET = 0;

    /**
     * Use String after DEFAULT_PREFIX
     */
    final static String DEFAULT_PREFIX = "input.";

    /**
     * Filtering the original list on .startsWith(entered) and returning the remainings.
     *
     * @param entered          String provided by the user
     * @param originalElements A list with the original elements
     * @return A list with the remaining elements after filtering on entered String
     */
    public List<String> complete(String entered, List<String> originalElements);

    /**
     * Filtering the original list on .startsWith(entered) and returning the remainings.
     *
     * @param entered          Key entered by the user
     * @param originalElements A list with the original elements
     * @return A list with the remaining elements after filtering on entered String
     */
    public List<String> complete(KeyEvent entered, List<String> originalElements);
}           
