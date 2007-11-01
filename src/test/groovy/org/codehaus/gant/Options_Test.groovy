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

package org.codehaus.gant.tests

/**
 *  A test to ensure certain options get processed correctly.
 *
 *  @author Russel Winder <russel@russel.org.uk>
 *  @version $Revision: 6948 $ $Date: 2007-07-17 19:36:41 +0100 (Tue, 17 Jul 2007) $
 */
final class Options_Test extends GantTestCase {
  void testVersion ( ) {
    assertEquals ( 0 , gant.process ( [ '-V' ] as String[] ) )
    //  It appears that during test, the manifest version number is not actually found so we get <unknown>
    //  returned as the version number.
    assertEquals ( 'Gant version <unknown>' , output.toString ( ).trim ( ) )
  }

  ////
  ////  This test fails because Commons CLI is broken.  Multiple options such as this means that the target
  ////  name is absorbed as an option.  This means the target is 'default' which does not exist, thus the
  ////  build fails.
  ////
  void XXX_testDefinitions ( ) {
    System.setIn ( new StringBufferInputStream ( '''
target ( printDefinitions : "Print some definitions" ) {
  println ( first )
  println ( second )
  println ( third )
}''' ) )
    assertEquals ( 0 , gant.process ( [ '-f' , '-' , '-Dfirst=strif' , '-Dsecond=dnoced' , '-Dthird=driht' , 'printDefinitions' ] as String[] ) )
    assertEquals ( '''tsrif
dnoces
driht
''' , output.toString ( ) )
  }
}
