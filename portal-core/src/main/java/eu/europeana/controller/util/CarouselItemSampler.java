package eu.europeana.controller.util;

import eu.europeana.database.DashboardDao;
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
        List<CarouselItem> copy = new ArrayList<CarouselItem>(cache);
        List<CarouselItem> selection = new ArrayList<CarouselItem>();
        while (displayCount-- > 0 && !copy.isEmpty()) {
            int index = (int)(Math.random()*copy.size());
            selection.add(copy.remove(index));
        }
        return selection;
    }

    public void refresh() {
        cache = getData();
    }

    private List<CarouselItem> getData() {
        return dashboardDao.fetchCarouselItems();
    }
}