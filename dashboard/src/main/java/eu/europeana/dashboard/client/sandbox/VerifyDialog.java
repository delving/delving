package eu.europeana.dashboard.client.sandbox;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.*;
import eu.europeana.dashboard.client.DashboardWidget;

/**
 * Ask to make sure they mean it.
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

public class VerifyDialog extends DialogBox {
    private DashboardWidget source;
    private String question;
    private CheckBox yesCheckBox;
    private CheckBox noCheckBox;
    private Button closeButton;
    private Runnable action;

    public VerifyDialog(DashboardWidget source, String caption, String question) {
        this.yesCheckBox = new CheckBox(source.getWorld().messages().yesIAmSure());
        this.noCheckBox = new CheckBox(source.getWorld().messages().noIAmSure());
        this.closeButton = new Button(source.getWorld().messages().submit());
        this.source = source;
        this.question = question;
        this.setAnimationEnabled(true);
        this.setText(caption);
        this.setWidget(createDialogWidget());
        closeButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event)  {
                VerifyDialog.this.hide();
                if (action != null && yesCheckBox.getValue()) {
                    action.run();
                }
            }
        });
    }

    private Widget createDialogWidget() {
        VerticalPanel p = new VerticalPanel();
        p.setSpacing(4);
        HTML contents = new HTML(question);
        p.add(contents);
        p.setCellHorizontalAlignment(contents, HasHorizontalAlignment.ALIGN_CENTER);
        p.add(yesCheckBox);
        p.add(noCheckBox);
        p.add(closeButton);
        p.setCellHorizontalAlignment(closeButton, HasHorizontalAlignment.ALIGN_RIGHT);
        return p;
    }

    public void ask(Runnable action) {
        this.action = action;
        this.yesCheckBox.setValue(false);
        int left = source.getWidget().getAbsoluteLeft();
        int top = source.getWidget().getAbsoluteTop();
        this.setPopupPosition(left, top+20);
        this.show();
    }
}
