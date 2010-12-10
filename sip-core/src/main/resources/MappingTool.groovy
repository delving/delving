// ToolCode.groovy - the place for helpful methods


import java.security.MessageDigest

def extractYear(fieldObject) {
  String field = fieldObject.toString();
  def year = /\d{4}/
  def dateSlashA = /$year\/\d\d\/\d\d\//
  def dateDashA = /$year-\d\d-\d\d/
  def dateSlashB = /\d\d\/\d\d\/$year/
  def dateDashB = /\d\d-\d\d-$year/
  def ad = /(ad|AD|a\.d\.|A\.D\.)/
  def bc = /(bc|BC|b\.c\.|B\.C\.)/
  def yr = /\d{1,3}/
  def yearAD = /$yr\s?$ad/
  def yearBC = /$yr\s?$bc/
  def normalYear = /($year|$dateSlashA|$dateSlashB|$dateDashA|$dateDashB)/
  def yearRangeDash = /$normalYear-$normalYear/
  def yearRangeTo = /$normalYear to $normalYear/
  def yearRange = /($yearRangeDash|$yearRangeTo)/
  def yearRangeBrief = /$year-\d\d/
  def result = [];

  switch (field) {

    case ~/$normalYear/:
      result += (field =~ /$year/)[0]
      break;

    case ~/$yearAD/:
      result += (field =~ /$yr/)[0] + ' AD';
      break;

    case ~/$yearBC/:
      result += (field =~ /$yr/)[0] + ' BC';
      break;

    case ~/$yearRange/:
      def list = field =~ /$year/
      if (list[0] == list[1]) {
        result += list[0]
      }
      else {
        result += list[0]
        result += list[1]
      }
      break;

    case ~/$yearRangeBrief/:
      def list = field =~ /\d{1,4}/
      result += list[0]
      result += list[0][0] + list[0][1] + list[1]
      break;

    case ~/$yr/:
      result += field + ' AD'
      break;

    default:
      field.eachMatch(/$year/) {
        result += it;
      }
      break;
  }
  return result;
}

def createEuropeanaURI(identifier, collectionId) {
  if (!collectionId) {
    throw new MissingPropertyException("collectionId", String.class)
  }
  if (!identifier) {
    throw new MissingPropertyException("Identifier passed to createEuropeanaURI", String.class)
  }
  def uriBytes = identifier.toString().getBytes("UTF-8");
  def digest = MessageDigest.getInstance("SHA-1");
  def hash = ''
  for (Byte b in digest.digest(uriBytes)) {
    hash += '0123456789ABCDEF'[(b & 0xF0) >> 4]
    hash += '0123456789ABCDEF'[b & 0x0F]
  }
  return ["$collectionId/$hash"];
}

def sanitize(thing) { // same effect as in eu.delving.metadata.Sanitizer.sanitizeGroovy
  thing = (thing.toString() =~ /'/).replaceAll("\\\\'");
  thing = (thing =~ /\n/).replaceAll(' ');
  thing = (thing =~ / +/).replaceAll(' ');
  return thing
}