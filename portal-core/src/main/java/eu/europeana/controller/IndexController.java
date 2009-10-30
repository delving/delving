package eu.europeana.controller;

import eu.europeana.controller.util.CarouselItemSampler;
import eu.europeana.controller.util.ControllerUtil;
import eu.europeana.controller.util.ProposedSearchTermSampler;
import eu.europeana.database.domain.Language;

import javax.servlet.http.HttpServletRequest;

/**
 * Where people arrive.
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 * @author Eric van der Meulen <eric.meulen@gmail.com>
 */

public class IndexController extends AbstractPortalController {

    private CarouselItemSampler carouselItemSampler;
    private ProposedSearchTermSampler proposedSearchTermSampler;

    public void setCarouselItemSampler(CarouselItemSampler carouselItemSampler) {
        this.carouselItemSampler = carouselItemSampler;
    }

    public void setProposedSearchTermSampler(ProposedSearchTermSampler proposedSearchTermSampler) {
        this.proposedSearchTermSampler = proposedSearchTermSampler;
    }

    public void handle(HttpServletRequest request, Model model) throws Exception {
        Language language = ControllerUtil.getLocale(request);
        model.setView("index_orig");
        model.put("proposedSearchTerms", proposedSearchTermSampler.pickRandomItems(language));
        model.put("carouselItems", carouselItemSampler.pickShuffledRandomItems());
    }

}