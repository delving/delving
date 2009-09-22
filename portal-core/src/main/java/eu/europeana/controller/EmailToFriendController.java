package eu.europeana.controller;

import eu.europeana.controller.util.ControllerUtil;
import eu.europeana.database.domain.User;
import eu.europeana.util.EmailSender;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.TreeMap;

/**
 * When somebody wants to send a link to a frien d
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class EmailToFriendController extends AbstractAjaxController {

    private EmailSender friendEmailSender;
    private String subject;

    public void setFriendEmailSender(EmailSender friendEmailSender) {
        this.friendEmailSender = friendEmailSender;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public boolean handleAjax(HttpServletRequest request) throws Exception {
        String emailAddress = getStringParameter("email", request);
        if (!ControllerUtil.validEmailAddress(emailAddress)) {
            throw new IllegalArgumentException("Email address invalid: ["+emailAddress+"]");
        }
        String uri = getStringParameter("uri", request);
        User user = ControllerUtil.getUser();
        Map<String,Object> model = new TreeMap<String,Object>();
        model.put("user", user);
        model.put("uri", uri);
        model.put("email", emailAddress);
        friendEmailSender.sendEmail(emailAddress, user.getEmail(), subject, model);
        return true;
    }
}