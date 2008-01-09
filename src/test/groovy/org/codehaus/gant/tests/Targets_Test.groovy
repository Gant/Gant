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
 *  A test to ensure that the target specification works. 
 *
 *  @author Russel Winder <russel.winder@concertant.com>
 */
final class Targets_Test extends GantTestCase {
  final result = 'OK.'
   /*
  void testStringParameter ( ) {
    System.setIn ( new StringBufferInputStream ( "target ( 'string' ) { print ( '${result}' ) }" ) )
    assertEquals ( 2 , gant.process ( [ '-f' ,  '-' , 'string' ] as String[] ) )
    assertEquals ( result , output ) 
  }
   */
  void testNoDescription ( ) {
    System.setIn ( new StringBufferInputStream ( "target ( noDescription : '' ) { print ( '${result}' ) }" ) )
    assertEquals ( 0 , gant.process ( [ '-f' ,  '-' , 'noDescription' ] as String[] ) )
    assertEquals ( result , output ) 
  }
  void testWithDescription ( ) {
    System.setIn ( new StringBufferInputStream ( "target ( withDescription : 'Blah blah' ) { print ( '${result}' ) }" ) )
    assertEquals ( 0 , gant.process ( [ '-f' ,  '-' , 'withDescription' ] as String[] ) )
    assertEquals ( result , output ) 
  }
  void testEmptyMap ( ) {
    System.setIn ( new StringBufferInputStream ( "target ( [ : ] ) { print ( '${result}' ) }" ) )
    assertEquals ( 2 , gant.process ( [ '-f' ,  '-' , 'withDescription' ] as String[] ) )
    assertEquals ( 'Standard input, line 1 -- Error evaluating Gantfile: Target specified without a name.\n' , output ) 
  }
  void testMultipleEntries ( ) {
    System.setIn ( new StringBufferInputStream ( "target ( fred : '' , debbie : '' ) { print ( '${result}' ) }" ) )
    assertEquals ( 2 , gant.process ( [ '-f' ,  '-' , 'withDescription' ] as String[] ) )
    assertEquals ( 'Standard input, line 1 -- Error evaluating Gantfile: Target specified with multiple names.\n' , output ) 
  }
}
