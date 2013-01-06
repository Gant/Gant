//  Gant -- A Groovy way of scripting Ant tasks.
//
//  Copyright © 2008--2011, 2013  Russel Winder
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
 *  @author Russel Winder <russel@winder.org.uk>
 */
final class GantBinding_Test extends GantTestCase {
  final targetName = 'targetName'
  final propertyToCheck = 'java.vm.specification.version'
  final propertyValue = System.getProperty(propertyToCheck)
  void testCreate() {
    def object = new GantBinding()
    assertTrue(object.ant instanceof GantBuilder)
    assertTrue(object.includeTargets instanceof IncludeTargets)
    assertTrue(object.includeTool instanceof IncludeTool)
    assertTrue(object.target instanceof Closure)
    assertTrue(object.targetDescriptions instanceof TreeMap)
    assertTrue(object.message instanceof Closure)
    assertTrue(object.setDefaultTarget instanceof Closure)
    assertTrue(object.cacheEnabled instanceof Boolean)
    assertTrue(object.gantLib instanceof List)
  }
  void testGantBindingIsActuallyUsedOutsideTarget() {
    script = """
assert binding instanceof org.codehaus.gant.GantBinding
target(${targetName}: '') { }
"""
    assertEquals(0, processCmdLineTargets(targetName))
    assertEquals(resultString(targetName, ''), output)
    assertEquals('', error)
  }
  void testGantBindingIsActuallyUsedInsideTarget() {
    script = """
target(${targetName}: '') {
  assert binding instanceof org.codehaus.gant.GantBinding
}
"""
    assertEquals(0, processCmdLineTargets(targetName))
    assertEquals(resultString(targetName, ''), output)
    assertEquals('', error)
  }
  void testAntPropertyAccessAsAntPropertyOutsideTarget() {
    script = """
assert ant.project.properties.'${propertyToCheck}' == '${propertyValue}'
target(${targetName}: '') { }
"""
    assertEquals(0, processCmdLineTargets(targetName))
    assertEquals(resultString(targetName, ''), output)
    assertEquals('', error)
  }
  void testAntPropertyAccessAsAntPropertyInsideTarget() {
    script = """
target(${targetName}: '') {
  assert ant.project.properties.'${propertyToCheck}' == '${propertyValue}'
}
"""
    assertEquals(0, processCmdLineTargets(targetName))
    assertEquals(resultString(targetName, ''), output)
    assertEquals('', error)
  }
  void testAntPropertyAccessAsBindingVariableOutsideTarget() {
    script = """
assert binding.'${propertyToCheck}' == '${propertyValue}'
target(${targetName}: '') { }
"""
    assertEquals(0, processCmdLineTargets(targetName))
    assertEquals(resultString(targetName, ''), output)
    assertEquals('', error)
  }
  void testAntPropertyAccessAsBindingVariableInsideTarget() {
    script = """
target(${targetName}: '') {
  assert binding.'${propertyToCheck}' == '${propertyValue}'
}
"""
    assertEquals(0, processCmdLineTargets(targetName))
    assertEquals(resultString(targetName, ''), output)
    assertEquals('', error)
  }
  void testAntPropertyAccessViaObjectSpecifierOutsideTarget() {
    script = """
assert this.'${propertyToCheck}' == '${propertyValue}'
target(${targetName}: '') { }
"""
    assertEquals(0, processCmdLineTargets(targetName))
    assertEquals(resultString(targetName, ''), output)
    assertEquals('', error)
  }
  void testAntPropertyAccessViaObjectSpecifierInsideTarget() {
    script = """
target(${targetName}: '') {
  assert this.'${propertyToCheck}' == '${propertyValue}'
  assert owner.'${propertyToCheck}' == '${propertyValue}'
  assert delegate.'${propertyToCheck}' == '${propertyValue}'
}
"""
    assertEquals(0, processCmdLineTargets(targetName))
    assertEquals(resultString(targetName, ''), output)
    assertEquals('', error)
  }
  void testPropertySettingWorksAsExpectedOutsideTarget() {
    script = """
final name = 'flobadob'
final value = 'burble'
assert null == ant.project.properties."\${name}"
ant.property(name: name, value: value)
assert value == ant.project.properties."\${name}"
assert value == binding."\${name}"
assert value == this."\${name}"
target(${targetName}: '') {
}
"""
    assertEquals(0, processCmdLineTargets(targetName))
    assertEquals(resultString(targetName, ''), output)
    assertEquals('', error)
  }
  void testPropertySettingWorksAsExpectedInTarget() {
    script = """
target(${targetName}: '') {
  final name = 'flobadob'
  final value = 'burble'
  assert null == ant.project.properties."\${name}"
  property(name: name, value: value)
  assert value == ant.project.properties."\${name}"
  assert value == binding."\${name}"
  assert value == this."\${name}"
  assert value == owner."\${name}"
  assert value == delegate."\${name}"
}
"""
    assertEquals(0, processCmdLineTargets(targetName))
    assertEquals(resultString(targetName, ''), output)
    assertEquals('', error)
  }
  void testPropertyAccessInsideCategory() {
    final message = 'Hello World'
    script = """
target(${targetName}: '') {
  use(groovy.xml.dom.DOMCategory) { println("${message}") }
}
"""
    assertEquals(0, processTargets(targetName))
    assertEquals(resultString(targetName, message + '\n'), output)
    assertEquals('', error)
  }

