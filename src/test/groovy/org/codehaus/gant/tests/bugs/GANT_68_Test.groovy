//  Gant -- A Groovy way of scripting Ant tasks.
//
//  Copyright © 2008-9 Russel Winder
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
  void testGetReasonableErrorMessageForMissingDestination ( ) {
    //  Use a preexisting directory as the source directory and make sure the build directory doesn't exist!
    final sourceDirectory = 'src/test/groovy/org/codehaus/gant/tests/bugs'
    final destinationDirectory = 'destinationDirectoryOfSomeObscureNameThatDoesntExist'
    script = """
sourceDirectory = '${sourceDirectory}'
destinationDirectory = '${destinationDirectory}'
target ( compile : '' ) {
  delete ( dir : destinationDirectory )
  javac ( srcdir : sourceDirectory , destdir : destinationDirectory , fork : 'true' , failonerror : 'true' , source : '1.5' , target : '1.5' , debug : 'on' , deprecation : 'on' )
}
"""
    assertEquals ( -13 , processCmdLineTargets ( 'compile' ) )
    assertEquals ( ": destination directory \"${ ( new File ( destinationDirectory ) ).absolutePath }\" does not exist or is not a directory\n" , output )
  }
}
