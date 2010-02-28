/*
 * Copyright 2007 EDL FOUNDATION
 *
 * Licensed under the EUPL, Version 1.0 or - as soon they
 * will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * you may not use this work except in compliance with the
 * Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the Licence is
 * distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the Licence for the specific language governing
 * permissions and limitations under the Licence.
 */

package eu.europeana.dashboard.server;

import eu.europeana.core.database.domain.*;
import eu.europeana.core.database.incoming.ImportFile;
import eu.europeana.core.querymodel.query.DocType;
import eu.europeana.dashboard.client.dto.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Do all the conversions to data transfer objects from domain objects
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

public class DataTransfer {

    public static EuropeanaCollectionX convert(EuropeanaCollection collection) {
        return new EuropeanaCollectionX(
                collection.getId(),
                collection.getName(),
                collection.getDescription(),
                collection.getFileName(),
                collection.getCollectionLastModified(),
                collection.getFileUserName(),
                collection.getFileState().toString(),
                collection.getCollectionState().toString(),
                collection.getTotalRecords(),
                collection.getTotalOrphans(),
                collection.getImportError()
        );
    }

    public static EuropeanaCollection convert(EuropeanaCollectionX collectionX) {
        EuropeanaCollection collection = new EuropeanaCollection(collectionX.getId());
        collection.setName(collectionX.getName());
        collection.setDescription(collectionX.getDescription());
        collection.setFileName(collectionX.getFileName());
        collection.setCollectionLastModified(collectionX.getCollectionLastModified());
        collection.setFileUserName(collectionX.getFileUserName());
        collection.setFileState(ImportFileState.valueOf(collectionX.getFileState().toString()));
        collection.setCollectionState(CollectionState.valueOf(collectionX.getCollectionState().toString()));
        collection.setTotalRecords(collectionX.getTotalRecords());
        collection.setTotalOrphans(collectionX.getTotalOrphans());
        collection.setImportError(collectionX.getImportError());
        return collection;
    }

    public static CarouselItemX convert(CarouselItem item) {
        String typeString = (item.getType() == null) ? DocType.UNKNOWN.toString() : item.getType().toString();
        return new CarouselItemX(
                item.getId(),
                item.getEuropeanaUri(),
                item.getTitle(),
                item.getThumbnail(),
                item.getCreator(),
                item.getYear(),
                item.getProvider(),
                item.getLanguage().getName(),
                CarouselItemX.DocTypeX.valueOf(typeString)
        );
    }

    public static EuropeanaIdX convert(EuropeanaId id) {
        return new EuropeanaIdX(
                id.getId(),
                id.getTimesViewed(),
                id.getCreated(),
                id.getLastViewed(),
                id.getLastModified(),
                id.getEuropeanaUri(),
                id.getBoostFactor()
        );
    }

    public static SavedSearchX convert(SavedSearch s) {
        return new SavedSearchX(
                s.getId(),
                s.getQueryString(),
                convert(s.getLanguage())
        );
    }

    public static LanguageX convert(Language language) {
        return new LanguageX(
                language.getCode(),
                language.getName()
        );
    }

    public static CountryX convert(Country country) {
        return new CountryX(country.toString(), country.getEnglishName());
    }

    public static UserX convert(User user) {
        return new UserX(
                user.getId(),
                user.getUserName(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getLanguages(),
                user.getProjectId(),
                user.getProviderId(),
                user.isNewsletter(),
                user.getRegistrationDate(),
                user.getLastLogin(),
                RoleX.valueOf(user.getRole().toString()),
                user.isEnabled()
        );
    }

    public static User convert(UserX userX) {
        return new User(
                userX.getId(),
                userX.getUserName(),
                userX.getEmail(),
                userX.getPassword(),
                userX.getFirstName(),
                userX.getLastName(),
                userX.getLanguages(),
                userX.getProjectId(),
                userX.getProviderId(),
                userX.isNewsletter(),
                Role.valueOf(userX.getRole().toString()),
                userX.isEnabled()
        );
    }

    public static DashboardLogX convert(DashboardLog log) {
        return new DashboardLogX(
                log.getId(),
                log.getWho(),
                log.getTime(),
                log.getWhat()
        );
    }

    public static ImportFileX convert(ImportFile importFile) {
        return new ImportFileX(
                importFile.getFileName(),
                ImportFileX.State.valueOf(importFile.getState().toString()),
                importFile.getLastModified()
        );
    }

    public static ImportFile convert(ImportFileX importFile) {
        return new ImportFile(
                importFile.getFileName(),
                ImportFileState.valueOf(importFile.getState().toString()),
                importFile.getLastModified()
        );
    }

    public static List<ImportFileX> convert(List<ImportFile> importFiles) {
        List<ImportFileX> list = new ArrayList<ImportFileX>();
        for (ImportFile importFile : importFiles) {
            list.add(convert(importFile));
        }
        return list;
    }
}
