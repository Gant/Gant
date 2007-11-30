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
 *  A test to ensure that the target listing works. 
 *
 *  @author Russel Winder <russel.winder@concertant.com>
 */
final class Targets_Test extends GantTestCase {
  final coreScript = '''
target ( something : "Do something." ) { }
target ( somethingElse : "Do something else." ) { }
'''
  void testSomething ( ) {
    System.setIn ( new StringBufferInputStream ( coreScript ) )
    assertEquals ( 0 , gant.process ( [ '-T' ,  '-f' ,  '-' ] as String[] ) )
    assertEquals ( '''gant something  --  Do something.
gant somethingElse  --  Do something else.
''' , output.toString ( ) ) 
  }
  void testSomethingAndClean ( ) {
    System.setIn ( new StringBufferInputStream ( 'includeTargets << new File ( "src/main/java/gant/targets/clean.gant" )\n' + coreScript ) )
    assertEquals ( 0 , gant.process ( [ '-T' ,  '-f' ,  '-' ] as String[] ) )
    assertEquals ( '''gant clean  --  Action the cleaning.
gant clobber  --  Action the clobbering.  Do the cleaning first.
gant something  --  Do something.
gant somethingElse  --  Do something else.
''' , output.toString ( ) ) 
  }
  void testGStrings ( ) {
    System.setIn ( new StringBufferInputStream ( '''
def theWord = 'The Word'
target ( something : "Do ${theWord}." ) { }
target ( somethingElse : "Do ${theWord}." ) { }
''' ) )
    assertEquals ( 0 , gant.process ( [ '-T' ,  '-f' ,  '-' ] as String[] ) )
    assertEquals ( '''gant something  --  Do The Word.
gant somethingElse  --  Do The Word.
''' , output.toString ( ) ) 
  }
  void testDefaultSomething ( ) {
    System.setIn ( new StringBufferInputStream ( '''
target ( something : "Do something." ) { }
target ( somethingElse : "Do something else." ) { }
target ( 'default' : 'Default is something.' ) { something ( ) }
''' ) )
    assertEquals ( 0 , gant.process ( [ '-T' ,  '-f' ,  '-' ] as String[] ) )
    assertEquals ( '''gant -- Default is something.
gant something  --  Do something.
gant somethingElse  --  Do something else.
''' , output.toString ( ) ) 
  }  
}
