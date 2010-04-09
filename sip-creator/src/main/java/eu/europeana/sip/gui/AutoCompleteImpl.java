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

    private final Logger LOG = Logger.getLogger(AutoCompleteImpl.class);
    private final StringBuffer KEY_BUFFER = new StringBuffer();
    private final String VALIDATION_REGEX = "[a-zA-Z_0-9\\.]+";

    private String prefix;
    private int offSet;
    private Listener listener;

    @Override
    public void itemSelected(Object selectedItem) {
        KEY_BUFFER.setLength(0);
        LOG.debug("Item selected and KEY_BUFFER emptied ; " + selectedItem);
    }

    interface Listener {
        /**
         * Autocompletion cancelled by user e.g. ESCAPE
         */
        public void cancelled();
    }

    public AutoCompleteImpl(Listener listener) {
        this.listener = listener;
        this.offSet = DEFAULT_OFFSET;
        this.prefix = DEFAULT_PREFIX;
    }

    private boolean validate(KeyEvent entered) {
        Pattern pattern = Pattern.compile(VALIDATION_REGEX);
        return pattern.matcher("" + entered.getKeyChar()).find();
    }

    @Override
    public List<String> complete(String entered, List<String> originalElements) {
        if (!entered.startsWith(prefix)) { // todo: do this check in advance?
            return originalElements;
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
        List<String> remainingElements;
        if (validate(entered)) {
            KEY_BUFFER.append(entered.getKeyChar());
        }
        switch (entered.getKeyCode()) {
            case KeyEvent.VK_ENTER: // todo: enter should select item in list
            case KeyEvent.VK_ESCAPE:
                KEY_BUFFER.setLength(0);
                LOG.debug("KEY_BUFFER emptied");
                listener.cancelled();
                break;
            case KeyEvent.VK_BACK_SPACE:
                if (KEY_BUFFER.length() <= 0) {
                    break;
                }
                KEY_BUFFER.setLength(KEY_BUFFER.length() - 1);
        }
        remainingElements = complete(KEY_BUFFER.toString(), originalElements);
        return remainingElements;
    }
}
