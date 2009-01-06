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

package org.codehaus.gant

/**
 *  This class is for code sharing between classes doing include activity.
 *
 *  @see IncludeTargets
 *  @see IncludeTool
 *  @author Russel Winder <russel.winder@concertant.com>
 */
abstract class AbstractInclude {
  /**
   *  The <code>GantBinding</code> for this run.
   */
  protected Binding binding
  /**
   *  The list of loaded classes.
   */
  protected final List loadedClasses = [ ]
  /**
   *  When using the ** * operator there is a need to not instantiate the class immediately so information
   *  has to be buffered.  This variable  holds a reference to the class ready for instantiation once all the
   *  constructor parameters are known.
   */
  protected Class pendingClass = null
  /**
   *  Constructor.
   *
   *  @param binding The <code>GantBinding</code> to associate with.
   */
  protected AbstractInclude ( final GantBinding binding ) { this.binding = binding }
  /**
   *  Implementation of the << operator taking a <code>Class</code> parameter.
   *
   *  @param theClass The <code>Class</code> to load and instantiate.
   *  @return The includer object to allow for << chaining.
   */
  public abstract leftShift ( Class theClass )
  /**
   *  Implementation of the << operator taking a <code>File</code> parameter.
   *
   *  @param file The <code>File</code> to load, compile, and instantiate.
   *  @return The includer object to allow for << chaining.
   */
  public abstract leftShift ( File file )
  /**
   *  Implementation of the << operator taking a <code>String</code> parameter.
   *
   *  @param s The <code>String</code> to compile and instantiate.
   *  @return The includer object to allow for << chaining.
   */
  public abstract leftShift ( String s )
  /**
   *  Implementation of the << operator taking a <code>List</code> parameter.
   *
   *  @param l The <code>List</code> of things to load (, compile) and instantiate.
   *  @return The includer object to allow for << chaining.
   */
  public leftShift ( final List l ) { l.each { item -> this << item } ; this }
  /**
   *  Implementation of the << operator taking a <code>Object</code> parameter.  This always throws an
   *  exception, it is here to avoid using a type other than <code>Class</code>, <code>File</code>,
   *  <code>String</code> or <code>List</code> (of <code>Class</code>, <code>File</code>, or
   *  <code>String</code>).
   *
   *  @param theClass The <code>Class</code> to load and instantiate.
   *  @throw RuntimeException always.
   */
  public leftShift ( final Object o ) { throw new RuntimeException ( 'Ignoring include of type ' + o.class.name ) }
  /**
   *  Implementation of the ** operator taking a <code>Class</code> parameter.
   *
   *  @param theClass The <code>Class</code> to load and instantiate.
   *  @return The includer object to allow for * operator.
   */
  public power ( final Class theClass ) { pendingClass = theClass ; this }
  /**
   *  Implementation of the * operator taking a <code>Map</code> parameter.  This operator only makes
   *  sense immediately after a ** operator, since only then is there a <code>Class</code> to instantiate.
   *
   *  @param keywordParameter The <code>Map</code> of parameters to the constructor.
   *  @return The includer object to allow for ** * operator chaining.
   */
  public abstract multiply ( Map keywordParameters )
  /**
   *  Create an instance of a class included using the << operator.
   *
   *  @param theClass The <code>Class</code> to instantiate.
   *  @throws NoSuchMethodException if the required constructor cannot be found.
   */
  protected createInstance ( Class theClass ) {
    if ( Script.isAssignableFrom ( theClass ) ) {
      // We need to ensure that the script runs so that it populates the binding.
      def script = theClass.newInstance ( )
      script.binding = binding
      script.run ( )
      return script
    }
    else {
      try { return theClass.getConstructor ( GantBinding ).newInstance ( [ binding ] as Object[] ) }
      catch ( NoSuchMethodException nsme ) { throw new RuntimeException ( 'Could not initialize ' + theClass.name , nsme ) }
    }
  }
  /**
   *  Create an instance of a class included with the ** * operator.
   *
   *  @param theClass The <code>Class</code> to instantiate.
   *  @param keywordParameter The <code>Map</code> containing the parameters for construction.
   *  @throws NoSuchMethodException if the required constructor cannot be found.
   */
  protected createInstance ( Class theClass , Map keywordParameters ) {
    try { return theClass.getConstructor ( GantBinding , Map ).newInstance ( [ binding , keywordParameters ] as Object[] ) }
    catch ( NoSuchMethodException nsme ) { throw new RuntimeException ( 'Could not initialize ' + theClass.name , nsme ) }
  }
  /**
   *  Make an attempt to evaluate a file, possible as a class.
   *
   *  @param file The <code>File</code> to read.
   *  @param asClass Specify whether the file is to be treated as a class.
   *  @return The class read or null if the file is not to be treated as a class.
   */
  private attemptEvaluate ( File file , boolean asClass ) {
    if ( asClass ) { return binding.groovyShell.evaluate ( file.text + " ; return ${file.name.replace('.groovy', '' )}" ) }
    //
    //  GANT-58 raised the issue of reporting errors correctly.  This means catching and processing
    //  exceptions so as to capture the original location of the error.
    //
    try { binding.groovyShell.evaluate ( file ) }
    catch ( Exception e ) {
      def errorSource = ''
      for ( stackEntry in e.stackTrace ) {
        if ( ( stackEntry.fileName == file.name ) && ( stackEntry.lineNumber  != -1 ) ) {
          errorSource += file.absolutePath + ', line ' + stackEntry.lineNumber + ' -- ' 
        }
      }
      throw new RuntimeException( errorSource + e.toString ( ) , e )
    }
    null
  }
  /**
   *  Read a file which may or may not be a class, searching the Gant library path if the file cannot
   *  be found at first.
   *
   *  @param file The <code>File</code> to read.
   *  @param asClass Specify whether this is supposed to be a class.
   *  @throws FileNotFoundException when the file cannot be found.
   */
  protected readFile ( File file , boolean asClass = false ) {
    try { return attemptEvaluate ( file , asClass ) }
    catch ( FileNotFoundException fnfe ) {
      for ( directory in binding.gantLib ) {
        def possible = new File ( (String) directory , file.name )
        if ( possible.isFile ( ) && possible.canRead ( ) ) { return attemptEvaluate ( possible , asClass ) }
      }
      throw fnfe
    }
  }
}
