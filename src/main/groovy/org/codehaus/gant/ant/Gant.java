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

package org.codehaus.gant.ant ;

import java.io.ByteArrayOutputStream ;
import java.io.File ;
import java.io.PrintStream ;

import java.util.ArrayList ;
import java.util.HashMap ;
import java.util.List ;
import java.util.Map ;

import org.apache.tools.ant.BuildException ;
import org.apache.tools.ant.Project ;
import org.apache.tools.ant.Task ;

import org.codehaus.gant.GantBinding ;
import org.codehaus.gant.GantBuilder ;

/**
 *  Execute a Gant script.
 *
 *  <p>This Ant task provides a Gant calling capability. The original intention behind this was to support
 *  continuous integration systems that do not directly support Gant but only Ant.  However it also allows
 *  for gradual evolution of an Ant build into a Gant build.</p>
 *
 *  <p>Possible attributes are:</p>
 *
 *  <ul>
 *    <li>file &ndash; the path of the Gant script to execute.</li>
 *    <li>target &ndash; the target to execute; must be a single target name.  For specifying than a
 *        single target, use nested gantTarget tags.</li>
 *  </ul>
 *
 *  <p>Both of these are optional.  The file 'build.gant' and the default target are used by default.  An
 *  error results if there is no default target and no target is specified.</p>
 *
 *  <p>Definitions, if needed, are specified using nested <code>definition</code> tags, one for each symbol
 *  to be defined.  Each <code>definition</code> tag takes a compulsory <code>name</code> attribute and an
 *  optional <code>value</code> attribute.</p>
 *
 * @author Russel Winder
 */
public class Gant extends Task {
  /**
   *  The path to the file to use to drive the Gant build.  The default is build.gant.  This path is
   *  relative to the basedir of the Ant project if it is set, or the directory in which the job was started
   *  if the basedir is not set.
   */
  private String file = "build.gant" ;
  /**
   *  A class representing a nested definition tag.
   */
  public static final class Definition {
    private String name ;
    private String value ;
    public void setName ( final String s ) { name = s ; }
    public String getName ( ) { return name ; }
    public void setValue ( final String s ) { value = s ; }
    public String getValue ( ) { return value ; }
  }
  /**
   *  A list of definitions to be set in the Gant instance.
   */
  private final List<Definition> definitions = new ArrayList<Definition> ( ) ;
  /**
   *  A class representing a nested target tag.
   */
  public static final class GantTarget {
    private String value ;
    public void setValue ( final String s ) { value = s ; }
    public String getValue ( ) { return value ; }
  }
  /**
   *  A list of targets to be achieved by the Gant instance.
   */
  private final List<GantTarget> targets = new ArrayList<GantTarget> ( ) ;
  /**
   *  Set the name of the build file to use.  This path is relative to the basedir of the Ant project if it
   *  is set, or the directory in which the job was started if the basedir is not set.
   *
   *  @param f The name of the file to be used to drive the build.
   */
  public void setFile ( final String f ) { file = f ; }
  /**
   *  Set the target to be achieved.
   *
   *  @param t The target to achieve.
   */
  public void setTarget ( final String t ) {
    final GantTarget gt = new GantTarget ( ) ;
    gt.setValue ( t ) ;
    targets.add ( gt ) ;
  }
  /**
   *  Create a node to represent a nested <code>gantTarget</code> tag.
   *
   *  @return a new <code>GantTarget</code> instance ready for values to be added.
   */
  public GantTarget createGantTarget ( ) {
    final GantTarget gt = new GantTarget ( ) ;
    targets.add ( gt ) ;
    return gt ;
  }
  /**
   *  Create a node to represent a nested <code>definition</code> tag.
   *
   *  @return a new <code>Definition</code> instance ready for values to be added.
   */
  public Definition createDefinition ( ) {
    final Definition definition = new Definition ( ) ;
    definitions.add ( definition ) ;
    return definition ;
  }
  /**
   * Load the file and then execute it.
   */
  @Override public void execute ( ) throws BuildException {
    if ( ! ( new File ( file ) ).exists ( ) ) { throw new BuildException ( "Gantfile does not exist." , getLocation ( ) ) ; }
    //
    //  To address the issues raised in GANT-50, we need to ensure that the org.apache.tools.ant.Project
    //  instance used by Gant is the one that initiated this task.  This means creating a new GantBinding
    //  with a specially prepared GantBuilder in order to create a suitable gant.Gant object.
    //
    //  NB  As this class is called Gant, we have to use fully qualified name to get to the other Gant class.
    //
    //  There seem to be various incomprehensible issues in attempting to use the original Project object so
    //  create a new one and set the basedir appropriately.
    //
    final Project newProject = new Project ( ) ;
    newProject.init ( ) ;
    newProject.setBaseDir ( getOwningTarget ( ).getProject ( ).getBaseDir ( ) ) ;
    final GantBuilder ant = new GantBuilder ( newProject ) ;
    final Map<String,String> environmentParameter = new HashMap<String,String> ( ) ;
    environmentParameter.put ( "environment" , "environment" ) ;
    //  Do not allow the output to escape.  The problem here is that if the output is allowed out then
    //  Ant, Gant, Maven, Eclipse and IntelliJ IDEA all behave slightly differently.  This makes testing
    //  nigh on impossible.  Also the user doesn't need to know about these.
    final PrintStream outSave = System.out ;
    System.setOut ( new PrintStream ( new ByteArrayOutputStream ( ) ) ) ;
    ant.invokeMethod ( "property" , new Object[] { environmentParameter } ) ;
    final GantBinding binding = new GantBinding ( ) ;
    binding.setVariable ( "ant" , ant ) ;
    binding.setVariable ( "Ant" , ant ) ; // TODO : deprecate and remove this.
    for ( final Definition definition : definitions ) {
      final Map<String,String> definitionParameter = new HashMap<String,String> ( ) ;
      definitionParameter.put ( "name" , definition.getName ( ) ) ;
      definitionParameter.put ( "value" , definition.getValue ( ) ) ;
      ant.invokeMethod ( "property" , new Object[] { definitionParameter } ) ;
    }
    System.setOut ( outSave ) ;
    final gant.Gant gant = new gant.Gant ( binding ) ;
    gant.loadScript ( new File ( newProject.getBaseDir ( ) , file ) ) ;
    final List<String> targetsAsStrings = new ArrayList<String> ( ) ;
    for ( final GantTarget g : targets ) { targetsAsStrings.add ( g.getValue ( ) ) ; }
    final int returnCode =  gant.processTargets ( targetsAsStrings ) ;
    if ( returnCode != 0 ) { throw new BuildException ( "Gant execution failed with return code " + returnCode + '.' , getLocation ( ) ) ; }
  }
}
