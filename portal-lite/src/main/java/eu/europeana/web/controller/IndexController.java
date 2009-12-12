package eu.europeana.web.controller;

import eu.europeana.database.domain.Language;
import eu.europeana.web.util.CarouselItemSampler;
import eu.europeana.web.util.ControllerUtil;
import eu.europeana.web.util.ProposedSearchTermSampler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

/**
 * Where people arrive.
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 * @author Eric van der Meulen <eric.meulen@gmail.com>
 */

@Controller
public class IndexController {

    @Autowired
    private CarouselItemSampler carouselItemSampler;

    @Autowired
    private ProposedSearchTermSampler proposedSearchTermSampler;

    public void setCarouselItemSampler(CarouselItemSampler carouselItemSampler) {
        this.carouselItemSampler = carouselItemSampler;
    }

    public void setProposedSearchTermSampler(ProposedSearchTermSampler proposedSearchTermSampler) {
        this.proposedSearchTermSampler = proposedSearchTermSampler;
    }

    @RequestMapping("/index.html")
    public ModelAndView indexHandler (HttpServletRequest request) throws Exception {
        final ModelAndView page = ControllerUtil.createModelAndViewPage("index_orig");
        Language language = ControllerUtil.getLocale(request);
        // todo remove these from portal lite when portal-full is ready
        page.addObject("proposedSearchTerms", proposedSearchTermSampler.pickRandomItems(language));
        page.addObject("carouselItems", carouselItemSampler.pickShuffledRandomItems());
        return page;
    }



}