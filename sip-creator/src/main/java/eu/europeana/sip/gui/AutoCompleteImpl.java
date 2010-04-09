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
    private Listener listener;

    @Override
    public void itemSelected(Object selectedItem) {
        keyBuffer.setLength(0);
        LOG.debug("Item selected and keyBuffer emptied ; " + selectedItem);
    }

    interface Listener {
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
        switch (entered.getKeyCode()) {
            case KeyEvent.VK_ENTER:
            case KeyEvent.VK_ESCAPE:
                keyBuffer.setLength(0);
                listener.cancelled();
                return null;
            case KeyEvent.VK_BACK_SPACE:
                if (keyBuffer.length() <= 0) {
                    listener.cancelled();
                    return null;
                }
                keyBuffer.setLength(keyBuffer.length() - 1);
        }
        return complete(keyBuffer.toString(), originalElements);
    }
}
