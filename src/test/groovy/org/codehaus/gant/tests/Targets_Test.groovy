//  Gant -- A Groovy build framework based on scripting Ant tasks.
//
//  Copyright © 2006-8 Russel Winder
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
 *  A test to ensure that the target specification works. 
 *
 *  @author Russel Winder <russel.winder@concertant.com>
 */
final class Targets_Test extends GantTestCase {
  final result = 'OK.'
  void testNoDescription ( ) {
    script = "target ( noDescription : '' ) { print ( '${result}' ) }"
    assertEquals ( 0 , processTargets ( 'noDescription' ) )
    assertEquals ( result , output ) 
  }
  void testWithDescription ( ) {
    script = "target ( withDescription : 'Blah blah' ) { print ( '${result}' ) }"
    assertEquals ( 0 , processTargets ( 'withDescription' ) )
    assertEquals ( result , output ) 
  }
  void testEmptyMap ( ) {
    script = "target ( [ : ] ) { print ( '${result}' ) }"
    assertEquals ( -2 , processTargets ( 'withDescription' ) )
    assertEquals ( 'Standard input, line 1 -- Error evaluating Gantfile: Target specified without a name.\n' , output ) 
  }
  void testMultipleEntries ( ) {
    script = "target ( fred : '' , debbie : '' ) { print ( '${result}' ) }"
    assertEquals ( -2 , processTargets ( 'withDescription' ) )
    assertEquals ( 'Standard input, line 1 -- Error evaluating Gantfile: Target specified with multiple names.\n' , output ) 
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
    System.err.println ( 'testOverwriting: This test is wrong -- it shows that target overwriting is supported.' )
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
  void testStringParameter ( ) {
    script = "target ( 'string' ) { print ( '${result}' ) }"
    assertEquals ( -2 , processTargets ( 'string' ) )
    assertTrue ( output.startsWith ( 'Standard input, line 1 -- Error evaluating Gantfile: No signature of method: org.codehaus.gant.GantBinding$_initializeGantBinding_closure1.doCall() is applicable for argument types:' ) ) 
  }
  void testStringSequenceParameter ( ) {
    script = "target ( 'key' , 'description' ) { print ( '${result}' ) }"
    assertEquals ( -2 , processTargets ( 'key' ) )
    assertTrue ( output.startsWith ( 'Standard input, line 1 -- Error evaluating Gantfile: No signature of method: org.codehaus.gant.GantBinding$_initializeGantBinding_closure1.doCall() is applicable for argument types:' ) ) 
  }
}
