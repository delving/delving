package eu.europeana.sip.convert

import eu.europeana.sip.groovy.Converter
import eu.europeana.sip.groovy.FieldMapping

/**
 * todo: javadoc
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

class Generator {

  static Converter getConverter(String name) {
    return (Converter)MAP.get(name);
  }

  public static Map MAP = [
          "Verbatim": new Verbatim()
  ]

}

class Verbatim implements Converter {

  boolean applicable(FieldMapping fieldMapping) {
    return "1:1".equals(fieldMapping.getArgumentPattern())
  }

  List generateCode(FieldMapping fieldMapping) {
    String input = fieldMapping.getFromVariables()[0]
    String output = fieldMapping.getToFields()[0]
    return [
            "for (x in ${input}) {",
            "   ${output} x",
            "}"
    ]
  }

  String toString() {
    return 'Verbatim'
  }
}
