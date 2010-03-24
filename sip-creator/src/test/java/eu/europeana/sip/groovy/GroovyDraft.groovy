package eu.europeana.sip.groovy

/**
 * Testing overriding methods
 *
 * @author Serkan Demirel <serkan@blackbuilt.nl>
 */

class GroovyDraft {

	def description;

	def GroovyDraft(description) {
		this.description = description;
	}

	def String toString() {
		return "GroovyDraft [description: ${description}]";
	}

	def String tellName() {
		return "original method";
	}

	static main(args) {
		def gd = new GroovyDraft("this is a draft");
		println "Before overriding toString() : ${gd}";
		println "Before overriding tellName() : ${gd.tellName()}";
		// override
		gd.metaClass.toString = {
			return "bla";
		}
		// append
		gd.metaClass.tellName = {
			return "serkan";
		}
		println "After overriding toString() - implicit call to toString() ${gd}";
		println "After overriding tellName() ${gd.tellName()}";
		println "After overriding toString() - explicit call to toString() ${gd.toString()}";
	}
}

