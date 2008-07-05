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

package org.codehaus.gant.tests.bugs

import org.codehaus.gant.tests.GantTestCase

//  Original provided by Peter Ledbrook.

class GANT_49_Test extends GantTestCase {
  void testBuilderBug() {
    script = '''
import groovy.xml.MarkupBuilder
target ( test : '' ) {
  ( new MarkupBuilder ( ) ).beans {
    resourceHolder ( 'Something 1' )
    container {
      item ( '1' )
      item ( '2' )
      item ( '3' )
    }
  }
  0
}
setDefaultTarget ( 'test' )
'''
    assertEquals ( 0 , processTargets ( ) )
    assertEquals ( '''<beans>
  <resourceHolder>Something 1</resourceHolder>
  <container>
    <item>1</item>
    <item>2</item>
    <item>3</item>
  </container>
</beans>''' , output )
  }
}
