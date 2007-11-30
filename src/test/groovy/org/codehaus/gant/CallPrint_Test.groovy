//  Gant -- A Groovy build framework based on scripting Ant tasks.
//
//  Copyright © 2006-7 Russel Winder
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
 *  @author Russel Winder <russel.winder@concertant.com>
 */
final class CallPrint_Test extends GantTestCase {
  void testSystemOutPrintln ( ) {
    System.setIn ( new StringBufferInputStream ( '''
target ( systemOutPrintln : "Do something." ) { System.out.println ( "Hello World" ) }
''' ) )
    assertEquals ( 0 , gant.process ( [ '-f' , '-' , 'systemOutPrintln' ] as String[] ) )
    assertEquals ( '''Hello World
''' , output.toString ( ) ) 
  }
  void testPrintln ( ) {
    System.setIn ( new StringBufferInputStream ( '''
target ( testPrintln : "Do something." ) { println ( "Hello World" ) }
''' ) )
    assertEquals ( 0 , gant.process ( [ '-f' , '-' , 'testPrintln' ] as String[] ) )
    assertEquals ( '''Hello World
''' , output.toString ( ) ) 
  }
  void testMessage ( ) {
    System.setIn ( new StringBufferInputStream ( '''
target ( testMessage : "Do something." ) { message ( 'message' , 'A message.' ) }
''' ) )
    assertEquals ( 0 , gant.process ( [ '-f' , '-' , 'testMessage' ] as String[] ) )
    assertEquals ( '  [message] A message.\n' , output.toString ( ) ) 
  }
}
