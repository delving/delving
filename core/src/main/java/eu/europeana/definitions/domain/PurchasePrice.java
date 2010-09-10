package eu.europeana.definitions.domain;

/**
 * purchase price categories
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public enum PurchasePrice {
    LOW(0, 100),
    LOW_MED(100, 1000),
    MED(1000, 10000),
    MED_HIGH(10000, 100000),
    HIGH(100000, 1000000),
    VERY_HIGH(1000000, -1);

    private int from, to;

    PurchasePrice(int from, int to) {
        this.from = from;
        this.to = to;
    }

    public String getCode() {
        if (to < 0) {
            return from + "+";
        }
        else {
            return from + " - " + to;
        }
    }

    public boolean contains(int price) {
        if (to < 0) {
            return price >= from;
        }
        else {
            return price >= from && price < to;
        }
    }

    public static PurchasePrice find(int price) {
        if (price < 0) {
            throw new IllegalArgumentException("Negative price! "+price);
        }
        for (PurchasePrice pp : PurchasePrice.values()) {
            if (pp.contains(price)) {
                return pp;
            }
        }
        throw new RuntimeException("Unable to match price "+price);
    }
}
