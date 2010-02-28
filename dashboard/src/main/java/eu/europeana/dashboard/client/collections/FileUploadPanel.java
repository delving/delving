package eu.europeana.dashboard.client.collections;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import eu.europeana.dashboard.client.DashboardWidget;
import eu.europeana.dashboard.client.dto.ImportFileX;

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

    @Override
    protected Widget createWidget() {
        fileUpload.setName("uploadFile");
        final FormPanel form = new FormPanel();
        final Button submitButton = new Button(world.messages().fileUpload());
        form.setAction(GWT.getModuleBaseURL() + "uploadNormalized");
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

        });
        form.addSubmitCompleteHandler(new FormPanel.SubmitCompleteHandler() {

			@Override
			public void onSubmitComplete(SubmitCompleteEvent event) {
               notifier.uploadEnded(getFileName());
               submitButton.setEnabled(true);
			}

        });
        submitButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
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
