package eu.europeana.controller;

import static junit.framework.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since Dec 6, 2008: 11:52:11 PM
 */
public class TestControllers {

    MockHttpServletRequest request;
    MockHttpServletResponse response;

    @Before
    public void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @Test
    public void testSaveItemInjection() throws Exception {
        SaveItemController saveItemController = new SaveItemController();
        request.addParameter("author", "europeana");
        request.addParameter("title", "culture <script>alert();</script>");
        request.addParameter("docType", "IMAGE");
        request.addParameter("europeanaUri", "http://foo.bar.com");
        request.addParameter("europeanaObject", "http://foo.bar.com/bla.jpg");
        ModelAndView mav = saveItemController.handleRequestInternal(request, response);
        assertTrue(mav.getModel().get("success").toString().equalsIgnoreCase("false"));
    }
}
