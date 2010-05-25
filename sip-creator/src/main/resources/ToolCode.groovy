import java.security.MessageDigest

// ToolCode.groovy - the place for helpful closures

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

  switch (field) {

    case ~/$normalYear/:
      return (field =~ /$year/)[0]

    case ~/$yearAD/:
      return (field =~ /$yr/)[0] + ' AD';

    case ~/$yearBC/:
      return (field =~ /$yr/)[0] + ' BC';

    case ~/$yearRange/:
      def list = field =~ /$year/
      if (list[0] == list[1]) {
        return list[0]
      }
      else {
        return list[0] + ', ' + list[1]
      }

    case ~/$yearRangeBrief/:
      def list = field =~ /\d{1,4}/
      return list[0] + ', ' + list[0][0] + list[0][1] + list[1]

    case ~/$yr/:
      return field+' AD'

    default:
      def result = ""
      field.eachMatch(/$year/) {
        result += it + '  ';
      }
      result = result.trim();
      return result.replace('  ', ', ');
    
  }
}

def createEuropeanaURI(uri) {
  def resolveUrl = 'http://www.europeana.eu/resolve/record';
  def uriBytes = uri.toString().getBytes("UTF-8");
  def digest = MessageDigest.getInstance("SHA-1");
  def hash = ''
  for (Byte b in digest.digest(uriBytes)) {
    hash += '0123456789ABCDEF'[(b & 0xF0)  >> 4]
    hash += '0123456789ABCDEF'[b & 0x0F]
  }
  return "$resolveUrl/$collectionId/$hash";
}

def createEuropeanaCollectionName() {
  return "${collectionId} ${type} ${country} ${providerAbbreviation} ${collectionName}".replaceAll(' ', '_')
}