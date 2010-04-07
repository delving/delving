package eu.europeana.sip.groovy

/**
 * Normalizing the dates
 * # Years with less then 4 digits need to be affixed with BC or AD
 * # BC dates always need to be affixed with BC
 * # For ranges only the start and end date are preserved (better solution will be added later, for example adding all intermediate years as europeana:year fields.)
 *
 * @author Serkan Demirel <serkan@blackbuilt.nl>
 */
class DateNormalizer {

	private String provided;
	private String result;
	private String normalizer = /[0-9]{2,4}/;

	def DateNormalizer(provided) {
		this.provided = provided;
	}

	def String normalize(String date) {
		// todo: do some fancy regex here
		System.out.printf("%s %n", date.contains(normalizer));
		return date;
	}

	def String parse(passed) {
		switch (passed) {
			case (/[0-9]+/):
				return "numeric";
			case "/[0-9]*/":
				return "string";
				break;
			case 10..20:
				return "range";
				break;
			default: return "fail";
		}
	}

	def static void main(args) {
		def dateNormalizer = new DateNormalizer("1701-1702");
		dateNormalizer.normalize("1701-1702");
		dateNormalizer.parse("1701-1700");
		dateNormalizer.parse(19);
		def parser = new XmlParser().parse(new File("sip-creator/src/test/resources/dates.xml"));
		printf("%s : %s%n", parser.sample.@actual, parser.sample.@expected);
		for (s in parser.sample.@actual) {
			printf("-> %s%n", dateNormalizer.parse(s));
		}
	}

}
