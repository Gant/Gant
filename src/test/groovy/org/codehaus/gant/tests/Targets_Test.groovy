//  Gant -- A Groovy way of scripting Ant tasks.
//
//  Copyright © 2006-9 Russel Winder
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
 *  A test to ensure that the target specification works.
 *
 *  @author Russel Winder <russel.winder@concertant.com>
 */
final class Targets_Test extends GantTestCase {
  private final targetName = 'targetname'
  private final ok = 'OK.'
  private final result = resultString ( targetName , ok )
  void testNoDescription ( ) {
    script = "target ( ${targetName} : '' ) { print ( '${ok}' ) }"
    assertEquals ( 0 , processCmdLineTargets ( targetName ) )
    assertEquals ( result , output )
    assertEquals ( '' , error )
  }
  void testWithDescription ( ) {
    script = "target ( ${targetName} : 'Blah blah' ) { print ( '${ok}' ) }"
    assertEquals ( 0 , processCmdLineTargets ( targetName ) )
    assertEquals ( result , output )
    assertEquals ( '' , error )
  }
  void testEmptyMap ( ) {
    script = "target ( [ : ] ) { print ( '${ok}' ) }"
    assertEquals ( -4 , processCmdLineTargets ( targetName ) )
    assertEquals ( '' , output )
    assertEquals ( 'Standard input, line 1 -- Error evaluating Gantfile: Target specified without a name.\n' , error )
  }
  void testMultipleEntries ( ) {
    script = "target ( fred : '' , debbie : '' ) { print ( '${ok}' ) }"
    assertEquals ( -4 , processCmdLineTargets ( targetName ) )
    assertEquals ( '' , output )
    assertEquals ( 'Standard input, line 1 -- Error evaluating Gantfile: Target specified without a name.\n' , error )
  }
  void testOverwriting ( ) {
    //
    //  TODO : Fix the problem of overwriting targets.  Until changed, creating a new symbol in the binding
    //  using a target overwrites the old symbol.  This is clearly wrong behaviour and needs amending.
    //
    script = """
target ( ${targetName} : '' ) { println ( 'Hello 1' ) }
target ( ${targetName} : '' ) { println ( 'Hello 2' ) }
"""
    assertEquals ( 0 , processCmdLineTargets ( targetName ) )
    assertEquals ( resultString ( targetName , 'Hello 2\n' ) , output )
    assertEquals ( '' , error )
  }
  void testForbidRedefinitionOfTarget ( ) {
    script = """
target ( ${targetName} : '' ) { }
target = 10
"""
    assertEquals ( -4 , processCmdLineTargets ( targetName ) )
    assertEquals ( '' , output )
    assertEquals ( 'Standard input, line 3 -- Error evaluating Gantfile: Cannot redefine symbol target\n' , error )
  }
  void testStringParameter ( ) {
    script = "target ( '${targetName}' ) { print ( '${ok}' ) }"
    assertEquals ( -4 , processCmdLineTargets ( targetName ) )
    assertEquals ( '' , output )
    assertTrue ( error.contains ( 'Standard input, line 1 -- Error evaluating Gantfile: No signature of method: org.codehaus.gant.GantBinding$_initializeGantBinding_closure' ) )
  }
  void testStringSequenceParameter ( ) {
    script = "target ( '${targetName}' , 'description' ) { print ( '${ok}' ) }"
    assertEquals ( -4 , processCmdLineTargets ( targetName ) )
    assertEquals ( '' , output )
    assertTrue ( error.startsWith ( 'Standard input, line 1 -- Error evaluating Gantfile: No signature of method: org.codehaus.gant.GantBinding$_initializeGantBinding_closure' ) )
  }
  void testMissingTargetInScriptExplicitTarget ( ) {
    script = "setDefaultTarget ( ${targetName} )"
    assertEquals ( -4 , processCmdLineTargets ( targetName ) )
    assertEquals ( '' , output )
    assertEquals ( "Standard input, line 1 -- Error evaluating Gantfile: No such property: ${targetName} for class: standard_input\n" , error )
  }
  void testMissingTargetInScriptDefaultTarget ( ) {
    script = "setDefaultTarget ( ${targetName} )"
    assertEquals ( -4 , processCmdLineTargets ( ) )
    assertEquals ( '' , output )
    assertEquals ( "Standard input, line 1 -- Error evaluating Gantfile: No such property: ${targetName} for class: standard_input\n" , error )
  }
  void testFaultyScript ( ) {
    script = 'XXXXX : YYYYY ->'
    assertEquals ( -2 , processCmdLineTargets ( ) )
    assertEquals ( '' , output )
    assertEquals ( 'Error evaluating Gantfile: startup failed' + ( ( groovyMinorVersion > 6 ) ? ':\n' : ', ' ) + '''standard_input: 1: unexpected token: -> @ line 1, column 15.''' +
                   ( ( groovyMinorVersion > 6 ) && ! ( ( groovyMinorVersion == 7 ) && ( releaseType == GantTestCase.ReleaseType.BETA ) && ( groovyBugFixVersion < 2 ) ) ? '''
   XXXXX : YYYYY ->
                 ^

''' : '\n' ) + '''1 error
''' , error )
  }

