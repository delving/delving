package eu.europeana.controller;

import eu.europeana.database.UserDao;
import eu.europeana.database.domain.CarouselItem;
import eu.europeana.database.domain.EuropeanaId;
import eu.europeana.database.domain.Language;
import eu.europeana.database.domain.Role;
import eu.europeana.database.domain.SavedItem;
import eu.europeana.database.domain.SavedSearch;
import eu.europeana.database.domain.SocialTag;
import eu.europeana.database.domain.User;
import eu.europeana.query.DocType;
import eu.europeana.web.util.ControllerUtil;
import javax.servlet.http.HttpServletRequest;

import java.util.Date;
import java.util.List;

/**
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class MyEuropeanaController extends AbstractPortalController {

	private UserDao userDao;
	public static final String USER_PRESENTATION_FACADE = "userPresentationFacade";

	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}

	public void handle(HttpServletRequest request, Model model) throws Exception {
		model.setView("myeuropeana");
		User user = ControllerUtil.getUser();
		if (user != null) {
			ControllerUtil.setUser(userDao.updateUser(user));
		}
		// making the presentation facade for carousel & pacta
//		model.put(USER_PRESENTATION_FACADE,
//				new UserPresentationFacade(
//						makeSavedItemsPresentationFacades(user),
//						makeSavedSearchPresentationFacades(user)));
	}

    public static class PresentationFacade {

		private boolean isInCarousel;
		private boolean isLocked;

		public PresentationFacade(boolean isInCarousel, boolean isLocked) {
			this.isInCarousel = isInCarousel;
			this.isLocked = isLocked;
		}

		public String enabled() {
			return isLocked ? " disabled=true " : " ";
		}

		public String checked() {
			return isInCarousel ? " checked=true " : "";
		}
	}

	public static class SavedItemPresentationFacade extends PresentationFacade {

		private SavedItem savedItem;

		public SavedItemPresentationFacade(boolean isInCarousel, boolean isLocked, SavedItem savedItem) {
			super(isInCarousel, isLocked);
			this.savedItem = savedItem;
		}

		/*
		 * delegates
		 */
		public CarouselItem getCarouselItem() {
			return savedItem.getCarouselItem();
		}

		public Long getId() {
			return savedItem.getId();
		}

		public String getAuthor() {
			return savedItem.getAuthor();
		}

		public String getTitle() {
			return savedItem.getTitle();
		}

		public Date getDateSaved() {
			return savedItem.getDateSaved();
		}

		public String getEuropeanaUri() {
			return savedItem.getEuropeanaId().getEuropeanaUri();
		}

		public EuropeanaId getEuropeanaId() {
			return savedItem.getEuropeanaId();
		}

		public DocType getDocType() {
			return savedItem.getDocType();
		}

		public String getEuropeanaObject() {
			return savedItem.getEuropeanaObject();
		}
	}

	public static class SavedSearchPresentationFacade extends PresentationFacade {
		private SavedSearch savedSearch;

		public SavedSearchPresentationFacade(boolean isInCarousel, boolean isLocked, SavedSearch savedSearch) {
			super(isInCarousel, isLocked);
			this.savedSearch = savedSearch;
		}

		public boolean equals(Object obj) {
            return obj instanceof SavedSearchPresentationFacade && savedSearch.equals(obj);
		}

		public Date getDateSaved() {
			return savedSearch.getDateSaved();
		}

		public Long getId() {
			return savedSearch.getId();
		}

		public Language getLanguage() {
			return savedSearch.getLanguage();
		}

		public String getQuery() {
			return savedSearch.getQuery();
		}

		public String getQueryString() {
			return savedSearch.getQueryString();
		}

		public int hashCode() {
			return savedSearch.hashCode();
		}

		public String toString() {
			return savedSearch.toString();
		}

	}

	public static class UserPresentationFacade {

		private User user;
		private List<SavedItemPresentationFacade> savedItems;
		private List<SavedSearchPresentationFacade> savedSearches;

		public UserPresentationFacade(
				List<SavedItemPresentationFacade> savedItems,
				List<SavedSearchPresentationFacade> savedSearches) {
			this.savedItems = savedItems;
			this.savedSearches = savedSearches;
		}

		public List<SavedItemPresentationFacade> getSavedItems() {
			return savedItems;
		}

		public List<SavedSearchPresentationFacade> getSavedSearches() {
			return savedSearches;
		}


		/*
		 *  delegates
		 */
		public Long getId() {
			return user.getId();
		}

		public String getEmail() {
			return user.getEmail();
		}

		public boolean isEnabled() {
			return user.isEnabled();
		}

		public String getFirstName() {
			return user.getFirstName();
		}

		public Date getLastLogin() {
			return user.getLastLogin();
		}

		public String getLastName() {
			return user.getLastName();
		}

		public String getLanguages() {
			return user.getLanguages();
		}

		public String getProjectId() {
			return user.getProjectId();
		}

		public String getProviderId() {
			return user.getProviderId();
		}

		public boolean isNewsletter() {
			return user.isNewsletter();
		}

		public String getPassword() {
			return user.getPassword();
		}

		public Date getRegistrationDate() {
			return user.getRegistrationDate();
		}

		public String getUserName() {
			return user.getUserName();
		}

		public Role getRole() {
			return user.getRole();
		}

		public List<SocialTag> getSocialTags() {
			return user.getSocialTags();
		}

		public List<User.SocialTagList> getSocialTagLists() {
			return user.getSocialTagLists();
		}

		public void setUser(User user) {
			this.user = user;
		}
	}
}