//  Gant -- A Groovy way of scripting Ant tasks.
//
//  Copyright © 2006-9 Russel Winder
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
 *  Tests that you can use a custom class loader with Gant.
 *
 *  @author Graeme Rocher
 *  @author Russel Winder
 */
final class CustomClassLoader_Test extends GantTestCase {
  final outputString = 'goodbye'
  final introducer = 'Starting'
  final ender = 'Finished'
  final defaultTargetName = 'default'
  void setUp ( ) {
    super.setUp ( )
    def gcl = new GroovyClassLoader ( )
    gcl.parseClass ( "package helloworld ; class Hello {  def say ( ) { '${outputString}' } }" )
    script = """
target ( '${defaultTargetName}' : '' ) {
	println '${introducer}'
	println new helloworld.Hello ( ).say ( )
	println '${ender}'
}
"""
    gant = new gant.Gant ( null , gcl )
  }
  void testDefault ( ) {
    assertEquals ( 0 , processCmdLineTargets ( ) )
    assertEquals ( resultString ( defaultTargetName , introducer + '\n' + outputString + '\n' + ender + '\n' ) , output )
    assertEquals ( '' , error )
  }
}
