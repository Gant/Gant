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

package org.codehaus.gant.ant.tests ;

import java.io.BufferedReader ;
import java.io.File ;
import java.io.IOException ;
import java.io.InputStream ;
import java.io.InputStreamReader ;

import java.util.ArrayList ;
import java.util.List ;

import junit.framework.TestCase ;

import org.apache.tools.ant.BuildException ;
import org.apache.tools.ant.Project ;
import org.apache.tools.ant.ProjectHelper ;

/**
 *  Unit tests for the Gant Ant task.  In order to test things appropriately this test must be initiated
 *  without any of the Groovy, Gant or related jars in the class path.  Also of course it must be a JUnit
 *  test with no connection to Groovy or Gant.
 *
 *  @author Russel Winder
 */
public class Gant_Test extends TestCase {
  private final String separator = System.getProperty ( "file.separator" ) ;
  private final boolean isWindows = System.getProperty ( "os.name" ).startsWith ( "Windows" ) ;
  private final String path ; {
    final StringBuilder sb = new StringBuilder ( ) ;
    sb.append ( "src" ) ;
    sb.append ( separator ) ;
    sb.append ( "test" ) ;
    sb.append ( separator ) ;
    sb.append ( "groovy" ) ;
    sb.append ( separator ) ;
    sb.append ( "org" ) ;
    sb.append ( separator ) ;
    sb.append ( "codehaus" ) ;
    sb.append ( separator ) ;
    sb.append ( "gant" ) ;
    sb.append ( separator ) ;
    sb.append ( "ant" ) ;
    sb.append ( separator ) ;
    sb.append ( "tests" ) ;
    path = sb.toString ( ) ;
  }
  private final String absolutePath =  System.getProperty ( "user.dir" )  + separator + path ;
  private final File antFile = new File ( path , "gantTest.xml" ) ;
  private Project project ;

  //  This variable is assigned in the Gant script hence the public static.
  public static String returnValue ;

  @Override protected void setUp ( ) throws Exception {
    super.setUp ( ) ;
    project = new Project ( ) ;
    project.init ( ) ;
    ProjectHelper.configureProject ( project , antFile ) ;
    returnValue = "" ;
  }

