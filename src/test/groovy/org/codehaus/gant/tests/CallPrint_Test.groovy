//  Gant -- A Groovy way of scripting Ant tasks.
//
//  Copyright © 2006-10 Russel Winder
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
 *  A test to ensure that using standard Groovy functions works.
 *
 *  @author Russel Winder <russel@russel.org.uk>
 */
final class CallPrint_Test extends GantTestCase {
  final outputString = 'Hello World.'
  void testSystemOutPrintln ( ) {
    final targetName = 'systemOutPrintln'
    script = "target ( ${targetName} : '' ) { System.out.println ( '${outputString}' ) }"
    assertEquals ( 0 , processCmdLineTargets ( targetName ) )
    assertEquals ( resultString ( targetName , outputString + '\n' ) , output )
    assertEquals ( '' , error )
  }
  void testPrintln ( ) {
    final targetName = 'testPrintln'
    script = "target ( ${targetName} : '' ) { println ( '${outputString}' ) }"
    assertEquals ( 0 , processCmdLineTargets ( targetName ) )
    assertEquals ( resultString ( targetName , outputString + '\n' ) , output ) 
    assertEquals ( '' , error )
  }
  void testMessage ( ) {
    final targetName = 'testMessage'
    script = "target ( ${targetName} : '' ) { message ( 'message' , '${outputString}' ) }"
    assertEquals ( 0 , processCmdLineTargets ( targetName ) )
    assertEquals ( resultString ( targetName , "  [message] " + outputString + '\n' ) , output ) 
    assertEquals ( '' , error )
  }
}
