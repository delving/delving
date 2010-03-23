package eu.europeana.sip.gui;

/**
 * Supported file types for FileMenu
 *
 * @author Serkan Demirel <serkan@blackbuilt.nl>
 */
public enum FileType {

    XML("XML File", ".xml"),
    MAPPING("Groovy mapping file", ".mapping");

    private String description;
    private String extension;

    FileType(String description, String extension) {
        System.out.printf("FileType %s:%s%n", description, extension);
        this.description = description;
        this.extension = extension;
    }

    public String getDescription() {
        return description;
    }

    public String getExtension() {
        return extension;
    }
}
