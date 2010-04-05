//  Gant -- A Groovy way of scripting Ant tasks.
//
//  Copyright © 2008-10 Russel Winder
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

import java.io.File ;
import java.io.IOException ;

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
  private final String locationPrefix = ( "Gradle".equals ( System.getProperty ( "buildFrameworkIdentifier" ) ) ? ".." + separator : "" ) ;
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
  private final String canonicalPath ; {
    try { canonicalPath = ( new File ( locationPrefix + path ) ).getCanonicalPath ( ) ; }
    catch ( final IOException ioe ) { throw new RuntimeException ( "Path calculation failure." , ioe ) ; }
  }
  private final File antFile =  new File ( canonicalPath , "gantTest.xml" ) ;
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
}
