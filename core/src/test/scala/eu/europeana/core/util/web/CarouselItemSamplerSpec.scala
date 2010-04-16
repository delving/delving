package eu.europeana.core.util.web

import _root_.eu.europeana.core.database.domain.CarouselItem
import _root_.org.junit.runner.RunWith
import _root_.org.scalatest.matchers.ShouldMatchers
import _root_.org.scalatest.Spec
import _root_.org.scalatest.junit.JUnitRunner
import collection.mutable.ListBuffer
import collection.mutable.Set
import scala.collection.JavaConversions._
import java.util.List

/**
 *
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since Apr 5, 2010 10:55:58 PM
 */

@RunWith(classOf[JUnitRunner])
class CarouselItemSamplerSpec extends Spec with ShouldMatchers {

  describe("A Carousel Item Sampler") {
      val cache: List[CarouselItem] = fillCarouselItemList
      val sampler = new CarouselItemSampler

      describe("(when given a List of CaurouselItems)") {
        sampler setDisplayCount (3)
        sampler setCache (cache)

        it("should give back differently shuffled items with each request") {
          val carouselItemSet = Set[List[CarouselItem]]()
          val uniqueItems = 10
          (0 until uniqueItems).foreach(inc => carouselItemSet += sampler.pickShuffledRandomItems)
          carouselItemSet.size should equal (uniqueItems)
        }

      }

    }

  private def fillCarouselItemList: List[CarouselItem] = {
    def createCarouselItem(inc: Int): CarouselItem = {
      val item = new CarouselItem
      item.setEuropeanaUri("uri-" + inc)
      item.setTitle("title: " + inc)
      item
    }

    val listBuffer = new ListBuffer[CarouselItem]
    (0 until 40).foreach{ inc => listBuffer += createCarouselItem(inc) }
    listBuffer
  }
}