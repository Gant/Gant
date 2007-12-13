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
 *  A test to ensure that the targets method lookup works. 
 *
 *  @author Russel Winder <russel.winder@concertant.com>
 */
final class TargetMetaClassLookup_Test extends GantTestCase {
  void setUp ( ) {
    super.setUp ( )
    System.setIn ( new StringBufferInputStream ( '''
includeTargets << gant.targets.Clean
cleanPattern << "**/*~"
target ( something : "Do something." ) { Ant.echo ( message : "Did something." ) }
setdefault ( something )
''' ) )  }
    
  //  It seems that the same gant.targets.Clean instance is used for all tests in this class whuich is a bit
  //  sad becaus it means that there is an accumulatiopn of **/*~ patterns, 1 for each test method as
  //  addCleanPattern gets executed for each test.  So it is crucial to know when testClean is run to know
  //  what the output will be.  Put it first in the hope it will be run first.

  void testClean ( ) {
    //  Have to do this dry run or the result is indeterminate.
    //assertEquals ( 0 , gant.process ( [ '-n' , '-f' ,  '-'  , 'clean' ] as String[] ) )
 gant.process ( [ '-n' , '-f' ,  '-'  , 'clean' ] as String[] )
    assertEquals ( '''   [delete] quiet : 'false'
  [fileset] dir : '.' , includes : '**/*~' , defaultexcludes : 'false'
''' , output )
  }
  void testDefault ( ) {
    assertEquals ( 0 , gant.process ( [ '-f' ,  '-'  ] as String[] ) )
    assertEquals ( " [property] environment : 'environment'\n     [echo] message : 'Did something.'\n" , output ) 
  }
  void testBlah ( ) {
    assertEquals ( 11 , gant.process ( [ '-f' ,  '-'  , 'blah' ] as String[] ) )
    assertEquals ( " [property] environment : 'environment'\nTarget blah does not exist.\n" , output ) 
  }
  void testSomething ( ) {
    assertEquals ( 0 , gant.process ( [ '-f' ,  '-'  , 'something' ] as String[] ) )
    assertEquals ( " [property] environment : 'environment'\n     [echo] message : 'Did something.'\n" , output ) 
  }
}
