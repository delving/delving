package eu.europeana.controller.util;

import eu.europeana.database.domain.CarouselItem;
import junit.framework.Assert;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since Oct 30, 2009: 1:44:34 PM
 */
public class CarouselItemSamplerTest {
    private static final Logger log = Logger.getLogger(CarouselItemSamplerTest.class);

    private List<CarouselItem> cache;

    @Before
    public void setUp() {
        cache = fillCarouselItemList();
    }

    @Test
    public void testPickShuffledRandomItems() {
        CarouselItemSampler sampler = new CarouselItemSampler();
        sampler.setDisplayCount(3);
        sampler.setCache(cache);
        List<CarouselItem> previous = sampler.pickShuffledRandomItems();
        for (int i = 0; i < 10; i++) {
            Assert.assertNotNull(previous);
            System.out.println(previous);
            List<CarouselItem> next = sampler.pickShuffledRandomItems();
            Assert.assertNotNull(next);
            Assert.assertNotSame("Next and Previous shuffeled list should not be the same", next, previous);
            previous = next;
        }
    }

    private List<CarouselItem> fillCarouselItemList() {
        List<CarouselItem> dummylist = new ArrayList<CarouselItem>();
        for (int i = 0; i < 40; i++) {
            CarouselItem item = new CarouselItem();
            item.setEuropeanaUri("uri-" + i);
            item.setTitle("title: "+ i);
            dummylist.add(item);
        }
        return dummylist;
    }
}
