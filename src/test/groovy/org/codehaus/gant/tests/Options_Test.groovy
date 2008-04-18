//  Gant -- A Groovy build framework based on scripting Ant tasks.
//
//  Copyright © 2007-8 Russel Winder
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
 *  A test to ensure certain options get processed correctly.
 *
 *  @author Russel Winder <russel.winder@concertant.com>
 */
final class Options_Test extends GantTestCase {
  void testVersion ( ) {
    assertEquals ( 0 , gant.processArgs ( [ '-V' ] as String[] ) )
    //  It appears that during test, the manifest version number is not actually found so we get <unknown>
    //  returned as the version number.
    assertEquals ( 'Gant version <unknown>' , output.trim ( ) )
  }
  void testDefinitions ( ) {
    script = '''
target ( printDefinitions : "Print some definitions" ) {
  println ( first )
  println ( second )
  println ( third )
}'''
    assertEquals ( 0 , gant.processArgs ( [ '-f' , '-' , '-Dfirst=tsrif' , '-Dsecond=dnoces' , '-Dthird=driht' , 'printDefinitions' ] as String[] ) )
    assertEquals ( '''tsrif
dnoces
driht
''' , output )
  }
  void testFileOptionLong ( ) {
    script = 'target ( test : "Test entry" ) { println ( "Hello." ) }'
    assertEquals ( 0 , gant.processArgs ( [ '--file' , '-' , 'test' ] as String[] ) )
    assertEquals ( '''Hello.
''' , output )
  }
}
