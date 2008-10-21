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
  void testBuilderBug ( ) {
    //
    //  NB Codehaus Bamboo execution is not in a context such that
    //  org.codehaus.groovy.runtime.HandleMetaClass exists since it is running against Groovy 1.5.6 and not
    //  Subversion HEAD.
    //
    script = '''
import groovy.xml.MarkupBuilder
target ( test : '' ) {
  def builder = new MarkupBuilder ( )

  //assert builder.metaClass instanceof org.codehaus.groovy.runtime.HandleMetaClass
  assert this.is ( owner )
  assert this.is ( delegate )
  //assert this.metaClass instanceof org.codehaus.groovy.runtime.HandleMetaClass
  assert binding instanceof org.codehaus.gant.GantBinding
  //assert binding.metaClass instanceof org.codehaus.groovy.runtime.HandleMetaClass

  def outerThis = this

   builder.beans {

    assert outerThis.is ( this )
    assert delegate.is ( builder )
    assert owner instanceof Closure
    assert owner.metaClass instanceof org.codehaus.gant.GantMetaClass

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
    assertEquals ( 0 , processCmdLineTargets ( ) )
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
