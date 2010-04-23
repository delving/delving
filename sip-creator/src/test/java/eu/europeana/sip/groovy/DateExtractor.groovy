package eu.europeana.sip.groovy

import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Extract Europeana dates
 *
 * @author Serkan Demirel <serkan@blackbuilt.nl>
 */
@EuropeanaExtractable
class DateExtractor {
	
	def static SAMPLE_DATES = '''
<samples>

    <!-- comma separated means array of results. We still have to decide how we are going to deal with ranges and before Christ dates -->
    <sample actual="1990" expected="1990"/>
    <sample actual="1940-1945" expected="1940,1945"/>
    <sample actual="1940-45" expected="1940,1945"/>
    <sample actual="100 AD" expected="100 AD"/>
    <sample actual="100 BC" expected="100 BC"/>
    <sample actual="10/02/2010" expected="2010"/>
    <sample actual="10/02/2010-11/02/2010" expected="2010"/>
    <sample actual="The object was created in 1910 and sold May 2005" expected="1910, 2005"/>
    <sample actual="2010-03-19" expected="2010"/>
    <sample actual="2005-03-09 to 2010-03-19" expected="2005,2010"/>
    <!-- in dc:date or dc:temporal fields -->
    <sample actual="65 BC" expected="65 BC"/>
    <sample actual="10 AD" expected="10 AD"/>
    <sample actual="100" expected="100 AD"/>
    <sample actual="65" expected="65 AD"/>
    <!-- proposed rules for dealing with historical dates

    * Years with less then 4 digits need to be affixed with BC or AD
    * BC dates always need to be affixed with BC
    * For ranges only the start and end date are preserved (better solution will be added later, for example adding all intermediate years as europeana:year fields.) -->

</samples>
''';

	String regex = /.*([0-9]{4})/;
	Pattern pattern = Pattern.compile(regex);

	String extract(String originalDate) {
		Matcher matcher = pattern.matcher(originalDate);

		println "Found #${matcher.groupCount()}";

		def match;
		while (matcher.find()) {
			match = matcher.group(1);
		}

		return "${match}";
	}

	static void main(String ... args) {

		def dateExtractor = new DateExtractor();
		def samples = new XmlParser().parseText(SAMPLE_DATES);
		for (sample in samples.sample) {
			println "--- Actual ${sample.@actual} --- Expected ${sample.@expected}";
			def result = dateExtractor.extract(sample.@actual.toString());
			assert result == sample.@expected;
		}
	}
}
