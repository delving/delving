package eu.europeana.database.dao.fixture;

import eu.europeana.database.domain.Contributor;
import eu.europeana.database.domain.Country;
import eu.europeana.database.domain.EuropeanaCollection;
import eu.europeana.database.domain.EuropeanaId;
import eu.europeana.database.domain.Partner;
import eu.europeana.database.domain.PartnerSector;
import eu.europeana.database.domain.Role;
import eu.europeana.database.domain.User;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Tools to put some data into the database for testing
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 * @author Nicola Aloia <nicola.aloia@isti.cnr.it>
 */

public class DatabaseFixture {

    @PersistenceContext
    protected EntityManager entityManager;

    @Transactional
    public List<User> createUsers(String who, int count) {
        List<User> users = new ArrayList<User>();
        for (int walk = 0; walk < count; walk++) {
            User user = new User(
                    null,
                    "user-" + who + walk,
                    who + walk + "@email.com",
                    "password-" + who + walk,
                    "First Name " + who + walk,
                    "Last Name " + who + walk,
                    "", "", "", false,
                    Role.ROLE_USER, true
            );
            entityManager.persist(user);
            users.add(user);
        }
        return users;
    }

    @Transactional
    public List<EuropeanaId> createEuropeanaIds(String collectionName, int count) {
        EuropeanaCollection collection = new EuropeanaCollection();
        collection.setName(collectionName);
        collection.setDescription("Created for testing");
        entityManager.persist(collection);
        List<EuropeanaId> ids = new ArrayList<EuropeanaId>();
        for (int walk = 0; walk < count; walk++) {
            EuropeanaId id = new EuropeanaId(collection);
            id.setCreated(new Date());
            id.setEuropeanaUri("http://europeana.uri.pretend/item" + walk);
            entityManager.persist(id);
            ids.add(id);
        }
        return ids;
    }

    @Transactional
    public <Entity> Entity fetch(Class<Entity> entityClass, Long id) {
        return entityManager.find(entityClass, id);
    }

    @Transactional
    public EuropeanaId fetchEuropeanaId(Long id) {
        EuropeanaId europeanaId = entityManager.find(EuropeanaId.class, id);
        europeanaId.getSocialTags().size();
        return europeanaId;
    }

    @Transactional
    public List<Partner> createPartners(String name, int count) {
        List<Partner> partners = new ArrayList<Partner>();
        for (int walk = 0; walk < count; walk++) {
            Partner partner = new Partner();
            partner.setName(name + walk);
            partner.setUrl("http://europeana.uri.pretend/item" + walk);
            partner.setSector(PartnerSector.RESEARCH_INSTITUTIONS);
            entityManager.persist(partner);
            partners.add(partner);
        }
        return partners;
    }

    @Transactional
    public List<Contributor> createContributors(String name, int count) {
        List<Contributor> contributors = new ArrayList<Contributor>();

        for (int walk = 0; walk < count; walk++) {
            Contributor contributor = new Contributor();
            contributor.setAcronym(name + walk);
            contributor.setCountry(Country.ITALY);
            contributor.setEnglishName(name + walk);
            contributor.setOriginalName(name + walk);
            contributor.setNumberOfPartners(String.valueOf(walk));
            contributor.setProviderId(name + walk);
            contributor.setUrl("http://europeana.uri.pretend/item" + walk);
            entityManager.persist(contributor);
            contributors.add(contributor);
        }
        return contributors;
    }


    @Transactional
    public Partner getPartner(Long partnerId) {
        return entityManager.find(Partner.class, partnerId);
    }

    @Transactional
    public Contributor getContributor(Long contributorId) {
        return entityManager.find(Contributor.class, contributorId);
    }
}