  //  Tests resulting from GANT-45.

  final testScript = """
target ( ${targetName} : '' ) { println ( home ) }
setDefaultTarget ( ${targetName} )
"""
  final expectedOutput = 'Standard input, line 2 -- Error evaluating Gantfile: No such property: home for class: standard_input\n'
  void test_GANT_45_MessageBugDefaultTarget ( ) {
    script = testScript
    assertEquals ( -12 , processCmdLineTargets ( ) )
    assertEquals ( targetName + ':\n' , output )
    assertEquals ( expectedOutput , error )
  }
  void test_GANT_45_MessageBugExplicitTarget ( ) {
    script = testScript
    assertEquals ( -11 , processCmdLineTargets ( targetName ) )
    assertEquals ( targetName + ':\n' , output )
    assertEquals ( expectedOutput , error )
  }

  //  Test relating to GStrings as target names

  void testGStringWorkingAsATargetName ( ) {
    script = """
def name = '${targetName}'
target ( "\${name}" : '' ) { println ( "\${name}" ) }
"""
    assertEquals ( 0 , processCmdLineTargets ( targetName ) )
    assertEquals ( resultString ( targetName , targetName + '\n' ) , output )
    assertEquals ( '' , error )
  }

  //  Tests resulting from GANT-55 -- GString as a parameter to depends call causes problems.
  //
  //  GANT-61 turns out to be a replica of GANT-55.

  private final profileTag = '.profile'
  private final compileTag = '.compile'
  private final expectedGant55Result =  resultString ( targetName + compileTag , resultString ( targetName + profileTag , "Profile for ${targetName}\n" ) + "Compile for ${targetName}\n" )
  void test_GANT_55_original_usingGStringKeys ( ) {
    script = """
def tag = '${targetName}'
target ( "\${tag}${profileTag}" : '' ) { println ( "Profile for \$tag" ) }
target ( "\${tag}${compileTag}" : '' ) {
  depends ( "\${tag}${profileTag}" )
  println ( "Compile for \$tag" )
}
"""
    /*
     *  The original behaviour:
     *
    assertEquals ( -13 , processCmdLineTargets ( 'test.compile' ) )
    assertEquals ( 'depends called with an argument (test.profile) that is not a known target or list of targets.\n' , output )
    *
    *  Is now fixed:
    */
    assertEquals ( 0 , processCmdLineTargets ( "${targetName}${compileTag}" ) ) // NB parameter must be a String!
    assertEquals ( expectedGant55Result , output )
    assertEquals ( '' , error )
  }
  void test_GANT_55_usingStringKeys ( ) {
    script = """
target ( '${targetName}${profileTag}' : '' ) { println ( 'Profile for $targetName' ) }
target ( '${targetName}${compileTag}' : '' ) {
  depends ( '${targetName}${profileTag}' )
  println ( 'Compile for $targetName' )
}
"""
    assertEquals ( 0 , processCmdLineTargets ( targetName + compileTag ) )
    assertEquals ( expectedGant55Result , output )
    assertEquals ( '' , error )
  }

  //  Tests to ensure the patch of GANT-56 doesn't do nasty things.  A couple of tests from earlier change
  //  their purpose.

  void test_GANT_56 ( ) {
    script = """
targetName = '${targetName}'
targetDescription = 'Some description or other'
target ( name : targetName , description : targetDescription ) {
  assert it.name == targetName
  assert it.description == targetDescription
}
setDefaultTarget ( targetName )
"""
    assertEquals ( 0 , processCmdLineTargets ( ) )
    assertEquals ( resultString ( targetName , '' ) , output )
    assertEquals ( '' , error )
  }

  void test_nameAsATargetNameImpliesExplicitDefinitionStyle ( ) {
    final bar = 'bar'
    script = """
targetName = '${bar}'
target ( name : targetName ) {
  assert it.name == targetName
  assert it.description == null
}
setDefaultTarget ( targetName )
"""
    assertEquals ( 0 , processCmdLineTargets ( ) )
    assertEquals ( resultString ( bar , '' ) , output )
    assertEquals ( '' , error )
  }

  //  Phil Swenson asked for the name of the target being completed to be available -- see the email on the Gant
  //  Developer list dated 2009-09-26 20:48+00:00

  private final one = 'one'
  private final two = 'two'
  private final initiatingTargetScript = """
target ( ${one} : '' ) {
  println ( initiatingTarget )
}
target ( ${two} : '' ) {
  depends ( ${one} )
  println ( initiatingTarget )
}
"""
  void testInitiatingTargetAvailableToScript ( ) {
    script = initiatingTargetScript
    assertEquals ( 0 , processCmdLineTargets ( two ) )
    assertEquals ( resultString ( two , resultString ( one , two + '\n' ) + two + '\n' ) , output )
    assertEquals ( '' , error )
  }
  void testEachInitiatingTargetOfASequenceAvailableToScript ( ) {
    script = initiatingTargetScript
    assertEquals ( 0 , processCmdLineTargets ( [ one , two ] ) )
    assertEquals ( resultString ( one , one + '\n' ) + resultString ( two , resultString ( one , two + '\n' ) + two + '\n' ) , output )
    assertEquals ( '' , error )
  }
  
}
