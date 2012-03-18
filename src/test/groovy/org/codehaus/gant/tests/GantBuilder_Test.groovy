//  Gant -- A Groovy way of scripting Ant tasks.
//
//  Copyright © 2008-10 Russel Winder
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

import org.codehaus.gant.GantBuilder
import org.codehaus.gant.GantState

/**
 *  A test for the <code>GantBuilder</code> class.
 *
 *  @author Russel Winder <russel@winder.org.uk>
 */
final class GantBuilder_Test extends GantTestCase {
  void testSetMessageOutputLevel ( ) {
    //  org.apache.tools.ant.BuildLogger appears to have no way of querying the message output level only of
    //  setting it. This means we can only test that using the setMessageOutputLevel fails to fail.
    assertEquals ( GantState.NORMAL , GantState.verbosity )
    final gantBuilder = new GantBuilder ( )
    GantState.verbosity = GantState.VERBOSE
    gantBuilder.logger.setMessageOutputLevel ( GantState.verbosity )
    assertEquals ( GantState.VERBOSE , GantState.verbosity )
  }
  void testGroovycTaskFail ( ) {
    final targetName = 'hello'
    final sourceDirectory = '.'
    final destinationDirectory = '/tmp/tmp/tmp/tmp'
    final expectedError = "groovy.lang.MissingMethodException: No signature of method: standard_input.groovyc() is applicable for argument types: (java.util.LinkedHashMap) values: [[srcdir:${sourceDirectory}, destdir:${destinationDirectory}]]\n"
    //
    //  This test may only be guaranteed to work if JUnit is operating in perTest fork mode since otherwise
    //  another test may have caused the Groovyc task to be loaded which leads to a 0 return value.
    //
    script = """
target ( ${targetName} : '' ) {
  groovyc ( srcdir : '${sourceDirectory}' , destdir : '${destinationDirectory}' )
}
"""
    assertEquals ( -13 , processCmdLineTargets ( targetName ) )
    assertEquals ( targetName + ':\n' , output )
    assertEquals ( expectedError , error )
  }
}
