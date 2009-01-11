//  Gant -- A Groovy way of scripting Ant tasks.
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

package org.codehaus.gant.tests.bugs

import org.codehaus.gant.tests.GantTestCase

class GANT_68_Test extends GantTestCase {
  void testGetReasonableErrorMessage ( ) {
    //  Use a preexisting directory!
    final sourceDirectory = 'src/test/groovy/org/codehaus/gant/tests/bugs'
    final buildDirectory = 'buildDirectoryOfSomeObscureNameThatDoesntExist'
    script = """
sourceDirectory = '${sourceDirectory}'
buildDirectory = '${buildDirectory}'
target ( compile : '' ) {
  delete ( dir : buildDirectory )
  javac ( srcdir : sourceDirectory , destdir : buildDirectory , fork : 'true' , failonerror : 'true' , source : '1.5' , target : '1.5' , debug : 'on' , deprecation : 'on' )
}
"""
    assertEquals ( -13 , processCmdLineTargets ( 'compile' ) )
    //  TODO :  This currently shows the presence of the bug
    assertEquals ( "groovy.lang.MissingMethodException: No signature of method: standard_input.javac() is applicable for argument types: (java.util.LinkedHashMap) values: [[srcdir:${sourceDirectory}, destdir:${buildDirectory}, fork:true, failonerror:true, source:1.5, target:1.5, debug:on, deprecation:on]]\n" , output )
  }
}
