package eu.europeana.dashboard.client.collections;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FormHandler;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormSubmitCompleteEvent;
import com.google.gwt.user.client.ui.FormSubmitEvent;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Widget;
import eu.europeana.dashboard.client.DashboardWidget;
import eu.europeana.dashboard.client.dto.ImportFile;

/**
 * a widget for uploading files
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

 public class FileUploadPanel extends DashboardWidget {

    private FileUpload fileUpload = new FileUpload();
    private Status notifier;

    public FileUploadPanel(World world) {
        super(world);
    }

    public interface Status {
        void uploadStarted(String fileName);
        void uploadEnded(String fileName);
    }

    public void setNotifier(Status onSubmitComplete) {
        this.notifier = onSubmitComplete;
    }

    protected Widget createWidget() {
        fileUpload.setName("uploadFile");
        final FormPanel form = new FormPanel();
        final Button submitButton = new Button(world.messages().fileUpload());
        form.setAction(GWT.getModuleBaseURL() + "uploadNormalized");
        form.setEncoding(FormPanel.ENCODING_MULTIPART);
        form.setMethod(FormPanel.METHOD_POST);
        form.addFormHandler(new FormHandler() {
            public void onSubmit(FormSubmitEvent event) {
                if (fileUpload.getFilename().length() == 0) {
                    Window.alert(world.messages().fileUploadNoFile());
                    event.setCancelled(true);
                }
                else if (!ImportFile.isCorrectSuffix(fileUpload.getFilename())) {
                    Window.alert(world.messages().fileUploadWrongType());
                    event.setCancelled(true);
                }
                else {
                    submitButton.setEnabled(false);
                    notifier.uploadStarted(getFileName());
                }
            }
            public void onSubmitComplete(FormSubmitCompleteEvent event) {
                notifier.uploadEnded(getFileName());
                submitButton.setEnabled(true);
            }
        });
        submitButton.addClickListener(new ClickListener() {
            public void onClick(Widget sender) {
                form.submit();
            }
        });
        fileUpload.setWidth("100%");
        submitButton.setWidth("100%");
        HorizontalPanel horiz = new HorizontalPanel();
        horiz.add(fileUpload);
        horiz.add(submitButton);
        form.setWidget(horiz);
        form.setWidth("100%");
        return form;
    }

    private String getFileName() {
        String path = fileUpload.getFilename();
        int slash = path.lastIndexOf('/');
        int backslash = path.lastIndexOf('\\');
        if (slash > 0) {
            return path.substring(slash+1);
        }
        else if (backslash > 0) {
            return path.substring(backslash+1);
        }
        else {
            return path;
        }
    }

}
