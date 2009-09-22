package eu.europeana.dashboard.client.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * For carrying translation through RPC
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

public class TranslationX implements IsSerializable {
    private LanguageX language;
    private String value;

    public TranslationX(LanguageX language, String value) {
        this.language = language;
        this.value = value;
    }

    public TranslationX() {
    }

    public LanguageX getLanguage() {
        return language;
    }

    public String getValue() {
        return value;
    }
}
