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
 *  A test to ensure that the target listing works. 
 *
 *  @author Russel Winder <russel.winder@concertant.com>
 */
final class ToolMetaClassLookup_Test extends GantTestCase {
  void setUp ( ) {
    super.setUp ( )
    script = '''
includeTool << gant.tools.Subdirectories
target ( something : 'Do something.' ) { Subdirectories.runSubprocess ( "echo yes" , new File ( "src" ) ) }
target ( "default" : "something" ) { something ( ) }
''' 
  }
  void testDefault ( ) {
    assertEquals ( 0 , processTargets ( ) )
    assertEquals ( 'yes\n' , output ) 
  }
  void testBlah ( ) {
    assertEquals ( 11 , processTargets ( 'blah') )
    assertEquals ( 'Target blah does not exist.\n' , output ) 
  }
  void testSomething ( ) {
    assertEquals ( 0 , processTargets ( 'something') )
    assertEquals ( 'yes\n' , output ) 
  }
}
