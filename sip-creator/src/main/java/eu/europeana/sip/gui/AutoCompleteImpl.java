package eu.europeana.sip.gui;

import org.apache.log4j.Logger;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of the AutoComplete interface.
 *
 * @author Serkan Demirel <serkan@blackbuilt.nl>
 */
public class AutoCompleteImpl implements AutoComplete {

    private final Logger LOG = Logger.getLogger(AutoCompleteImpl.class);
    private final StringBuffer keyBuffer = new StringBuffer();

    private String prefix;
    private int offSet;

    public AutoCompleteImpl() {
        this.offSet = DEFAULT_OFFSET;
        this.prefix = DEFAULT_PREFIX;
    }

    @Override
    public List<String> complete(String entered, List<String> originalElements) {
        if (!entered.startsWith(prefix)) {
            return originalElements;
        }
        entered = entered.substring(entered.lastIndexOf(".") + 1); // todo: index delimiter not the .
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
        switch (entered.getKeyCode()) {
            case KeyEvent.VK_ESCAPE:
                keyBuffer.setLength(0);
                break;
            case KeyEvent.VK_SHIFT:

                break;
            case KeyEvent.VK_BACK_SPACE:
                if (keyBuffer.length() <= 0) {
                    break;
                }
                keyBuffer.setLength(keyBuffer.length() - 1);
                break;
            default:
                keyBuffer.append(entered.getKeyChar());
        }
        remainingElements = complete(keyBuffer.toString(), originalElements);
        return remainingElements;
    }
}