  /*
   *  Need a separate test method for each read-only attribute to ensure correct processing of the script.
   *  It is a pity we cannot synthesize the methods as would be possible in interpreted Ruby.
   */
  private void undertakeTestingOfAReadOnlyEntryInBinding(String name) {
    script = """
${name} = null
target('default': '') { println('This should never be printed.') }
"""
    assertEquals(-4, processCmdLineTargets())
    assertEquals('', output)
    assertEquals("Standard input, line 2 -- Error evaluating Gantfile: Cannot redefine symbol ${name}\n", error)
  }
  void testAttemptToAlterReadOnlyBindingEntriesCausesException_target() { undertakeTestingOfAReadOnlyEntryInBinding('target') }
  void testAttemptToAlterReadOnlyBindingEntriesCausesException_message() { undertakeTestingOfAReadOnlyEntryInBinding('message') }
  void testAttemptToAlterReadOnlyBindingEntriesCausesException_ant() { undertakeTestingOfAReadOnlyEntryInBinding('ant') }
  void testAttemptToAlterReadOnlyBindingEntriesCausesException_includeTargets() { undertakeTestingOfAReadOnlyEntryInBinding('includeTargets') }
  void testAttemptToAlterReadOnlyBindingEntriesCausesException_includeTool() { undertakeTestingOfAReadOnlyEntryInBinding('includeTool') }
  void testAttemptToAlterReadOnlyBindingEntriesCausesException_targetDescriptions() { undertakeTestingOfAReadOnlyEntryInBinding('targetDescriptions') }
  void testAttemptToAlterReadOnlyBindingEntriesCausesException_setDefaultTarget() { undertakeTestingOfAReadOnlyEntryInBinding('setDefaultTarget') }
  void testAttemptToAlterReadOnlyBindingEntriesCausesException_initiatimgTarget() { undertakeTestingOfAReadOnlyEntryInBinding('initiatingTarget') }
  void testAttemptToAlterReadOnlyBindingEntriesCausesException_targets() { undertakeTestingOfAReadOnlyEntryInBinding('targets') }

  //  GANT-75 called for adding the properties gant.file and gant.version.

  void testGantFilePropertyIsAccessble() {
    script = "target(${targetName}: '') { println(binding.'gant.file') }"
    assertEquals(0, processTargets(targetName))
    assertEquals(resultString(targetName, '<stream>\n'), output)
    assertEquals('', error)
  }
  void testGantVersionPropertyIsAccessible() {
    script = "target(${targetName}: '') { println(binding.'gant.version') }"
    assertEquals(0, processTargets(targetName))
    assertEquals(resultString(targetName, gant.binding.'gant.version' + '\n'), output)
    assertEquals('', error)
  }

