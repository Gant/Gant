//  Gant -- A Groovy way of scripting Ant tasks.
//
//  Copyright © 2009–2010, 2013, 2014  Russel Winder
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
//
//  Author: Russel Winder <russel@winder.org.uk>

//  This file contains the individual tests resulting from specific bug reports that do not require their
//  own test class (because there are not a set of tests), and they do not obviously belong in another test
//  class.

package  org.codehaus.gant.tests.bugs

import org.codehaus.gant.tests.GantTestCase

class Assorted_Test extends GantTestCase {
  private final targetName = 'targetName'

  void test_GANT_29_ensureCaseChangeWorks() {
    final result = 'some string or other'
    script = """
import org.codehaus.gant.tests.bugs.subPackage.GANT_29_SampleTool
includeTool << GANT_29_SampleTool
target(${targetName}: '') {
  gANT_29_SampleTool.name = '${result}'
  println(gANT_29_SampleTool.name)
}
setDefaultTarget(${targetName})
"""
    assertEquals(0, processCmdLineTargets(targetName))
    assertEquals(resultString(targetName, result + '\n'), output)
    assertEquals('', error)
  }

  void test_GANT_32_singleFileFailsCorrectly() {
    script = """
target(${targetName}: '') { foo }
def foo { badvariable }
"""
    assertEquals(-2, processCmdLineTargets( targetName))
    assertEquals('', output)
    assertEquals('''Error evaluating Gantfile: startup failed:
standard_input: 3: unexpected token: foo @ line 3, column 5.
   def foo { badvariable }
       ^

1 error
''', error)
  }
  void test_GANT_32_multipleFilesFailsCorrectly() {
    final file = File.createTempFile('gant-', '-GANT_32.groovy')
    file.write("""target(${targetName}: '') { foo }
def foo { badvariable }
""")
    script = "includeTargets << new File('${escapeWindowsPath(file.path)}')"
    try { assertEquals(-4, processCmdLineTargets(targetName)) }
    finally { file.delete() }
    assertEquals('', output)
    assertTrue(error.startsWith('Standard input, line 1 -- Error evaluating Gantfile: org.codehaus.groovy.control.MultipleCompilationErrorsException: startup failed:\n'))
    assertTrue(error.endsWith('''GANT_32.groovy: 2: unexpected token: foo @ line 2, column 5.
   def foo { badvariable }
       ^

1 error
'''))
  }

  void test_GANT_34_originalIvycachePathProblemFixed() {
    script = '''
includeTool << gant.tools.Ivy
target('default': '') {
  ivy.cachepath(organisation: 'commons-lang',
                  module: 'commons-lang',
                  revision: '2.3',
                  pathid: 'clpath',
                  inline: true)
}
'''
    assertEquals(0, processCmdLineTargets())
    //  The output is not tested since it is extensive and it is not clear that it is guaranteed to be the
    //  same on all platforms: it contains the Ivy jar version number and some timings.
    assertEquals('', error)
  }

  void test_GANT_49_builderBug() {
    //
    //  NB Codehaus Bamboo execution is not in a context such that
    //  org.codehaus.groovy.runtime.HandleMetaClass exists since it is running against Groovy 1.5.6 rather
    //  than 1.6 or later.
    //
    script = """
import groovy.xml.MarkupBuilder
target(${targetName}: '') {
  def builder = new MarkupBuilder()

  //assert builder.metaClass instanceof org.codehaus.groovy.runtime.HandleMetaClass
  assert this.is(owner)
  assert this.is(delegate)
  //assert this.metaClass instanceof org.codehaus.groovy.runtime.HandleMetaClass
  assert binding instanceof org.codehaus.gant.GantBinding
  //assert binding.metaClass instanceof org.codehaus.groovy.runtime.HandleMetaClass

  def outerThis = this

   builder.beans {

    assert outerThis.is(this)
    assert delegate.is(builder)
    assert owner instanceof Closure
    assert owner.metaClass instanceof org.codehaus.gant.GantMetaClass

    resourceHolder('Something 1')
    container {
      item('1')
      item('2')
      item('3')
    }
  }
}
setDefaultTarget('${targetName}')
"""
    assertEquals(0, processCmdLineTargets())
    assertEquals(resultString(targetName, '''<beans>
  <resourceHolder>Something 1</resourceHolder>
  <container>
    <item>1</item>
    <item>2</item>
    <item>3</item>
  </container>
</beans>'''), output)
    assertEquals('', error)
  }

  void test_GANT_58_singleFileFailsCorrectly() {
    def file = File.createTempFile('gant_', '_GANT_58_Test.groovy')
    file.write('''
def a = 1
def b = 0
def c = a / b
''')
    script = """
includeTargets << new File('${escapeWindowsPath(file.path)}')
target('default', '') { }
"""
    try {
      assertEquals(-4, processCmdLineTargets())
      assertEquals('', output)
      assertEquals("Standard input, line 2 -- Error evaluating Gantfile: ${file.path}, line 4 -- java.lang.ArithmeticException: Division by zero\n", error)
    }
    finally { file.delete() }
  }

  void test_GANT_63_exceptionFailsCorrectly() {
    script = """
target(${targetName}: '') {
  def f = new File('blahblahblahblahblah')
  println('before')
  f.eachDir { println(it) }
  println('after')
}
setDefaultTarget(${targetName})
"""
    assertEquals(-13, processCmdLineTargets())
    assertEquals("${targetName}:\nbefore\n", output)
    assertTrue(error.startsWith('java.io.FileNotFoundException: '))
    assertTrue(error.endsWith('blahblahblahblahblah\n'))
  }

  void test_GANT_68_getReasonableErrorMessageForMissingDestination() {
    //  Use a preexisting directory as the source directory and make sure the build directory doesn't exist!
    final sourceDirectory = 'src/test/groovy/org/codehaus/gant/tests/bugs'
    final destinationDirectory = 'destinationDirectoryOfSomeObscureNameThatDoesntExist'
    script = """
sourceDirectory = '${sourceDirectory}'
destinationDirectory = '${destinationDirectory}'
target(${targetName}: '') {
  delete(dir: destinationDirectory)
  javac(srcdir: sourceDirectory, destdir: destinationDirectory, fork: 'true', failonerror: 'true', source: '5', target: '5', debug: 'on', deprecation: 'on')
}
"""
    assertEquals(-13, processCmdLineTargets(targetName))
    assertEquals("${targetName}:\n", output)
    assertEquals(": destination directory \"${(new File(destinationDirectory)).absolutePath }\" does not exist or is not a directory\n", error)
  }

  void test_GANT_131_commandLineParsingOfDValuesWithEquals() {
    final targetName = 'someNameOrOther'
    script = """
target(name: '${targetName}') {
  println 'key: ' + key
}
"""
    assertEquals(0, processCmdLineTargets(['-Dkey="xxx=yyy"', targetName]))
    assertEquals(resultString(targetName, 'key: "xxx=yyy"\n'), output)
    assertEquals('', error)
  }
}
