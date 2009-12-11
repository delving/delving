package eu.europeana.web.controller;

import eu.europeana.database.StaticInfoDao;
import eu.europeana.database.UserDao;
import eu.europeana.database.domain.CarouselItem;
import eu.europeana.database.domain.SavedItem;
import eu.europeana.database.domain.SavedSearch;
import eu.europeana.database.domain.SearchTerm;
import eu.europeana.database.domain.SocialTag;
import eu.europeana.database.domain.User;
import eu.europeana.query.DocType;
import eu.europeana.web.util.ControllerUtil;

import javax.servlet.http.HttpServletRequest;
import java.net.URLDecoder;

/**
 * Remove something associated with a user
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class SaveController extends AbstractAjaxController {

    private UserDao userDao;
    private StaticInfoDao staticInfoDao;

    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    public void setStaticInfoDao(StaticInfoDao staticInfoDao) {
        this.staticInfoDao = staticInfoDao;
    }

    public boolean handleAjax(HttpServletRequest request) throws Exception {
        User user = ControllerUtil.getUser();
        String className = request.getParameter("className");
        String idString = request.getParameter("id");
        if (className == null) {
            throw new IllegalArgumentException("Expected 'className' parameter!");
        }

        switch (findModifiable(className)) {
            case CAROUSEL_ITEM:
                SavedItem savedItemForCarousel = userDao.fetchSavedItemById(Long.valueOf(idString));
                CarouselItem carouselItem = staticInfoDao.createCarouselItem(
                        savedItemForCarousel.getEuropeanaId(),
                        savedItemForCarousel.getId());
                if (carouselItem == null) {
                    return false;
                }
                break;
            case SAVED_ITEM:
                SavedItem savedItem = new SavedItem();
                savedItem.setTitle(getStringParameter("title", request));
                savedItem.setAuthor(getStringParameter("author", request));
                savedItem.setDocType(DocType.valueOf(getStringParameter("docType", request)));
                savedItem.setLanguage(ControllerUtil.getLocale(request));
                savedItem.setEuropeanaObject(getStringParameter("europeanaObject", request));
                user = userDao.addSavedItem(user, savedItem, getStringParameter("europeanaUri", request));
                break;
            case SAVED_SEARCH:
                SavedSearch savedSearch = new SavedSearch();
                savedSearch.setQuery(getStringParameter("query", request));
                savedSearch.setQueryString(URLDecoder.decode(getStringParameter("queryString", request), "utf-8"));
                savedSearch.setLanguage(ControllerUtil.getLocale(request));
                user = userDao.addSavedSearch(user, savedSearch);
                break;
            case SEARCH_TERM:
                SearchTerm searchTerm = staticInfoDao.addSearchTerm(Long.valueOf(idString));
                if (searchTerm == null) {
                    return false;
                }
                break;
            case SOCIAL_TAG:
                SocialTag socialTag = new SocialTag();
                socialTag.setTag(getStringParameter("tag", request));
                socialTag.setEuropeanaUri(getStringParameter("europeanaUri", request));
                socialTag.setDocType(DocType.valueOf(getStringParameter("docType", request)));
                socialTag.setEuropeanaObject(getStringParameter("europeanaObject", request));
                socialTag.setTitle(getStringParameter("title", request));
                socialTag.setLanguage(ControllerUtil.getLocale(request));
                user = userDao.addSocialTag(user, socialTag);
                break;
            default:
                throw new IllegalArgumentException("Unhandled removable");
        }

        ControllerUtil.setUser(user);
        return true;
    }

    private Modifiable findModifiable(String className) {
        for (Modifiable modifiable : Modifiable.values()) {
            if (modifiable.matches(className)) {
                return modifiable;
            }
        }
        throw new IllegalArgumentException("Unable to find removable class with name " + className);
    }

    private enum Modifiable {
        SEARCH_TERM(SearchTerm.class),
        CAROUSEL_ITEM(CarouselItem.class),
        SAVED_ITEM(SavedItem.class),
        SAVED_SEARCH(SavedSearch.class),
        SOCIAL_TAG(SocialTag.class);

        private String className;

        private Modifiable(Class<?> clazz) {
            this.className = clazz.getName().substring(clazz.getName().lastIndexOf('.') + 1);
        }

        public boolean matches(String className) {
            return this.className.equals(className);
        }
    }
}