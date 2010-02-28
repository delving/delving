package eu.europeana.dashboard.client.sandbox;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import eu.europeana.dashboard.client.DashboardWidget;
import eu.europeana.dashboard.client.dto.ImportFileX;

/**
 * a widget for uploading files
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

public class FileUploadWidget extends DashboardWidget {

    private FileUpload fileUpload = new FileUpload();
    private Button submitButton;
    private Status notifier;

    public FileUploadWidget(World world) {
        super(world);
    }

    public interface Status {
        void uploadStarted(String fileName);
        void uploadEnded();
    }

    public void setNotifier(Status onSubmitComplete) {
        this.notifier = onSubmitComplete;
    }

    @Override
    protected Widget createWidget() {
        fileUpload.setName("uploadFile");
        fileUpload.setWidth("300");
        final FormPanel form = new FormPanel();
        form.setAction(GWT.getModuleBaseURL() + "uploadSandbox");
        form.setEncoding(FormPanel.ENCODING_MULTIPART);
        form.setMethod(FormPanel.METHOD_POST);
        form.addSubmitHandler(new FormPanel.SubmitHandler() {
            @Override
            public void onSubmit(FormPanel.SubmitEvent event) {
                if (fileUpload.getFilename().length() == 0) {
                    Window.alert(world.messages().fileUploadNoFile());
                    event.cancel();
                }
                else if (!ImportFileX.isCorrectSuffix(fileUpload.getFilename())) {
                    Window.alert(world.messages().fileUploadWrongType());
                    event.cancel();
                }
                else {
                    submitButton.setEnabled(false);
                    notifier.uploadStarted(getFileName());
                }
            }

            public void onSubmitComplete(FormSubmitCompleteEvent event) {
                notifier.uploadEnded();
                submitButton.setEnabled(true);
            }
        });
        fileUpload.setWidth("100%");
        submitButton = new Button(world.messages().fileUpload());
        submitButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent widget) {
                form.submit();
            }
        });
        submitButton.setWidth("100%");
        VerticalPanel panel = new VerticalPanel();
        panel.add(fileUpload);
        panel.add(new HTML("<br>"));
        panel.add(submitButton);
        form.setWidget(panel);
        form.setWidth("100%");
        return form;
    }

    private String getFileName() {
        String path = fileUpload.getFilename();
        int slash = path.lastIndexOf('/');
        int backslash = path.lastIndexOf('\\');
        if (slash > 0) {
            return path.substring(slash + 1);
        }
        else if (backslash > 0) {
            return path.substring(backslash + 1);
        }
        else {
            return path;
        }
    }

}