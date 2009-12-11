package eu.europeana.web.util;

import eu.europeana.database.domain.User;
import eu.europeana.query.QueryModel;
import org.apache.log4j.Logger;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.text.MessageFormat;
import java.util.Date;

/**
 */
public class RequestLoggerImpl {
    private Logger log = Logger.getLogger(getClass());

//        private RequestLogger.LogTypeId logTypeId;
//        private String query; //
//        private QueryExpression.QueryType queryType;
//        private String queryConstraints; // a comma separated list of qf's from url.
//        private String pageId;
//        // private String state;
//        private int pageNr;
//        private int nrResults;
//        private String languageFacets;
//        private String countryFacet;
//        private RequestLogger.UserActions actions;

//
//        public RequestLogger(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o) {
//            // determine the Class
//            final Class<? extends Object> aClass = o.getClass();
////        if (aClass == StaticPageController.class) {
////            actions = UserActions.STATICPAGE;
////        } else if (aClass == IndexController.class) {
////            actions = UserActions.INDEXPAGE;
////        }
//        }

        // this constructor is for search Controllers

//        public RequestLogger(ResultModel resultModel, QueryModel queryModel, ResultPagination resultPagination, Class<? extends Object> aClass) {

//            nrResults = resultPagination.getNumFound();
//
//            // get elements from queryModel
//            query = queryModel.getQueryString();
//            queryType = queryModel.getQueryType();
//            queryConstraints = formatQueryConstraints(queryModel.getConstraints());


//        }


        private String formatQueryConstraints(QueryModel.Constraints constraints) {
            return null;  //To change body of created methods use File | Settings | File Templates.
        }

        /*
        * This method will be called in configInterceptor and will take some info form
        * request and modelAndView, format the log entry, and write it to log.
        */
        public void writeRequestLog (HttpServletRequest request, ModelAndView modelAndView) {
            if (modelAndView.getViewName() != null) {
                Date date = new Date();
                String ip = request.getRemoteAddr();
                String templateName = modelAndView.getViewName();
                final User user = ControllerUtil.getUser();
                String userId;
                if (user != null) {
                    userId = user.getId().toString();
                } else {
                    userId = "";
                }
                String language = ControllerUtil.getLocale(request).toString();
                log.info(MessageFormat.format("[userId={0}, lang={1}, date={2}, template={3}, ip={4}]", userId, language, date, templateName, ip));
            }
        }

}
