


from lxml import etree
t = etree.parse('html_files/aboutus_de.html')
print t

from lxml.builder import ElementMaker # lxml only !

e = ElementMaker(namespace="html_files/aboutus_de.html",
                 nsmap={'p' : "http://my.de/fault/namespace"})
print e

#import xml.dom.minidom
#d = xml.dom.minidom.parse('html_files/aboutus_de.html')
#print d
