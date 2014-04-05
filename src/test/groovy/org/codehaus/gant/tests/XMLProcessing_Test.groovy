//  Gant -- A Groovy way of scripting Ant tasks.
//
//  Copyright © 2008–2012, 2013  Russel Winder
//
//  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
//  compliance with the License. You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software distributed under the License is
//  distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
//  implied. See the License for the specific language governing permissions and limitations under the
//  License.

package org.codehaus.gant.tests

/**
 *  A test to ensure that XML processing works.
 *
 *  <p>This test stems from a mis-feature report made on the Groovy/Gant user mailing list by Mike
 *  Nooney.</p>
 *
 *  @author Russel Winder <russel@winder.org.uk>
 */
final class XMLProcessing_Test extends GantTestCase {
  public void testMikeNooneyXMLExampleToEnsureNoProblemWithXMLJars() {
    def xmlScript = '''<Document>
    <Sentence code="S0001" format="Document.Title"/>
    <Sentence code="S0002" format="Section.Title"/>
    <Sentence code="S0003" format="Subsection.Title"/>
    <Sentence code="S0004" format="Sentence"/>
    <Sentence code="S0005" format="Sentence"/>
    <Sentence code="S0006" format="Subsection.Title"/>
    <Sentence code="S0007" format="Sentence"/>
</Document>
'''
    def targetName = 'testing'
    script = """
target(${targetName}: '') {
  def testClass = new GroovyShell(binding).evaluate('''
class Test {
	public static void test() {
		def reader = new StringReader(\\\'\\\'\\\'${xmlScript}\\\'\\\'\\\')
		def xmlData = groovy.xml.DOMBuilder.parse(reader)
		def rootElement = xmlData.documentElement
		println('root element:' + rootElement)
	}
}
return Test
''' )
  testClass.test()
}
"""
    assertEquals(0, processCmdLineTargets(targetName))
    assertEquals(resultString(targetName, 'root element:<?xml version="1.0" encoding="UTF-8"?>' + xmlScript + '\n'), output)
  }
}
