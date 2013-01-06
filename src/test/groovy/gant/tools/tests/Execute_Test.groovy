//  Gant -- A Groovy way of scripting Ant tasks.
//
//  Copyright © 2007–2010, 2013  Russel Winder
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

package gant.tools.tests

import org.codehaus.gant.tests.GantTestCase

/**
 *  A test to ensure that the Execute tool is not broken.
 *
 *  @author Russel Winder <russel@winder.org.uk>
 */
final class Execute_Test extends GantTestCase {
  final targetName = 'testing'
  void testExecutableString() {
    final command = isWindows ? 'cmd /c echo 1': 'echo 1'
    script = """includeTool << gant.tools.Execute
target(${targetName}: '') { execute.executable('${command}') }
"""
    assertEquals(0, processCmdLineTargets(targetName))
    assertEquals(resultString(targetName, '  [execute] ' + command + '\n1\n'), output)
    assertEquals('', error)
  }
  void testExecutableListOfString() {
    //  Format these correctly and they are both input and expected value.
    final command = isWindows ? '["cmd", "/c", "echo", "1"]': '["echo", "1"]'
    final expected = command.replaceAll('"', '')
    script = """includeTool << gant.tools.Execute
target(${targetName}: '') { execute.executable(${command}) }
"""
    assertEquals(0, processCmdLineTargets(targetName))
    assertEquals(resultString(targetName, '  [execute] ' + expected + '\n1\n'), output)
    assertEquals('', error)
  }
  void testShellString() {
    script = """includeTool << gant.tools.Execute
target(${targetName}: '') { execute.shell('echo 1') }
"""
    assertEquals(0, processCmdLineTargets(targetName))
    assertEquals(resultString(targetName, '    [shell] echo 1\n1\n'), output)
    assertEquals('', error)
  }
  void testExecuteReturnCodeCorrect() {
    final command = isWindows ? 'cmd /c echo 1': 'echo 1'
    script = """includeTool << gant.tools.Execute
target(${targetName}: '') { assert execute.executable('${command}') == 0 }
"""
    assertEquals(0, processCmdLineTargets(targetName))
    assertEquals(resultString(targetName, '  [execute] ' + command + '\n1\n'), output)
    assertEquals('', error)
  }
  void testExecuteReturnCodeError() {
    //  TODO:  Find out what can be done in Windows to check this.
    if (! isWindows) {
      script = """includeTool << gant.tools.Execute
target(${targetName}: '') { assert execute.executable('false') == 1 }
"""
      assertEquals(0, processCmdLineTargets(targetName))
      assertEquals(resultString(targetName, '  [execute] false\n'), output)
      assertEquals('', error)
    }
  }
  void testParameterizedUsage() {
    script = """includeTool ** gant.tools.Execute * [ command: 'echo 1' ]
target(${targetName}: '') { execute.shell('echo 1') }
"""
    assertEquals(0, processCmdLineTargets(targetName))
    assertEquals(resultString(targetName, '    [shell] echo 1\n1\n'), output)
    assertEquals('', error)
  }
}
