package eu.europeana.web.util;

import eu.europeana.database.StaticInfoDao;
import eu.europeana.database.domain.CarouselItem;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Pick some objects from serch terms
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class CarouselItemSampler {
    private StaticInfoDao staticInfoDao;
    private int displayCount = 3;
    private List<CarouselItem> cache = new ArrayList<CarouselItem>();

    public void setStaticInfoDao(StaticInfoDao staticInfoDao) {
        this.staticInfoDao = staticInfoDao;
    }

    public void setDisplayCount(int displayCount) {
        this.displayCount = displayCount;
    }

    void setCache(List<CarouselItem> cache) {
        this.cache = cache;
    }

    public List<CarouselItem> pickShuffledRandomItems() {
        List<CarouselItem> copy = new LinkedList<CarouselItem>(cache);
        List<CarouselItem> selection = new ArrayList<CarouselItem>();
        int displaySizeCount = displayCount;
        while (displaySizeCount-- > 0 && !copy.isEmpty()) {
            int index = (int)(Math.random()*copy.size());
            selection.add(copy.remove(index));
        }
        return selection;
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
        return staticInfoDao.fetchCarouselItems();
    }
}