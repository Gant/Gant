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
 *  A test for the <code>GantBuilder</code> class.
 *
 *  @author Russel Winder <russel.winder@concertant.com>
 */
final class GantBuilder_Test extends GantTestCase {
  void testGroovycTaskFail ( ) {
    def path = '/tmp/tmp/tmp/tmp'
    script = """
target ( hello : '' ) {
  groovyc ( srcdir : '.' , destdir : '${path}' )
}
"""
    assertEquals ( -13 , processTargets ( 'hello' ) )
    assertTrue ( output.startsWith ( 'No signature of method: standard_input.groovyc() is applicable for argument types: (java.util.LinkedHashMap) values: ' ) )
  }
}
