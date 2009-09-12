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

package org.codehaus.gant.tests ;

import java.util.Arrays ;
import java.util.ArrayList ;
import java.util.List ;

import java.io.ByteArrayInputStream ;
import java.io.ByteArrayOutputStream ;
import java.io.PrintStream ;

import groovy.util.GroovyTestCase ;

import gant.Gant ;

import org.codehaus.gant.GantState ;

import org.codehaus.groovy.runtime.InvokerHelper ;

/**
 *  A Gant test case: Adds the required input stream manipulation features to avoid replication of code.
 *  Also prepare a new instance of Gant for each test.
 *
 *  @author Russel Winder <russel.winder@concertant.com>
 */
public abstract class GantTestCase extends GroovyTestCase {
  public static final String exitMarker = "------ " ;
  //
  //  Groovy version numbering is complicated:
  //
  //  For released versions the number is x.y.z where x is the major number, y is the minor number, and z is
  //  the bugfix number -- with all of them being integers.
  //
  //  For released pre-release versions the number depends on the state of the release.  Early on the
  //  numbers are x.y-beta-z.  Later on they are x.y-rc-z.
  //
  //  For branches from the main repository, the number is related to the branch.  Basically add -SNAPSHOT
  //  to the number with z being one higher than the last release.  So checkouts of maintenance branches
  //  will have x.y.z-SNAPSHOT, while from trunk numbers will be like x.y-beta-z-SNAPSHOT.
  //
  public enum ReleaseType { RELEASED, RELEASED_SNAPSHOT, BETA, BETA_SNAPSHOT, RC, RC_SNAPSHOT } ;
  public static final int groovyMajorVersion ;
  public static final int groovyMinorVersion ;
  public static final int groovyBugFixVersion ;
  public static final ReleaseType releaseType ;
  static {
    final String[] version =  InvokerHelper.getVersion ( ).split ( "[.-]" ) ;
    switch ( version.length ) {
     case 3 :
       groovyBugFixVersion =  Integer.parseInt ( version[2] ) ;
       releaseType = ReleaseType.RELEASED ;
       break ;
     case 4 :
       if ( version[3].equals ( "SNAPSHOT" ) ) {
         groovyBugFixVersion =  Integer.parseInt ( version[2] ) ;
         releaseType = ReleaseType.RELEASED_SNAPSHOT ;
       }
       else {
         groovyBugFixVersion =  Integer.parseInt ( version[3] ) ;
         releaseType = version[2].equals ( "RC" ) ? ReleaseType.RC : ReleaseType.BETA ;
       }
       break ;
     case 5 :
        groovyBugFixVersion =  Integer.parseInt ( version[3] ) ;
        releaseType = version[2].equals ( "RC" ) ? ReleaseType.RC_SNAPSHOT : ReleaseType.BETA_SNAPSHOT ;
        assert version[4] == "SNAPSHOT" ;
       break ;
     default :
       throw new RuntimeException ( "Groovy version number is not well-formed." ) ;
    }
    groovyMajorVersion = Integer.parseInt ( version[0] ) ;
    groovyMinorVersion = Integer.parseInt ( version[1] ) ;
  }
  public static final boolean isWindows ;
  static {
    final String osName = System.getProperty ( "os.name" ) ;
    isWindows = ( osName.length ( ) > 6 ) && osName.substring ( 0 , 7 ).equals ( "Windows" ) ;
  }
  private ByteArrayOutputStream output ;
  private ByteArrayOutputStream error ;
  private PrintStream savedOut ;
  private PrintStream savedErr ;
  protected Gant gant ;
  protected String script ;
  @Override protected void setUp ( ) throws Exception {
    super.setUp ( ) ;
    savedOut = System.out ;
    savedErr = System.err ;
    output = new ByteArrayOutputStream ( ) ;
    error = new ByteArrayOutputStream ( ) ;
    System.setOut ( new PrintStream ( output ) ) ;
    System.setErr ( new PrintStream ( error ) ) ;
    gant = new Gant ( ) ;
    gant.setBuildClassName ( "standard_input" ) ;
    script = "" ;
    //
    //  If the JUnit is run with fork mode 'perTest' then we do not have to worry about the static state.
    //  However, when the fork mode is 'perBatch' or 'once' then we have to ensure that the static state
    //  is reset to the normal state.
    //
    GantState.verbosity = GantState.NORMAL ;
    GantState.dryRun = false ;
  }
  @Override protected void tearDown ( ) throws Exception {
    System.setOut ( savedOut ) ;
    System.setErr ( savedErr ) ;
    super.tearDown ( ) ;
  }
  protected void setScript ( final String s ) { script = s ; System.setIn ( new ByteArrayInputStream ( script.getBytes ( ) ) ) ; }
  protected Integer processTargets ( ) { gant.loadScript ( System.in ) ; return gant.processTargets ( ) ; }
  protected Integer processTargets ( final String s ) { gant.loadScript ( System.in ) ; return gant.processTargets ( s ) ; }
  protected Integer processTargets ( final List<String> l ) { gant.loadScript ( System.in ) ; return gant.processTargets ( l ) ; }
  protected Integer processCmdLineTargets ( ) { return gant.processArgs ( new String[] { "-f" , "-" } ) ; }
  protected Integer processCmdLineTargets ( final String s ) { return gant.processArgs ( new String[] { "-f" , "-" , s } ) ; }
  protected Integer processCmdLineTargets ( final List<String> l ) {
    final List<String> args = new ArrayList<String> ( Arrays.asList ( "-f" , "-" ) ) ;
    args.addAll ( l ) ;
    return gant.processArgs ( args.toArray ( new String[0] ) ) ;
  }
  protected String getOutput ( ) { return output.toString ( ).replace ( "\r" , "" ) ; }
  protected String getError ( ) { return error.toString ( ).replace ( "\r" , "" ) ; }
  protected String escapeWindowsPath ( final String path ) { return isWindows ? path.replace ( "\\" ,  "\\\\" ) : path ; }
  protected String resultString ( final String targetName , final String result ) {
    return targetName + ":\n" + result + exitMarker + targetName + '\n' ;
  }
}