  //  GANT-117 requires functions to be able to set or add to the per-target pre- and post-hooks.

  def baseScript = '''
target('one': '') { }
target('two': '') { }
'''

  void testSetAllPerTargetPreHooks() {
    script = baseScript + '''
setAllPerTargetPreHooks({ -> println 'XXXX' })
'''
    assertEquals(0, processTargets('one'))
    assertEquals("XXXX\n${exitMarker}one\n", output)
    assertEquals('', error)
    assertEquals(0, processTargets('two'))
    assertEquals("XXXX\n${exitMarker}one\nXXXX\n${exitMarker}two\n", output)
    assertEquals('', error)
  }

  void testSetAllPerTargetPostHooks() {
      script = baseScript + '''
setAllPerTargetPostHooks({ -> println 'XXXX' })
'''
      assertEquals(0, processTargets('one'))
      assertEquals('one:\nXXXX\n', output)
      assertEquals('', error)
      assertEquals(0, processTargets('two'))
      assertEquals('one:\nXXXX\ntwo:\nXXXX\n', output)
      assertEquals('', error)
    }

  void testAddAllPerTargetPreHooks() {
      script = baseScript + '''
addAllPerTargetPreHooks({ -> println 'XXXX' })
'''
      assertEquals(0, processTargets('one'))
      assertEquals("one:\nXXXX\n${exitMarker}one\n", output)
      assertEquals('', error)
      assertEquals(0, processTargets('two'))
      assertEquals("one:\nXXXX\n${exitMarker}one\ntwo:\nXXXX\n${exitMarker}two\n", output)
      assertEquals('', error)
    }

    void testAddAllPerTargetPostHooks() {
      script = baseScript + '''
addAllPerTargetPostHooks({ -> println 'XXXX' })
'''
      assertEquals(0, processTargets('one'))
      assertEquals("one:\n${exitMarker}one\nXXXX\n", output)
      assertEquals('', error)
      assertEquals(0, processTargets('two'))
      assertEquals("one:\n${exitMarker}one\nXXXX\ntwo:\n${exitMarker}two\nXXXX\n", output)
      assertEquals('', error)
    }

  void testAddPreHookNotAClosure() {
    script = baseScript + '''
addAllPerTargetPreHooks([])
'''
    assertEquals(0, processTargets('one'))
    assertEquals("one:\n${exitMarker}one\n", output)
    assertEquals('Target prehook list item is not a closure.\n', error)
  }

  void testAddPostHookNotAClosure() {
    script = baseScript + '''
  addAllPerTargetPostHooks([])
'''
    assertEquals(0, processTargets('one'))
    assertEquals("one:\n${exitMarker}one\n", output)
    assertEquals('Target posthook list item is not a closure.\n', error)
  }

  //  Some tests stemming from the 1.9.4 regression investigation.

  final regressionInvestigationOutput = 'The Default Target Is Running...'
  final regressionInvestigationDecoratedOutput = "default:\n${regressionInvestigationOutput}\n${exitMarker}default\n"
  final regressionInvestigationScript = """
target(default: 'some default target') {
    println('${regressionInvestigationOutput}')
}
"""
  final regressionInvestigationKillHooks = """
setAllPerTargetPreHooks({ })
setAllPerTargetPostHooks({ })
"""

  void testNotSettingPreAndPostHooksOnDefaultTask() {
    script = regressionInvestigationScript
    assertEquals(0, processTargets())
    assertEquals(regressionInvestigationDecoratedOutput, output)
    assertEquals('', error)
  }

  void testSettingPreAndPostHooksToNothingBeforeDefaultTask() {
    script = regressionInvestigationKillHooks + regressionInvestigationScript
    assertEquals(0, processTargets())
    assertEquals(regressionInvestigationDecoratedOutput, output)
    assertEquals('', error)
  }

  void testSettingPreAndPostHooksToNothingAfterDefaultTask() {
    script = regressionInvestigationScript + regressionInvestigationKillHooks
    assertEquals(0, processTargets())
    assertEquals(regressionInvestigationOutput + '\n', output)
    assertEquals('', error)
  }

}
