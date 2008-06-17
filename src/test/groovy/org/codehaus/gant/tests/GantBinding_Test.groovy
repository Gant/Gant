//  Gant -- A Groovy build framework based on scripting Ant tasks.
//
//  Copyright © 2008 Russel Winder
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

import org.codehaus.gant.GantBinding
import org.codehaus.gant.GantBuilder
import org.codehaus.gant.IncludeTargets
import org.codehaus.gant.IncludeTool

/**
 *  A test for the <code>GantBinding</code> class.
 *
 *  @author Russel Winder <russel.winder@concertant.com>
 */
final class GantBinding_Test extends GantTestCase {
  void testCreate ( ) {
    def object = new GantBinding ( )
    assertTrue ( object.ant instanceof GantBuilder )
    assertTrue ( object.Ant instanceof GantBuilder )
    assertTrue ( object.includeTargets instanceof IncludeTargets )
    assertTrue ( object.includeTool instanceof IncludeTool )
    assertTrue ( object.target instanceof Closure )
    assertTrue ( object.targetDescriptions instanceof TreeMap )
    assertTrue ( object.message instanceof Closure )
    assertTrue ( object.setDefaultTarget instanceof Closure )
    assertTrue ( object.cacheEnabled instanceof Boolean )
    assertTrue ( object.gantLib instanceof List )
  }
  void testOverwriting ( ) {
    //
    //  Until changed, creating a new symbol in the binding using a target overwrites the old symbol.  This
    //  is clearly wrong behaviour and needs amending.
    //
    script = '''
target ( hello : '' ) { println ( 'Hello 1' ) }
target ( hello : '' ) { println ( 'Hello 2' ) }
'''
    System.err.println ( 'testOverwriting: This test is wrong.' )
    assertEquals ( 0 , processTargets ( 'hello' ) )
    assertEquals ( 'Hello 2\n' , output )
  }
  void testForbidRedefinitionOfTarget ( ) {
    script = '''
target ( test : '' ) { }
target = 10
'''
    assertEquals ( -2 , processTargets ( 'test' ) )
    assertEquals ( 'Standard input, line 3 -- Error evaluating Gantfile: Cannot redefine symbol target\n' , output )
  }
}
