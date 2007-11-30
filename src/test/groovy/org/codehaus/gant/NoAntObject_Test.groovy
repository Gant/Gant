//  Gant -- A Groovy build framework based on scripting Ant tasks.
//
//  Copyright © 2007 Russel Winder
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
 *  A test to ensure that calling an Ant task without the Ant object works as required.
 *
 *  @author Russel Winder <russel.winder@concertant.com>
 */
final class NoAntObject_Test extends GantTestCase {
  void testEchoAttribute ( ) {
    System.setIn ( new StringBufferInputStream ( 'target ( test : "" ) { echo ( message : "Hello." ) } ' ) )
    assertEquals ( 0 , gant.process ( [ '-f' ,  '-' , 'test' ] as String[] ) )
    assertEquals ( '''     [echo] Hello.
''' , output.toString ( ) ) 
  }
  void testEchoText ( ) {
    System.setIn ( new StringBufferInputStream ( 'target ( test : "" ) { echo { "Hello." } } ' ) )
    assertEquals ( 0 , gant.process ( [ '-f' ,  '-' , 'test' ] as String[] ) )
    assertEquals ( '' , output.toString ( ) ) 
  }
  void testEchoMixed ( ) {
    System.setIn ( new StringBufferInputStream ( 'target ( test : "" ) { echo ( message : "Hello" ) { " World." } } ' ) )
    assertEquals ( 0 , gant.process ( [ '-f' ,  '-' , 'test' ] as String[] ) )
    assertEquals ( '''     [echo] Hello
''' , output.toString ( ) ) 
  }
}
