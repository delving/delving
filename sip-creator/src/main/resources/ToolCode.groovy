import java.security.MessageDigest

// ToolCode.groovy - the place for helpful closures

def extractYear(String field) {
  year = /\d{4}/
  dateSlashA = /$year\/\d\d\/\d\d\//
  dateDashA = /$year-\d\d-\d\d/
  dateSlashB = /\d\d\/\d\d\/$year/
  dateDashB = /\d\d-\d\d-$year/
  ad = /(ad|AD|a\.d\.|A\.D\.)/
  bc = /(bc|BC|b\.c\.|B\.C\.)/
  yr = /\d{1,3}/
  yearAD = /$yr\s?$ad/
  yearBC = /$yr\s?$bc/
  normalYear = /($year|$dateSlashA|$dateSlashB|$dateDashA|$dateDashB)/
  yearRangeDash = /$normalYear-$normalYear/
  yearRangeTo = /$normalYear to $normalYear/
  yearRange = /($yearRangeDash|$yearRangeTo)/
  yearRangeBrief = /$year-\d\d/

  switch (field) {

    case ~/$normalYear/:
      return (field =~ /$year/)[0]

    case ~/$yearAD/:
      return (field =~ /$yr/)[0] + ' AD';

    case ~/$yearBC/:
      return (field =~ /$yr/)[0] + ' BC';

    case ~/$yearRange/:
      list = field =~ /$year/
      if (list[0] == list[1]) {
        return list[0]
      }
      else {
        return list[0] + ', ' + list[1]
      }

    case ~/$yearRangeBrief/:
      list = field =~ /\d{1,4}/
      return list[0] + ', ' + list[0][0] + list[0][1] + list[1]

    case ~/$yr/:
      return field+' AD'

    default:
      result = ""
      field.eachMatch(/$year/) {
        result += it + '  ';
      }
      result = result.trim();
      return result.replace('  ', ', ');
    
  }
}

def createEuropeanaURI(String collection, String uri) {
  resolveUrl = 'http://www.europeana.eu/resolve/record';
  uriBytes = uri.getBytes("UTF-8");
  digest = MessageDigest.getInstance("SHA-1");
  hash = ''
  for (Byte b in digest.digest(uriBytes)) {
    hash += '0123456789ABCDEF'[b & 0x0F]
    hash += '0123456789ABCDEF'[(b & 0xF0) >> 4]
  }
  return "$resolveUrl/$collection/$hash";
}