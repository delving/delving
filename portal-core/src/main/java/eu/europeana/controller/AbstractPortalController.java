package eu.europeana.controller;

import eu.europeana.controller.util.ControllerUtil;
import eu.europeana.database.domain.User;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public abstract class AbstractPortalController extends AbstractController {

    public interface Model {
        void setView(String view);
        void setContentType(String contentType);
        void put(String key, Object value);
        Object get(String key);
    }

    public final ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelImpl model = new ModelImpl(request);
        handle(request,model);

        User user = ControllerUtil.getUser();
//        MyEuropeanaController.UserPresentationFacade facade = (MyEuropeanaController.UserPresentationFacade) model.get(MyEuropeanaController.USER_PRESENTATION_FACADE);

        model.put("user", user);
//        if (facade == null) {
//            model.put("user", user);
//        } else {
//            // populate facade
//            facade.setUser(user);
//            model.put("user", facade);
//        }

        if (model.contentType != null) {
            response.setContentType(model.contentType);
        }

        return new ModelAndView(model.view, model.modelMap);
    }

    public abstract void handle(HttpServletRequest request, Model model) throws Exception;

    private class ModelImpl implements Model {
        private HttpServletRequest request;
        private String view;
        private String contentType;
        private Map<String,Object> modelMap = new TreeMap<String,Object>();

        private ModelImpl(HttpServletRequest request) {
            this.request = request;
        }

        public void setView(String view) {
            this.view = view;
        }

        public void setContentType(String contentType) {
            this.contentType = contentType;
        }

        public void put(String key, Object value) {
            modelMap.put(key,value);
        }

        public Object get(String key) {
            return modelMap.get(key);
        }
    }
}