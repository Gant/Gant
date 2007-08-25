//  Gant -- A Groovy build tool based on scripting Ant tasks
//
//  Copyright © 2006-7 Russel Winder <russel@russel.org.uk>
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

package org.codehaus.groovy.gant.tests

/**
 *  A test to ensure that the target listing works. 
 *
 *  @author Russel Winder <russel@russel.org.uk>
 *  @version $Revision$ $Date$
 */
final class DryRun_Test extends GantTestCase {
  void setUp ( ) {
    super.setUp ( )
    System.setIn ( new StringBufferInputStream ( '''
target ( something : "Do something." ) { Ant.echo ( message : "Did something." ) }
target ( somethingElse : "Do something else." ) { Ant.echo ( message : "Did something else." ) }
''' ) )  }
    
  void testMissingDefault ( ) {
    assertEquals ( 1 , gant.process ( [ '-n' ,  '-f' ,  '-'  ] as String[] ) )
    assertEquals ( 'Target default does not exist.\n' , output.toString ( ) )
  }
  void testMissingNamedTarget ( ) {
    assertEquals ( 1 , gant.process ( [ '-n' ,  '-f' ,  '-'  , 'blah'] as String[] ) )
    assertEquals ( " [property] environment : 'environment'\nTarget blah does not exist.\n" , output.toString ( ) ) 
  }
  void testSomething ( ) {
    assertEquals ( 0 , gant.process ( [ '-n' ,  '-f' ,  '-'  , 'something'] as String[] ) )
    assertEquals ( " [property] environment : 'environment'\n     [echo] message : 'Did something.'\n" , output.toString ( ) ) 
  }
  void testSomethingElse ( ) {
    assertEquals ( 0 , gant.process ( [ '-n' ,  '-f' ,  '-'  , 'somethingElse'] as String[] ) )
    assertEquals ( " [property] environment : 'environment'\n     [echo] message : 'Did something else.'\n" , output.toString ( ) ) 
  }
}
