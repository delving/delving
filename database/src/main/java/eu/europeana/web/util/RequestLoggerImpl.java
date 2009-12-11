package eu.europeana.web.util;

import eu.europeana.database.domain.User;
import eu.europeana.query.QueryExpression;
import eu.europeana.query.QueryModel;
import eu.europeana.query.RequestLogger;
import eu.europeana.query.ResultModel;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.MessageFormat;

/**
 */
public class RequestLoggerImpl implements RequestLogger {
    private Logger log = Logger.getLogger(getClass());

    private RequestLogger.LogTypeId logTypeId;
    private String query; //
    private QueryExpression.QueryType queryType;
    private String queryConstraints; // a comma separated list of qf's from url.
    private String pageId;
    // private String state;
    private int pageNr;
    private int nrResults;
    private String languageFacets;
    private String countryFacet;
    private RequestLogger.UserActions actions;


    private String formatQueryConstraints(QueryModel.Constraints constraints) {
        return null;  //To change body of created methods use File | Settings | File Templates.
    }

    /*
    * this method is used to log actions in Interceptors
     */

    public void log(HttpServletRequest request, HttpServletResponse servletResponse, UserActions actions) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void log(HttpServletRequest request, ModelAndView model, UserActions actions) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void log(HttpServletRequest request, ResultModel resultModel, QueryModel queryModel, ResultPagination resultPagination, ModelAndView model, UserActions actions) {
        // get elements from queryModel
        nrResults = resultPagination.getNumFound();
        query = queryModel.getQueryString();
        queryType = queryModel.getQueryType();
        queryConstraints = formatQueryConstraints(queryModel.getConstraints());
    }

    public void log(HttpServletRequest request, UserActions actions) {
        log.info(
                MessageFormat.format(
                "[{0}, action={1}]",
                printLogPrefix(request), actions));
    }


    private String printLogPrefix(HttpServletRequest request) {
        DateTime date = new DateTime();
        String ip = request.getRemoteAddr();
        final User user = ControllerUtil.getUser();
        String userId;
        if (user != null) {
            userId = user.getId().toString();
        } else {
            userId = "";
        }
        String language = ControllerUtil.getLocale(request).toString();
        return MessageFormat.format(
                "userId={0}, lang={1}, date={2},  ip={3},",
                userId, language, date, ip);
    }
}
