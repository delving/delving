package eu.europeana.sip.gui;

import org.junit.Before;
import org.junit.Test;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Serkan Demirel <serkan@blackbuilt.nl>
 */
public class TestAutoComplete {

    private AutoComplete autoComplete = new AutoCompleteImpl();
    private List<String> original = new ArrayList<String>();

    @Before
    public void setUp() {
        original.add("dcterms_spatial");
        original.add("dc_description");
        original.add("dc_type");
        original.add("europeana_year");
        original.add("europeana_title");
        original.add("europeana_description");
    }

    @Test
    public void testNonExisting() {
        assert 0 == autoComplete.complete("abc", original).size();
    }

    @Test
    public void testExisting() {
        assert 3 == autoComplete.complete("dc", original).size();
        assert 2 == autoComplete.complete("dc_", original).size();
    }
}
