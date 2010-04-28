package eu.europeana.sip.convert

import eu.europeana.sip.groovy.CodeTemplate
import eu.europeana.sip.groovy.FieldMapping

/**
 * todo: javadoc
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

class Generator {

  static CodeTemplate getConverter(String name) {
    return (CodeTemplate)MAP.get(name);
  }

  public static Map MAP = [
          "Verbatim": new Verbatim(),
          "First Instance": new FirstInstance()
  ]

}

class Verbatim implements CodeTemplate {

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

class FirstInstance implements CodeTemplate {

  boolean applicable(FieldMapping fieldMapping) {
    return "1:1".equals(fieldMapping.getArgumentPattern())
  }

  List generateCode(FieldMapping fieldMapping) {
    String input = fieldMapping.getFromVariables()[0]
    String output = fieldMapping.getToFields()[0]
    return [
            "${output} ${input}[0]"
    ]
  }

  String toString() {
    return 'Verbatim'
  }
}
