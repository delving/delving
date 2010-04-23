package eu.europeana.sip.gui;

import org.apache.log4j.Logger;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Implementation of the AutoComplete interface.
 *
 * @author Serkan Demirel <serkan@blackbuilt.nl>
 */
public class AutoCompleteImpl implements AutoComplete, AutoCompleteDialog.Listener {

    private static final Logger LOG = Logger.getLogger(AutoCompleteImpl.class);
    private static final String VALIDATION_REGEX = "[a-zA-Z_0-9\\.]+";
    private final StringBuffer keyBuffer = new StringBuffer();

    private String prefix;
    private int offSet;

    @Override
    public void itemSelected(Object selectedItem) {
        keyBuffer.setLength(0);
        LOG.debug("Item selected and keyBuffer emptied ; " + selectedItem);
    }

    public AutoCompleteImpl() {
        this.offSet = DEFAULT_OFFSET;
        this.prefix = DEFAULT_PREFIX;
    }

    private boolean validate(KeyEvent entered) {
        Pattern pattern = Pattern.compile(VALIDATION_REGEX);
        return pattern.matcher("" + entered.getKeyChar()).find();
    }

    @Override
    public List<String> complete(String entered, List<String> originalElements) {
        if (!entered.endsWith(prefix)) { // todo: do this check in advance?
            return null;
        }
        if (null == originalElements) {
            LOG.error("originalElements is null");
            return null;
        }
        entered = entered.substring(entered.lastIndexOf(DEFAULT_PREFIX) + DEFAULT_PREFIX.length());
        List<String> remaining = new ArrayList<String>();
        for (String inList : originalElements) {
            if (inList.startsWith(entered)) {
                remaining.add(inList);
            }
        }
        return remaining;
    }

    @Override
    public List<String> complete(KeyEvent entered, List<String> originalElements) {
        if (validate(entered)) {
            keyBuffer.append(entered.getKeyChar());
        }
        return complete(keyBuffer.toString(), originalElements);
    }

    @Override
    public void cleared() {
        keyBuffer.setLength(0);
    }

    @Override
    public void cancelled() {
        keyBuffer.setLength(0);
    }
}
