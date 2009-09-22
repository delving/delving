package eu.europeana.controller.util;

import eu.europeana.database.dao.DashboardDao;
import eu.europeana.database.domain.CarouselItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Pick some objects from serch terms
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class CarouselItemSampler {
    private DashboardDao dashboardDao;
    private int displayCount = 3;
    private List<CarouselItem> cache = new ArrayList<CarouselItem>();

    public void setDashboardDao(DashboardDao dashboardDao) {
        this.dashboardDao = dashboardDao;
    }

    public void setDisplayCount(int displayCount) {
        this.displayCount = displayCount;
    }

    public List<CarouselItem> pickRandomItems() {
        List<CarouselItem> selection = new ArrayList<CarouselItem>(cache);
        while (selection.size() > displayCount) {
            int index = (int)(Math.random()*selection.size());
            selection.remove(index);
        }
        return selection;
    }

    public void refresh() {
        cache = getData();
    }

    private List<CarouselItem> getData() {
        return dashboardDao.getAllCarouselItems();
    }
}