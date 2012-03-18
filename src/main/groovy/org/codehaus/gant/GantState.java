//  Gant -- A Groovy way of scripting Ant tasks.
//
//  Copyright © 2006-10 Russel Winder
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

package org.codehaus.gant ;

import org.apache.tools.ant.Project ;

/**
 *  A class to hold the shared global state for a run of Gant, also a variety of general-use constants are
 *  defined here.
 *
 *  <p>This class was originally needed because parts of Gant are written in Java and parts in Groovy and it
 *  was not possible to compile them all at the same time.  All references to Groovy classes had to be
 *  avoided in the Java classes so that the Java could be compiled and then the Groovy compiled.  This class
 *  contains things that should be in the <code>Gant</code> class but could not be.  All this is no longer
 *  true, so the material could go back into the <code>Gant</code> class.</p>
 *
 *  @author Russel Winder <russel@winder.org.uk>
 */
public class GantState {

  //  Ant's Project message priority levels are good for specifying the priority of a message sent to the
  //  log but present an awkward way of specifying the level of verbosity.  We therefore create some aliases
  //  to make code a little more self-documenting.  
  //
  //  Ant appears not to have a silent mode.  Gant allows for a silent mode by adding an extra verbosity
  //  level.  We have to be aware that the constants from Project are effectively an enumeration -- integer
  //  values starting at 0 -- and that the larger the number, the lower the priority of the message.  Thus
  //  if we set the priority of the logger less than MSG_ERR, no messages will be output.
  //
  //  NB Ant appears to output errors to the error channel (System.err by default) and all other messages to
  //  the standard output (System.out by default).  Gant's behaviour to date (i.e. up to version 1.6.1) has
  //  been to output all information to System.out.  For the moment then error information is logged at
  //  MSG_WARN priority, and MSG_ERR is unused.

  /**
   *  Output no information ever.
   */
  public static final int SILENT = Project.MSG_ERR - 1 ;
  /**
   *  Output only information about errors.
   */
  public static final int ERRORS_ONLY = Project.MSG_ERR ;
  /**
   *  Output only the meagrest of information.
   */
  public static final int WARNINGS_AND_ERRORS = Project.MSG_WARN ;
  /**
   *  Output information about which task is executing, and other things.
   */
  public static final int NORMAL = Project.MSG_INFO ;
  /**
   *  Output lots of information about what is going on.
   */
  public static final int VERBOSE = Project.MSG_VERBOSE ;
  /**
   *  Output huge amounts of information about what is going on.
   */
  public static final int DEBUG = Project.MSG_DEBUG ;
  /**
   *  The current state of the verbosity of execution -- default is <code>NORMAL</code>.
   */
  public static int verbosity = NORMAL ;
  /**
   *  Whether this is a dry drun, i.e. no actual execution occur.
   */
  public static boolean dryRun = false ;
  /**
   *  We never want an instance of this class, so the constructor is made private.
   */
  private GantState ( ) { }
}
