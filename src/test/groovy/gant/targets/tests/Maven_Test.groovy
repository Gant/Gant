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

package gant.targets.tests

import org.codehaus.gant.tests.GantTestCase

/**
 *  A test to ensure that the Maven targets are not broken.
 *
 *  @author Russel Winder <russel.winder@concertant.com>
 */
final class Maven_Test extends GantTestCase {
  void testLoadingTargets ( ) {
    System.setIn ( new StringBufferInputStream ( """
includeTargets << gant.targets.Maven
""" ) )
    assertEquals ( 0 , gant.process ( [ '-f' , '-' , 'initialize' ] as String[] ) )
    assertEquals ( '' , output ) 
  }
  void testCompileTarget ( ) {
    System.setIn ( new StringBufferInputStream ( """
includeTargets << gant.targets.Maven
""" ) )
    assertEquals ( 0 , gant.process ( [ '-f' , '-' , 'compile' ] as String[] ) )
    assertEquals ( '''  [groovyc] No sources to compile
''' , output ) 
  }


  void testPackageNoGroupIdLeftShift ( ) {
    System.setIn ( new StringBufferInputStream ( """
includeTargets << gant.targets.Maven
""" ) )
    assertEquals ( 13 , gant.process ( [ '-f' , '-' , 'package' ] as String[] ) )
    assertEquals ( '''Maven.groupId must be set to achieve target package.
''' , output ) 
  }
  void testPackageNoGroupIdPower ( ) {
    System.setIn ( new StringBufferInputStream ( """
includeTargets ** gant.targets.Maven * [ : ]
""" ) )
    assertEquals ( 13 , gant.process ( [ '-f' , '-' , 'package' ] as String[] ) )
    assertEquals ( '''Maven.groupId must be set to achieve target package.
''' , output ) 
  }
  void testPackageNoArtifactIdLeftShift ( ) {
    System.setIn ( new StringBufferInputStream ( """
includeTargets << gant.targets.Maven
Maven.groupId = 'flob'
""" ) )
    assertEquals ( 13 , gant.process ( [ '-f' , '-' , 'package' ] as String[] ) )
    assertEquals ( '''Maven.artifactId must be set to achieve target package.
''' , output ) 
  }
  void testPackageNoArtifactIdPower ( ) {
    System.setIn ( new StringBufferInputStream ( """
includeTargets ** gant.targets.Maven * [ groupId : 'flob' ]
""" ) )
    assertEquals ( 13 , gant.process ( [ '-f' , '-' , 'package' ] as String[] ) )
    assertEquals ( '''Maven.artifactId must be set to achieve target package.
''' , output ) 
  }
  void testPackageVersionLeftShift ( ) {
    System.setIn ( new StringBufferInputStream ( """
includeTargets << gant.targets.Maven
Maven.groupId = 'flob'
Maven.artifactId = 'adob'
""" ) )
    assertEquals ( 13 , gant.process ( [ '-f' , '-' , 'package' ] as String[] ) )
    assertEquals ( '''Maven.version must be set to achieve target package.
''' , output ) 
  }
  void testPackageVersionPower ( ) {
    System.setIn ( new StringBufferInputStream ( """
includeTargets ** gant.targets.Maven * [ groupId : 'flob' , artifactId : 'adob' ]
""" ) )
    assertEquals ( 13 , gant.process ( [ '-f' , '-' , 'package' ] as String[] ) )
    assertEquals ( '''Maven.version must be set to achieve target package.
''' , output ) 
  }








}
