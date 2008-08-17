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

import org.codehaus.gant.GantBinding
import org.codehaus.gant.GantBuilder
import org.codehaus.gant.IncludeTargets
import org.codehaus.gant.IncludeTool

/**
 *  A test for the <code>GantBinding</code> class.
 *
 *  @author Russel Winder <russel.winder@concertant.com>
 */
final class GantBinding_Test extends GantTestCase {
  void testCreate ( ) {
    def object = new GantBinding ( )
    assertTrue ( object.ant instanceof GantBuilder )
    assertTrue ( object.Ant instanceof GantBuilder )
    assertTrue ( object.includeTargets instanceof IncludeTargets )
    assertTrue ( object.includeTool instanceof IncludeTool )
    assertTrue ( object.target instanceof Closure )
    assertTrue ( object.targetDescriptions instanceof TreeMap )
    assertTrue ( object.message instanceof Closure )
    assertTrue ( object.setDefaultTarget instanceof Closure )
    assertTrue ( object.cacheEnabled instanceof Boolean )
    assertTrue ( object.gantLib instanceof List )
  }
  void testGantBindingIsActuallyUsedOutsideTarget ( ) {
    script = '''
assert binding instanceof org.codehaus.gant.GantBinding
target ( testBindingObject : '' ) {
}
'''
    assertEquals ( 0 , processTargets ( 'testBindingObject' ) )
    assertEquals ( '' , output )
  }
  void testGantBindingIsActuallyUsedInsideTarget ( ) {
    script = '''
target ( testBindingObject : '' ) {
  assert binding instanceof org.codehaus.gant.GantBinding
}
'''
    assertEquals ( 0 , processTargets ( 'testBindingObject' ) )
    assertEquals ( '' , output )
  }
  void testAntPropertyAccessAsAntPropertyOutsideTarget ( ) {
    script = '''
assert ant.project.properties.'java.vm.specification.version' == '1.0'
target ( antProperty : '' ) {
}
'''
    assertEquals ( 0 , processTargets ( 'antProperty' ) )
    assertEquals ( '' , output )
  }
  void testAntPropertyAccessAsAntPropertyInsideTarget ( ) {
    script = '''
target ( antProperty : '' ) {
  assert ant.project.properties.'java.vm.specification.version' == '1.0'
}
'''
    assertEquals ( 0 , processTargets ( 'antProperty' ) )
    assertEquals ( '' , output )
  }
  void testAntPropertyAccessAsBindingVariableOutsideTarget ( ) {
    script = '''
assert binding.'java.vm.specification.version' == '1.0'
target ( antProperty : '' ) {
}
'''
    assertEquals ( 0 , processTargets ( 'antProperty' ) )
    assertEquals ( '' , output )
  }
  void testAntPropertyAccessAsBindingVariableInsideTarget ( ) {
    script = '''
target ( antProperty : '' ) {
  assert binding.'java.vm.specification.version' == '1.0'
}
'''
    assertEquals ( 0 , processTargets ( 'antProperty' ) )
    assertEquals ( '' , output )
  }
  void testAntPropertyAccessViaObjectSpecifierOutsideTarget ( ) {
    script = '''
assert this.'java.vm.specification.version' == '1.0'
target ( antProperty : '' ) {
}
'''
    assertEquals ( 0 , processTargets ( 'antProperty' ) )
    assertEquals ( '' , output )
  }
  void testAntPropertyAccessViaObjectSpecifierInsideTarget ( ) {
    script = '''
target ( antProperty : '' ) {
  assert this.'java.vm.specification.version' == '1.0'
  assert owner.'java.vm.specification.version' == '1.0'
  assert delegate.'java.vm.specification.version' == '1.0'
}
'''
    assertEquals ( 0 , processTargets ( 'antProperty' ) )
    assertEquals ( '' , output )
  }

  //  The following two tests behave differently depending on the forkmode of the junit task: it works if
  //  using perTest mode but fails with perBatch or once mode.  So why does the property call work for
  //  perTest and not otherwise?

  void testPropertySettingWorksAsExpectedOutsideTarget ( ) {
    script = '''
final name = 'flobadob'
final value = 'burble'
assert null == ant.project.properties."${name}"
ant.property ( name : name , value : value )
assert value == ant.project.properties."${name}"
assert value == binding."${name}"
assert value == this."${name}"
target ( antProperty : '' ) {
}
'''
    assertEquals ( 0 , processTargets ( 'antProperty' ) )
    assertEquals ( '' , output )
  }
  void testPropertySettingWorksAsExpectedInTarget ( ) {
    script = '''
target ( antProperty : '' ) {
  final name = 'flobadob'
  final value = 'burble'
  assert null == ant.project.properties."${name}"
  property ( name : name , value : value )
  assert value == ant.project.properties."${name}"
  assert value == binding."${name}"
  assert value == this."${name}"
  assert value == owner."${name}"
  assert value == delegate."${name}"
}
'''
    assertEquals ( 0 , processTargets ( 'antProperty' ) )
    assertEquals ( '' , output )
  }
}
