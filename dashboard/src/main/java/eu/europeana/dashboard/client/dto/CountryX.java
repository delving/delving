package eu.europeana.dashboard.client.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Hold a country code and its name, for more verbose GUI
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

public class CountryX implements IsSerializable {
    private String code;
    private String name;

    public CountryX(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public CountryX() {
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String toString() {
        return code+":"+name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CountryX countryX = (CountryX) o;
        return !(code != null ? !code.equals(countryX.code) : countryX.code != null);
    }

    @Override
    public int hashCode() {
        int result = code != null ? code.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }
}