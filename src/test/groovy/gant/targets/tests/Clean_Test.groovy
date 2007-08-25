//  Gant -- A Groovy build tool based on scripting Ant tasks
//
//  Copyright © 2007 Russel Winder <russel@russel.org.uk>
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

package gant.targets.tests

import org.codehaus.groovy.gant.tests.GantTestCase

/**
 *  A test to ensure that the Clean targets are not broken.
 *
 *  @author Russel Winder <russel@russel.org.uk>
 *  @version $Revision$ $Date$
 */
final class Clean_Test extends GantTestCase {
  void testCleanDirectoryString ( ) {
    System.setIn ( new StringBufferInputStream ( """
includeTargets << gant.targets.Clean
cleanDirectory << 'target'
target ( testClean : '' ) {
  println ( cleanDirectory )
}
""" ) )
    assertEquals ( 0 , gant.process ( [ '-f' , '-' , 'testClean' ] as String[] ) )
    assertEquals ( '''["target"]
''' , output.toString ( ) ) 
  }
  void testCleanDirectoryList ( ) {
    System.setIn ( new StringBufferInputStream ( """
includeTargets << gant.targets.Clean
cleanDirectory << [ 'target_a' , 'target_b' ]
target ( testClean : '' ) {
  println ( cleanDirectory )
}
""" ) )
    assertEquals ( 0 , gant.process ( [ '-f' , '-' , 'testClean' ] as String[] ) )
    assertEquals ( '''[["target_a", "target_b"]]
''' , output.toString ( ) ) 
  }
  void testCleanPatternString ( ) {
    System.setIn ( new StringBufferInputStream ( """
includeTargets << gant.targets.Clean
cleanPattern << '**/*~'
target ( testClean : '' ) {
  println ( cleanPattern )
}
""" ) )
    assertEquals ( 0 , gant.process ( [ '-f' , '-' , 'testClean' ] as String[] ) )
    assertEquals ( '''["**/*~"]
''' , output.toString ( ) ) 
  }
  void testCleanPatternList ( ) {
    System.setIn ( new StringBufferInputStream ( """
includeTargets << gant.targets.Clean
cleanPattern << [ '**/*~' , '**/*.bak' ]
target ( testClean : '' ) { println ( cleanPattern ) }
""" ) )
    assertEquals ( 0 , gant.process ( [ '-f' , '-' , 'testClean' ] as String[] ) )
    assertEquals ( '''[["**/*~", "**/*.bak"]]
''' , output.toString ( ) ) 
  }
  void testClobberDirectoryString ( ) {
    System.setIn ( new StringBufferInputStream ( """
includeTargets << gant.targets.Clean
clobberDirectory << 'target'
target ( testClobber : '' ) {
  println ( clobberDirectory )
}
""" ) )
    assertEquals ( 0 , gant.process ( [ '-f' , '-' , 'testClobber' ] as String[] ) )
    assertEquals ( '''["target"]
''' , output.toString ( ) ) 
  }
  void testClobberDirectoryList ( ) {
    System.setIn ( new StringBufferInputStream ( """
includeTargets << gant.targets.Clean
clobberDirectory << [ 'target_a' , 'target_b' ]
target ( testClobber : '' ) {
  println ( clobberDirectory )
}
""" ) )
    assertEquals ( 0 , gant.process ( [ '-f' , '-' , 'testClobber' ] as String[] ) )
    assertEquals ( '''[["target_a", "target_b"]]
''' , output.toString ( ) ) 
  }
  void testClobberPatternString ( ) {
    System.setIn ( new StringBufferInputStream ( """
includeTargets << gant.targets.Clean
clobberPattern << '**/*~'
target ( testClobber : '' ) {
  println ( clobberPattern )
}
""" ) )
    assertEquals ( 0 , gant.process ( [ '-f' , '-' , 'testClobber' ] as String[] ) )
    assertEquals ( '''["**/*~"]
''' , output.toString ( ) ) 
  }
  void testClobberPatternList ( ) {
    System.setIn ( new StringBufferInputStream ( """
includeTargets << gant.targets.Clean
clobberPattern << [ '**/*~' , '**/*.bak' ]
target ( testClobber : '' ) { println ( clobberPattern ) }
""" ) )
    assertEquals ( 0 , gant.process ( [ '-f' , '-' , 'testClobber' ] as String[] ) )
    assertEquals ( '''[["**/*~", "**/*.bak"]]
''' , output.toString ( ) ) 
  }
}