  public void testDefaultFileDefaultTarget ( ) {
    project.executeTarget ( "gantTestDefaultFileDefaultTarget" ) ;
    assertEquals ( "A test target in the default file." , returnValue ) ;
  }
  public void testDefaultFileNamedTarget ( ) {
    project.executeTarget ( "gantTestDefaultFileNamedTarget" ) ;
    assertEquals ( "Another target in the default file." , returnValue ) ;
  }
  public void testNamedFileDefaultTarget ( ) {
    project.executeTarget ( "gantTestNamedFileDefaultTarget" ) ;
    assertEquals ( "A test target in the default file." , returnValue ) ;
  }
  public void testNamedFileNamedTarget ( ) {
    project.executeTarget ( "gantTestNamedFileNamedTarget" ) ;
    assertEquals ( "Another target in the default file." , returnValue ) ;
  }
  public void testGantWithParametersAsNestedTags ( ) {
    project.executeTarget ( "gantWithParametersAsNestedTags" ) ;
    assertEquals ( "gant -Dflob=adob -Dburble gantParameters" , returnValue ) ;
  }
  public void testMultipleGantTargets ( ) {
    project.executeTarget ( "gantWithMultipleTargets" ) ;
    assertEquals ( "A test target in the default file.Another target in the default file." , returnValue ) ;
  }
  public void testUnknownTarget ( ) {
    try { project.executeTarget ( "blahBlahBlahBlah" ) ; }
    catch ( final BuildException be ) {
      assertEquals ( "Target \"blahBlahBlahBlah\" does not exist in the project \"Gant Ant Task Test\". " , be.getMessage ( ) ) ;
      return ;
    }
    fail ( "Should have got a BuildException." ) ;
  }
  public void testMissingGantfile ( ) {
    try { project.executeTarget ( "missingGantfile" ) ; }
    catch ( final BuildException be ) {
      assertEquals ( "Gantfile does not exist." , be.getMessage ( ) ) ;
      return ;
    }
    fail ( "Should have got a BuildException." ) ;
  }
  /*
   *  Test for the taskdef-related verify error problem.  Whatever it was supposed to do it passes now,
   *  2008-04-14.
   */
  public void testTaskdefVerifyError ( ) {
    project.executeTarget ( "gantTaskdefVerifyError" ) ;
    assertEquals ( "OK." , returnValue ) ;
  }
  /*
   *  A stream gobbler for the spawned process used by the <code>runAnt</code> method in the following
   *  tests.
   *
   *  @author Russel Winder
   */
  private static final class StreamGobbler implements Runnable {
    private final InputStream is ;
    private final StringBuilder sb ;
    public StreamGobbler ( final InputStream is , final StringBuilder sb ) {
      this.is = is ;
      this.sb = sb ;
    }
    public void run ( ) {
      try {
        final BufferedReader br = new BufferedReader ( new InputStreamReader ( is ) ) ;
        while ( true ) {
          final String line = br.readLine ( ) ;  //  Could throw an IOException hence the try block.
          if ( line == null ) { break ; }
          sb.append ( line ).append ( '\n' ) ;
        }
      }
      catch ( final IOException ignore ) { fail ( "Got an IOException reading a line in the read thread." ) ; }
    }
  }
  /*
   *  Run Ant in a separate process.  Return the standard output and the standard error that results as a
   *  List<String> with two items, item 0 is stnadard output and item 1 is standard error.
   *
   *  <p>This method assumes that either the environment variable ANT_HOME is set to a complete Ant
   *  installation or that the command ant (ant.bat on Windows) is in the path.</p>
   *
   *  <p>As at 2008-12-06 Canoo CruiseControl runs with GROOVY_HOME set to /usr/local/java/groovy, and
   *  Codehaus Bamboo runs without GROOVY_HOME being set.</p>
   *
   *  @param xmlFile the path to the XML file that Ant is to use.
   *  @param target the target to run, pass "" or null for the default target.
   *  @param expectedReturnCode the return code that the Ant execution should return.
   *  @param withClasspath whether the Ant execution should use the full classpathso as to find all the classes.
   */
  private List<String> runAnt ( final String xmlFile , final String target , final int expectedReturnCode , final boolean withClasspath ) {
    final List<String> command = new ArrayList<String> ( ) ;
    final String antHomeString = System.getenv ( "ANT_HOME" ) ;
    String antCommand ;
    if ( antHomeString != null ) { antCommand = antHomeString + separator + "bin" + separator  + "ant" ; }
    else { antCommand = "ant" ; }
    if ( isWindows ) {
      command.add ( "cmd.exe" ) ;
      command.add ( "/c" ) ;
      antCommand += ".bat" ;
    }
    command.add ( antCommand ) ;
    command.add ( "-f" ) ;
    command.add ( xmlFile ) ;
    if ( withClasspath ) {
      for ( final String p : System.getProperty ( "java.class.path" ).split ( System.getProperty ( "path.separator" ) ) ) {
        command.add ( "-lib" ) ;
        command.add ( p ) ;
      }
    }
    if ( ( target != null ) && ! target.trim ( ).equals ( "" ) ) { command.add ( target ) ; }
    final ProcessBuilder pb = new ProcessBuilder ( command ) ;
    final StringBuilder outputStringBuilder = new StringBuilder ( ) ;
    final StringBuilder errorStringBuilder = new StringBuilder ( ) ;
    try {
      final Process p = pb.start ( ) ;  //  Could throw an IOException hence the try block.
      final Thread outputGobbler = new Thread ( new StreamGobbler ( p.getInputStream ( ) , outputStringBuilder ) ) ;
      final Thread errorGobbler = new Thread ( new StreamGobbler ( p.getErrorStream ( ) , errorStringBuilder ) ) ;
      outputGobbler.start ( ) ;
      errorGobbler.start ( ) ;
      try { assertEquals ( expectedReturnCode , p.waitFor ( ) ) ; }
      catch ( final InterruptedException ignore ) { fail ( "Got an InterruptedException waiting for the Ant process to finish." ) ; }
      try { outputGobbler.join ( ) ;}
      catch ( final InterruptedException ignore ) { fail ( "Got an InterruptedException waiting for the output gobbler to terminate." ) ; }
      try { errorGobbler.join ( ) ;}
      catch ( final InterruptedException ignore ) { fail ( "Got an InterruptedException waiting for the error gobbler to terminate." ) ; }
      final List<String> returnList = new ArrayList<String> ( ) ;
      returnList.add ( outputStringBuilder.toString ( ) ) ;
      returnList.add ( errorStringBuilder.toString ( ) ) ;
      return returnList ;
    }
    catch ( final IOException ignore ) { fail ( "Got an IOException from starting the process." ) ; }
    //  Keep the compiler happy, it doesn't realize that execution cannot get here -- i.e. that fail is a non-returning function.
    return null ;
  }
  /**
   *  The output due to the targets in commonBits.xml.
   */
  private final String commonTargetsList = "-initializeWithGroovyHome:\n\n-initializeNoGroovyHome:\n\n-defineGantTask:\n\n" ;
  /*
   *  Tests stemming from GANT-19 and relating to ensuring the right classpath when loading the Groovyc Ant
   *  task.
   */
  private String createBaseMessage ( ) {
    final StringBuilder sb = new StringBuilder ( ) ;
    sb.append ( "Buildfile: " ) ;
    sb.append ( path ).append ( separator ) ;
    sb.append ( "gantTest.xml\n\n" ) ;
    sb.append ( commonTargetsList ) ;
    sb.append ( "gantTestDefaultFileDefaultTarget:\n" ) ;
    return sb.toString ( ) ;
  }
  private String trimTimeFromSuccessfulBuild ( final String message ) {
    return message.replaceFirst ( "Total time: [0-9]*.*" , "" ) ;
  }
  public void testRunningAntFromShellFailsNoClasspath ( ) {
    final List<String> result = runAnt ( antFile.getPath ( ) , null , ( isWindows ? 0 : 1 ) , false ) ;
    assert result.size ( ) == 2 ;
    //  On Windows the ant.bat file always returns zero :-(
    assertEquals ( createBaseMessage ( ) , result.get ( 0 ) ) ;
    assertTrue ( result.get ( 1 ).startsWith ( "\nBUILD FAILED\norg.codehaus.groovy.control.MultipleCompilationErrorsException: startup failed, build: 15: unable to resolve class org.codehaus.gant.ant.tests.Gant_Test\n @ line 15, column 1.\n1 error\n" ) ) ;
  }
  public void testRunningAntFromShellSuccessful ( ) {
    final List<String> result = runAnt ( antFile.getPath ( ) , null , 0 , true ) ;
    assert result.size ( ) == 2 ;
    assertEquals ( createBaseMessage ( ) + "\nBUILD SUCCESSFUL\n\n", trimTimeFromSuccessfulBuild ( result.get ( 0 ) ) ) ;
    assertEquals ( "" , result.get ( 1 ) ) ;
  }
  /*
   *  The following tests are based on the code presented in email exchanges on the Groovy developer list by
   *  Chris Miles.  cf.  GANT-50.  This assumes that the tests are run from a directory other than this one.
   */
  private final String basedirAntFilePath = path + separator + "basedir.xml" ;
  //
  //  TODO: The includeTag is needed because of an error -- it should be removed and the [groovy] tag always present.
  //
  private String createMessageStart ( final String target , final String taskName , final boolean extraClassPathDefinition ) {
    final StringBuilder sb = new StringBuilder ( ) ;
    sb.append ( "Buildfile: " ) ;
    sb.append ( path ).append ( separator ) ;
    sb.append ( "basedir.xml\n     [echo] basedir::ant basedir=" ) ;
    sb.append ( absolutePath ) ;
    sb.append ( "\n\n-initializeWithGroovyHome:\n\n-initializeNoGroovyHome:\n\n-define" ) ;
    sb.append ( taskName ) ;
    sb.append ( "Task:\n\n" ) ;
    if ( extraClassPathDefinition ) { sb.append ( "-defineClasspath:\n\n" ) ; }
    sb.append ( target ) ;
    sb.append ( ":\n" ) ;
    return sb.toString ( ) ;
  }
  public void testBasedirInSubdirDefaultProjectForGant ( ) {
    final String target = "defaultProject" ;
    final StringBuilder sb = new StringBuilder ( ) ;
    sb.append ( createMessageStart ( target , "Groovy" , true ) ) ;
    sb.append ( "   [groovy] basedir::groovy basedir=" ) ;
    sb.append ( absolutePath ) ;
    sb.append ( "\n   [groovy] default:\n   [groovy] \n   [groovy] basedir::gant basedir=" ) ;
    //
    //  Currently a Gant object instantiated in a Groovy task in an Ant script does not inherit the basedir
    //  of the "calling" Ant.  Instead it assumes it is rooted in the process start directory.  According to
    //  GANT-50 this is an error.  The question is to decide whether it is or not.
    //
    //  TODO : Should this be sb.append ( absolutePath ) ?  cf. GANT-50.
    //
    sb.append ( System.getProperty ( "user.dir" ) ) ;
    sb.append ( "\n   [groovy] ------ default\n   [groovy] \n\nBUILD SUCCESSFUL\n\n" ) ;
    final List<String> result = runAnt ( basedirAntFilePath , target , 0 , false ) ;
    assert result.size ( ) == 2 ;
    assertEquals ( sb.toString ( ) , trimTimeFromSuccessfulBuild ( result.get ( 0 ) ) ) ;
    assertEquals ( "" , result.get ( 1 ) ) ;
  }
  public void testBasedirInSubdirExplicitProjectForGant ( ) {
    final String target = "explicitProject" ;
    final StringBuilder sb = new StringBuilder ( ) ;
    sb.append ( createMessageStart ( target , "Groovy" , true ) ) ;
    sb.append ( "   [groovy] basedir::groovy basedir=" ) ;
    sb.append ( absolutePath ) ;
    //
    //  In this case the instantiated Gant object is connected directly to the Project object instantiated
    //  by Ant and so uses the same basedir.  However it seems that the output (and error) stream are not
    //  routed through the bit of Ant that prefixes the output with the current task name.
    //
    //  TODO : Sort out whether Is it correct that [groovy] is not printed out at the start of this?  cf. GANT-50.
    //
    sb.append ( "\ndefault:\nbasedir::gant basedir=" ) ;
    sb.append ( absolutePath ) ;
    sb.append ( "\n------ default\n\nBUILD SUCCESSFUL\n\n" ) ;
    final List<String> result = runAnt ( basedirAntFilePath , target , 0 , false ) ;
    assert result.size ( ) == 2 ;
    assertEquals ( sb.toString ( ) , trimTimeFromSuccessfulBuild ( result.get ( 0 ) ) ) ;
    assertEquals ( "" , result.get ( 1 ) ) ;
  }
  public void testBasedirInSubdirGantTask ( ) {
    final String target = "gantTask" ;
    final StringBuilder sb = new StringBuilder ( ) ;
    sb.append ( createMessageStart ( target , "Gant" , false ) ) ;
    sb.append ( "     [gant] basedir::gant basedir=" ) ;
    sb.append ( absolutePath ) ;
    sb.append ( "\n\nBUILD SUCCESSFUL\n\n" ) ;
    final List<String> result = runAnt ( basedirAntFilePath , target , 0 , false ) ;
    assert result.size ( ) == 2 ;
    assertEquals ( sb.toString ( ) , trimTimeFromSuccessfulBuild ( result.get ( 0 ) ) ) ;
    assertEquals ( "" , result.get ( 1 ) ) ;
  }
  //  Test out the GANT-80 issues.
  public void test_GANT_80 ( ) {
    final String message = "Hello World." ; //  Must be the same string as in GANT_80.gant 
    final String antFilePath = path + separator + "GANT_80.xml" ;
    final StringBuilder sb = new StringBuilder ( ) ;
    sb.append ( "Buildfile: " ) ;
    sb.append ( antFilePath ) ;
    sb.append ( "\n\n" ) ;
    sb.append ( commonTargetsList ) ;
    //  TODO:  Why does the [echo] get stripped off the second of these?
    sb.append ( "default:\n     [gant] Hello World.\n     [gant] Hello World.\n\nBUILD SUCCESSFUL\n\n" ) ;
    final List<String> result = runAnt ( antFilePath , null , 0 , false ) ;
    assert result.size ( ) == 2 ;
    assertEquals ( sb.toString ( ) , trimTimeFromSuccessfulBuild ( result.get ( 0 ) ) ) ;
    //  TODO:  Fix this error, the wrong output results.
    //assertEquals ( "Hello World.\n" , result.get ( 1 ) ) ;
    assertEquals ( "" , result.get ( 1 ) ) ;
  }
}
