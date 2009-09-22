package eu.europeana.dashboard.client.widgets;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
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
        closeButton.addClickListener(new ClickListener() {
            public void onClick(Widget sender) {
                VerifyDialog.this.hide();
                if (action != null && certainty.isChecked()) {
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
        this.certainty.setChecked(false);
        this.setText(caption);
        contents.setText(question);
        int left = source.getAbsoluteLeft();
        int top = source.getAbsoluteTop();
        this.setPopupPosition(left, top+20);
        this.show();
    }

}
