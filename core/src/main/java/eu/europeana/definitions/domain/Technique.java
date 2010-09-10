package eu.europeana.definitions.domain;

/**
 * Enumeration of techniques
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public enum Technique {
    PAINTING("schilderkunst"),
    WORK_ON_PAPER("werk op papier"),
    TEXTILE("textiel"),
    PHOTOGRAPHY("fotografie"),
    FILM_VIDEO_DVD("film, video, dvd"),
    OTHER("anders");

    private String dutchName;

    Technique(String dutchName) {
        this.dutchName = dutchName;
    }

    public String getCode() {
        return dutchName;
    }
}
