//  Gant -- A Groovy way of scripting Ant tasks.
//
//  Copyright © 2006–2010, 2013 Russel Winder
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
 *  A test to ensure that the target listing works.
 *
 *  @author Russel Winder <russel@winder.org.uk>
 */
final class DryRun_Test extends GantTestCase {
  final something = 'something'
  final somethingElse = 'somethingElse'
  void setUp() {
    super.setUp()
    script = """
target(${something}: '') { echo(message: '${something}') }
target(${somethingElse}: '') { echo(message: '${somethingElse}') }
"""
  }
  void testMissingDefault() {
    assertEquals(-12, gant.processArgs(['-n',  '-f',  '-'] as String[]))
    assertEquals('', output)
    assertEquals('Target default does not exist.\n', error)
  }
  void testMissingNamedTarget() {
    final missingTargetName = 'blah'
    assertEquals(-11, gant.processArgs(['-n',  '-f',  '-' , missingTargetName] as String[]))
    assertEquals('', output)
    assertEquals("Target ${missingTargetName} does not exist.\n", error)
  }
  void testSomething() {
    assertEquals(0, gant.processArgs(['-n',  '-f',  '-' , something] as String[]))
    assertEquals(resultString(something, "     [echo] message : '${something}'\n"), output)
    assertEquals('', error)
  }
  void testSomethingElse() {
    assertEquals(0, gant.processArgs(['-n',  '-f',  '-' , somethingElse] as String[]))
    assertEquals(resultString(somethingElse, "     [echo] message : '${somethingElse}'\n"), output)
    assertEquals('', error)
  }
}
