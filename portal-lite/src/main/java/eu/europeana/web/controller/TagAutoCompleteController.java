package eu.europeana.web.controller;

import eu.europeana.database.UserDao;
import eu.europeana.database.integration.TagCount;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 */

public class TagAutoCompleteController extends AbstractController {

    private UserDao userDao;

    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    public ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
//        response.setContentType("xml");
        String query = request.getParameter("q").toLowerCase();
        if (query == null) {
            query = "";
        }
        List<TagCount> tagCountList = userDao.getSocialTagCounts(query);
        ModelAndView modelAndView = new ModelAndView("tag-autocomplete");
        modelAndView.addObject("tagList", tagCountList);
        return modelAndView;
    }
}