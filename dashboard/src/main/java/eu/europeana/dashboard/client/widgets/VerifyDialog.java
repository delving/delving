package eu.europeana.dashboard.client.widgets;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.*;
import eu.europeana.dashboard.client.DashboardMessages;

/**
 * are you sure?
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

public class VerifyDialog extends DialogBox {
    private DashboardMessages messages;
    private CheckBox certainty;
    private Button closeButton;
    private HTML contents;
    private Runnable action;

    public VerifyDialog(DashboardMessages messages) {
        this.messages = messages;
        this.setAnimationEnabled(true);
        this.setWidget(createDialogWidget());
        closeButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event)  {
                VerifyDialog.this.hide();
                if (action != null && certainty.getValue()) {
                    action.run();
                }
            }
        });
    }

    protected Widget createDialogWidget() {
        certainty = new CheckBox(messages.yesIAmSure());
        closeButton = new Button(messages.submit());
        VerticalPanel p = new VerticalPanel();
        p.setSpacing(4);
        contents = new HTML();
        p.add(contents);
        p.setCellHorizontalAlignment(contents, HasHorizontalAlignment.ALIGN_CENTER);
        p.add(certainty);
        p.add(closeButton);
        p.setCellHorizontalAlignment(closeButton, HasHorizontalAlignment.ALIGN_RIGHT);
        return p;
    }

    public void ask(Widget source, String caption, String question, Runnable action) {
        this.action = action;
        this.certainty.setValue(false);
        this.setText(caption);
        contents.setText(question);
        int left = source.getAbsoluteLeft();
        int top = source.getAbsoluteTop();
        this.setPopupPosition(left, top+20);
        this.show();
    }

}
