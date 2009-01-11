//  Gant -- A Groovy way of scripting Ant tasks.
//
//  Copyright © 2008-9 Russel Winder
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
 *  @author Russel Winder <russel.winder@concertant.com>
 */
final class GantBuilder_Test extends GantTestCase {
  void testSetMessageOutputLevel ( ) {
    //  org.apache.tools.ant.BuildLogger appears to have no way of querying the message output level only of
    //  setting it. This means we can only test that using the setMessageOutputLevel fails to fail.
    assertEquals ( GantState.NORMAL , GantState.verbosity )
    final def gantBuilder = new GantBuilder ( )
    GantState.verbosity = GantState.VERBOSE
    gantBuilder.setMessageOutputLevel ( )
    assertEquals ( GantState.VERBOSE , GantState.verbosity )
  }
  void testGroovycTaskFail ( ) {
    //
    //  This test can only be guaranteed to work if JUnit is operating in perTest fork mode since otherwise
    //  another test may have caused the Groovyc task to be loaded which leads to a 0 return value.
    //
    def path = '/tmp/tmp/tmp/tmp'
    script = """
target ( hello : '' ) {
  groovyc ( srcdir : '.' , destdir : '${path}' )
}
"""
    assertEquals ( -13 , processCmdLineTargets ( 'hello' ) )
    assertEquals ( ''': Problem: failed to create task or type groovyc
Cause: The name is undefined.
Action: Check the spelling.
Action: Check that any custom tasks/types have been declared.
Action: Check that any <presetdef>/<macrodef> declarations have taken place.

''' , output )
  }
}
