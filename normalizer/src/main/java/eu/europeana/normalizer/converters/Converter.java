package eu.europeana.normalizer.converters;

/**
 * Convert a field
 */

public interface Converter {
    String convertValue(String value) throws ConverterException;
}

