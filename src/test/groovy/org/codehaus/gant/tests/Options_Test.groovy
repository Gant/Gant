//  Gant -- A Groovy way of scripting Ant tasks.
//
//  Copyright © 2007-10 Russel Winder
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
 *  @author Russel Winder <russel@russel.org.uk>
 */
final class Options_Test extends GantTestCase {
  private final targetName = 'printDefinitions'
  void testVersion ( ) {
    //  Gant gets its idea of version number from the manifest in the jar.  This means the tests have
  	//  to run against the jar to get a non-null version number -- running the tests against the compiled
  	//  classes will always give null as the version number.  The Gant and Ant builds perform the
  	//  packaging then run the tests, the Gradle, Maven, Eclipse, and IntelliJ IDEA tests occur before the
  	//  packaging.  To avoid getting a test fail with these fiddle with the expectations.
    assertEquals ( 0 , gant.processArgs ( [ '-V' ] as String[] ) )
    assertEquals ( 'Gant version ' + ( gant.binding.'gant.version' == null ? '<unknown>' : gant.binding.'gant.version' ) , output.trim ( ) )
  }
  void testDefinitions ( ) {
    script = """
target ( ${targetName} : '' ) {
  println ( first )
  println ( second )
  println ( third )
}
"""
    assertEquals ( 0 , gant.processArgs ( [ '-f' , '-' , '-Dfirst=tsrif' , '-Dsecond=dnoces' , '-Dthird=driht' , targetName ] as String[] ) )
    assertEquals ( resultString ( targetName , '''tsrif
dnoces
driht
''' ) , output )
  }
  void testFileOptionLong ( ) {
    final message = 'Hello.'
    script = "target ( ${targetName} : '' ) { println ( '${message}' ) }"
    assertEquals ( 0 , gant.processArgs ( [ '--file' , '-' , targetName ] as String[] ) )
    assertEquals ( resultString ( targetName , message + '\n' ) , output )
  }
}
