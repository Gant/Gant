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
  final targetName = 'targetName'
  void testCreate ( ) {
    def object = new GantBinding ( )
    assertTrue ( object.ant instanceof GantBuilder )
    assertTrue ( object.includeTargets instanceof IncludeTargets )
    assertTrue ( object.includeTool instanceof IncludeTool )
    assertTrue ( object.target instanceof Closure )
    assertTrue ( object.targetDescriptions instanceof TreeMap )
    assertTrue ( object.message instanceof Closure )
    assertTrue ( object.setDefaultTarget instanceof Closure )
    assertTrue ( object.cacheEnabled instanceof Boolean )
    assertTrue ( object.gantLib instanceof List )
  }
  void testAntReferenceProperlyDeprecated ( ) {
    final object = new GantBinding ( )
    assertTrue ( object.Ant instanceof GantBuilder )
    final message = 'hello.' 
    script = """
target ( ${targetName} : '' ) {
  Ant.echo ( message : '${message}' )
}
"""
    assertEquals ( 0 , processCmdLineTargets ( targetName ) )
    assertEquals ( resultString ( targetName , "     [echo] ${message}\n" ) , output )
  }
  void testGantBindingIsActuallyUsedOutsideTarget ( ) {
    script = """
assert binding instanceof org.codehaus.gant.GantBinding
target ( ${targetName} : '' ) { }
"""
    assertEquals ( 0 , processCmdLineTargets ( targetName ) )
    assertEquals ( resultString ( targetName , '' ) , output )
  }
  void testGantBindingIsActuallyUsedInsideTarget ( ) {
    script = """
target ( ${targetName} : '' ) {
  assert binding instanceof org.codehaus.gant.GantBinding
}
"""
    assertEquals ( 0 , processCmdLineTargets ( targetName ) )
    assertEquals ( resultString ( targetName , '' ) , output )
  }
  void testAntPropertyAccessAsAntPropertyOutsideTarget ( ) {
    script = """
assert ant.project.properties.'java.vm.specification.version' == '1.0'
target ( ${targetName} : '' ) { }
"""
    assertEquals ( 0 , processCmdLineTargets ( targetName ) )
    assertEquals ( resultString ( targetName , '' ) , output )
  }
  void testAntPropertyAccessAsAntPropertyInsideTarget ( ) {
    script = """
target ( ${targetName} : '' ) {
  assert ant.project.properties.'java.vm.specification.version' == '1.0'
}
"""
    assertEquals ( 0 , processCmdLineTargets ( targetName ) )
    assertEquals ( resultString ( targetName , '' ) , output )
  }
  void testAntPropertyAccessAsBindingVariableOutsideTarget ( ) {
    script = """
assert binding.'java.vm.specification.version' == '1.0'
target ( ${targetName} : '' ) { }
"""
    assertEquals ( 0 , processCmdLineTargets ( targetName ) )
    assertEquals ( resultString ( targetName , '' ) , output )
  }
  void testAntPropertyAccessAsBindingVariableInsideTarget ( ) {
    script = """
target ( ${targetName} : '' ) {
  assert binding.'java.vm.specification.version' == '1.0'
}
"""
    assertEquals ( 0 , processCmdLineTargets ( targetName ) )
    assertEquals ( resultString ( targetName , '' ) , output )
  }
  void testAntPropertyAccessViaObjectSpecifierOutsideTarget ( ) {
    script = """
assert this.'java.vm.specification.version' == '1.0'
target ( ${targetName} : '' ) { }
"""
    assertEquals ( 0 , processCmdLineTargets ( targetName ) )
    assertEquals ( resultString ( targetName , '' ) , output )
  }
  void testAntPropertyAccessViaObjectSpecifierInsideTarget ( ) {
    script = """
target ( ${targetName} : '' ) {
  assert this.'java.vm.specification.version' == '1.0'
  assert owner.'java.vm.specification.version' == '1.0'
  assert delegate.'java.vm.specification.version' == '1.0'
}
"""
    assertEquals ( 0 , processCmdLineTargets ( targetName ) )
    assertEquals ( resultString ( targetName , '' ) , output )
  }
  void testPropertySettingWorksAsExpectedOutsideTarget ( ) {
    script = """
final name = 'flobadob'
final value = 'burble'
assert null == ant.project.properties."\${name}"
ant.property ( name : name , value : value )
assert value == ant.project.properties."\${name}"
assert value == binding."\${name}"
assert value == this."\${name}"
target ( ${targetName} : '' ) {
}
"""
    assertEquals ( 0 , processCmdLineTargets ( targetName ) )
    assertEquals ( resultString ( targetName , '' ) , output )
  }
  void testPropertySettingWorksAsExpectedInTarget ( ) {
    script = """
target ( ${targetName} : '' ) {
  final name = 'flobadob'
  final value = 'burble'
  assert null == ant.project.properties."\${name}"
  property ( name : name , value : value )
  assert value == ant.project.properties."\${name}"
  assert value == binding."\${name}"
  assert value == this."\${name}"
  assert value == owner."\${name}"
  assert value == delegate."\${name}"
}
"""
    assertEquals ( 0 , processCmdLineTargets ( targetName ) )
    assertEquals ( resultString ( targetName , '' ) , output )
  }
  void testPropertyAccessInsideCategory ( ) {
    /*
     *  This tests generates a StackOverflowError with Groovy 1.5.7. The problem is, we have no idea whether
     *  the test is checking for a bug in Gant, or whether it is just making sure that we correctly
     *  workaround a bug in DOMCategory :( We can re-enable this test once the StackOverflowError is fixed
     *  in Groovy and we know that we're not just working around that DOMCategory issue.
     *
     *  See http://jira.codehaus.org/browse/GROOVY-3109
     */
    final message = 'Hello World'
    if ( groovyMinorVersion < 6 ) { return }
    script = """
target ( ${targetName} : '' ) {
  use ( groovy.xml.dom.DOMCategory ) { println ( "${message}" ) }
}
"""
    assertEquals ( 0 , processTargets ( targetName ) )
    assertEquals ( resultString ( targetName , message + '\n' ) , output )
  }

  /*
   *  Need a separate test method for each read-only attribute to ensure correct processing of the script.
   *  It is a pity we cannot synthesize the methods as would be possible in interpreted Ruby.
   */
  private void undertakeTestingOfAReadOnlyEntryInBinding ( String name ) {
    script = """
${name} = null
target ( 'default' : '' ) { println ( 'This should never be printed.' ) }
"""
    assertEquals ( -4 , processCmdLineTargets ( ) )
    assertEquals ( "Standard input, line 2 -- Error evaluating Gantfile: Cannot redefine symbol ${name}\n" , output )
  }
  void testAttemptToAlterReadOnlyBindingEntriesCausesException_target ( ) { undertakeTestingOfAReadOnlyEntryInBinding ( 'target' ) }
  void testAttemptToAlterReadOnlyBindingEntriesCausesException_message ( ) { undertakeTestingOfAReadOnlyEntryInBinding ( 'message' ) }
  void testAttemptToAlterReadOnlyBindingEntriesCausesException_ant ( ) { undertakeTestingOfAReadOnlyEntryInBinding ( 'ant' ) }
  void testAttemptToAlterReadOnlyBindingEntriesCausesException_includeTargets ( ) { undertakeTestingOfAReadOnlyEntryInBinding ( 'includeTargets' ) }
  void testAttemptToAlterReadOnlyBindingEntriesCausesException_includeTool ( ) { undertakeTestingOfAReadOnlyEntryInBinding ( 'includeTool' ) }
  void testAttemptToAlterReadOnlyBindingEntriesCausesException_targetDescriptions ( ) { undertakeTestingOfAReadOnlyEntryInBinding ( 'targetDescriptions' ) }
  void testAttemptToAlterReadOnlyBindingEntriesCausesException_setDefaultTarget ( ) { undertakeTestingOfAReadOnlyEntryInBinding ( 'setDefaultTarget' ) }
  void testAttemptToAlterReadOnlyBindingEntriesCausesException_initiatimgTarget ( ) { undertakeTestingOfAReadOnlyEntryInBinding ( 'initiatingTarget' ) }
  void testAttemptToAlterReadOnlyBindingEntriesCausesException_targets ( ) { undertakeTestingOfAReadOnlyEntryInBinding ( 'targets' ) }

  //  GANT-75 called for adding the properties gant.file and gant.version.  Unfortunately, the version
  //  should always be null during testing as there is no jar, and no manifest.  Perhaps this means the way
  //  the tests are run should change so that a jar and not the directory of compiled files is used?

  void testGantFilePropertyIsAccessble ( ) {
    script = "target ( ${targetName} : '' ) { println ( binding.'gant.file' ) }"
    assertEquals ( 0 , processTargets ( targetName ) )
    assertEquals ( resultString ( targetName , '<stream>\n' ) , output )
  }
  void testGantVersionPropertyIsAccessible ( ) {
    System.err.println ( "Need to find a way of testing the actual version being correct instead of null." )
    script = "target ( ${targetName} : '' ) { println ( binding.'gant.version' ) }"
    assertEquals ( 0 , processTargets ( targetName ) )
    assertEquals ( resultString ( targetName , 'null\n' ) , output )
  }
}
