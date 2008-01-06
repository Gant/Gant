//  Gant -- A Groovy build framework based on scripting Ant tasks.
//
//  Copyright © 2008 Russel Winder
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
 *  A test for accessing the properties in the AntBuilder object.
 *
 *  @author Russel Winder <russel.winder@concertant.com>
 */
final class Environment_Test extends GantTestCase {
  final java5orHigher = System.properties.'java.version'.split ( /\./ )[1] > '4'
  final groovyHome = System.properties.'groovy.home'
  void testGroovyHomePropertySet ( ) { assertNotNull ( System.properties.'groovy.home' ) }
  void testConsistencyIfPossible ( ) {
    if ( java5orHigher ) { assertEquals ( groovyHome , System.getenv ( ).'GROOVY_HOME' ) }
  }
  void testAntPropertiesSet ( ) {
    System.setIn ( new StringBufferInputStream ( '''
target ( report : '' ) {
   println ( System.properties.'groovy.home'.equals ( Ant.project.properties.'environment.GROOVY_HOME' ) ? 'true' : 'false' )
}
''' ) )
    assertEquals ( 0 , gant.process ( [ '-f' , '-' , 'report' ] as String[] ) )
    assertEquals ( 'true\n' , output )
  }
  void testAntEnvironmentGroovyHome ( ) {
    System.setIn ( new StringBufferInputStream ( '''
target ( report : '' ) { print ( Ant.project.properties.'environment.GROOVY_HOME' ) }
''' ) )
    assertEquals ( 0 , gant.process ( [ '-f' , '-' , 'report' ] as String[] ) )
    assertEquals ( groovyHome , output )
  }
  void testSystemEnvironmentGroovyHome ( ) {
    if ( java5orHigher ) {
      System.setIn ( new StringBufferInputStream ( '''
target ( report : '' ) { print ( System.getenv ( ).'GROOVY_HOME' ) }
''' ) )
      assertEquals ( 0 , gant.process ( [ '-f' , '-' , 'report' ] as String[] ) )
      assertEquals ( groovyHome , output )
    }
  }
  void testSystemPropertiesGroovyHome ( ) {
    System.setIn ( new StringBufferInputStream ( '''
target ( report : '' ) { print ( System.properties.'groovy.home' ) }
''' ) )
    assertEquals ( 0 , gant.process ( [ '-f' , '-' , 'report' ] as String[] ) )
    assertEquals ( groovyHome , output )
  }
}
