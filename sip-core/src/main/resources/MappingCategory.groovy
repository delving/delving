import eu.europeana.sip.core.GroovyList
import eu.europeana.sip.core.GroovyNode

// MappingCategory is a class used as a Groovy Category to add methods to existing classes

public class MappingCategory {

  static Object multiply(GroovyList list, Closure closure) { // operator *
    for (Object child: list) {
      closure.call(child);
    }
    return null;
  }

  static Object multiply(GroovyNode node, Closure closure) { // operator *
    for (Object child: node.children()) {
      closure.call(child);
    }
    return null;
  }

  static GroovyList mod(GroovyList list, String regex) {
    GroovyList all = new GroovyList();
    for (Object node: list) {
      if (node instanceof GroovyNode) {
        all += mod(node, regex);
      }
    }
    return all;
  }

  static GroovyList mod(GroovyNode node, String regex) { // operator %
    return new GroovyList(node.text().split(regex));
  }

  static String extractYear(GroovyList target) {
    return extractYear(target.text());
  }

  static String extractYear(GroovyNode target) {
    return extractYear(target.text());
  }

  static extractYear(String text) {
    def result = [];
    switch (text) {

      case ~/$normalYear/:
        result += (text =~ /$year/)[0]
        break;

      case ~/$yearAD/:
        result += (text =~ /$yr/)[0] + ' AD';
        break;

      case ~/$yearBC/:
        result += (text =~ /$yr/)[0] + ' BC';
        break;

      case ~/$yearRange/:
        def list = text =~ /$year/
        if (list[0] == list[1]) {
          result += list[0]
        }
        else {
          result += list[0]
          result += list[1]
        }
        break;

      case ~/$yearRangeBrief/:
        def list = text =~ /\d{1,4}/
        result += list[0]
        result += list[0][0] + list[0][1] + list[1]
        break;

      case ~/$yr/:
        result += text + ' AD'
        break;

      default:
        text.eachMatch(/$year/) {
          result += it;
        }
        break;
    }
    return result;
  }

  static toId(GroovyNode identifier, spec) {
    return toId(identifier.toString(), spec);
  }

  static toId(GroovyList identifier, spec) {
    return toId(identifier.toString(), spec);
  }

  static toId(String identifier, spec) {
    if (!spec) {
      throw new MissingPropertyException("spec", String.class)
    }
    if (!identifier) {
      throw new MissingPropertyException("Identifier passed to toId", String.class)
    }
    def uriBytes = identifier.toString().getBytes("UTF-8");
    def digest = java.security.MessageDigest.getInstance("SHA-1");
    def hash = ''
    for (Byte b in digest.digest(uriBytes)) {
      hash += '0123456789ABCDEF'[(b & 0xF0) >> 4]
      hash += '0123456789ABCDEF'[b & 0x0F]
    }
    return ["$spec/$hash"];
  }

  static String sanitize(GroovyNode node) {
    return sanitize(node.toString());
  }

  static String sanitize(GroovyList list) {
    return sanitize(list.toString());
  }

  static String sanitize(String text) { // same effect as in eu.delving.metadata.Sanitizer.sanitizeGroovy, except apostrophe removal
    text = (text =~ /\n/).replaceAll(' ');
    text = (text =~ / +/).replaceAll(' ');
    return text
  }

  static year = /\d{4}/
  static dateSlashA = /$year\/\d\d\/\d\d\//
  static dateDashA = /$year-\d\d-\d\d/
  static dateSlashB = /\d\d\/\d\d\/$year/
  static dateDashB = /\d\d-\d\d-$year/
  static ad = /(ad|AD|a\.d\.|A\.D\.)/
  static bc = /(bc|BC|b\.c\.|B\.C\.)/
  static yr = /\d{1,3}/
  static yearAD = /$yr\s?$ad/
  static yearBC = /$yr\s?$bc/
  static normalYear = /($year|$dateSlashA|$dateSlashB|$dateDashA|$dateDashB)/
  static yearRangeDash = /$normalYear-$normalYear/
  static yearRangeTo = /$normalYear to $normalYear/
  static yearRange = /($yearRangeDash|$yearRangeTo)/
  static yearRangeBrief = /$year-\d\d/
}