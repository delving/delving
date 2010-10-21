package eu.europeana.sip.definitions.domain;

/**
 * This enumerates the different types of acquisition
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public enum AcquisitionType {
    PURCHASE("aankoop"),
    GIFT("schenking"),
    BORROW("bruikleen"),
    TRADE("ruil"),
    ASSIGNMENT("opdracht"),
    WILL("legaat"),
    TRANSFER("overdracht"),
    INHERITANCE("successieregeling");

    private String dutchName;

    AcquisitionType(String dutchName) {
        this.dutchName = dutchName;
    }

    public String getCode() {
        return dutchName;
    }
}
