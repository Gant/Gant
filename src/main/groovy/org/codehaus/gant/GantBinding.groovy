//  Gant -- A Groovy build framework based on scripting Ant tasks.
//
//  Copyright © 2008 Russel Winder
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
 *  This class is a sub-class of <code>groovy.lang.Binding</code> to provide extra capabilities.  In
 *  particular, all the extra bits needed in the binding for Gant to actually work at all.  Handle this as a
 *  separate class to avoid replication of initialization if binding objects are cloned.
 *
 *  @author Russel Winder <russel.winder@concertant.com>
 */
public class GantBinding extends Binding implements Cloneable {
  /**
   *  Determine whether we are initializing an instance and so are able to define the read-only items.
   */
  private boolean initializing = true
  /**
   *  Default constructor.
   */
  public GantBinding ( ) { initializeGantBinding ( ) }
  /**
   *  Constructor taking an explicit <code>Binding</code> as parameter.
   *
   *  @param binding The <code>Binding</code> to use as a base of maplets to initialize the
   *  <code>GantBinding</code> with.
   */
  public GantBinding ( final Binding binding ) {
    super ( binding.variables )
    initializeGantBinding ( )
  }
  /**
   *  Method holding all the code common to all construction.
   */
  private void initializeGantBinding ( ) {
    //
    //  When this class is instantiated from a Gant command line or via a Groovy script then the classloader
    //  is a org.codehaus.groovy.tools.RootLoader, and is used to load all the Ant-related classes.  This
    //  means that all Ant classes already know about all the Groovy jars in the classpath.  When this class
    //  is instantiated from the Gant Ant Task or from a Groovy Ant task script, all the Ant classes have
    //  already been loaded using an instance of URLLoader and have no knowledge of the Groovy jars.
    //  Fortunately, this class has to have been loaded by an org.apache.tools.ant.AntClassLoader which does
    //  have all the necessary classpath information.  In this situation we must force a reload of the
    //  org.apache.tools.ant.Project class so that it has the right classpath.
    //
    //  Having said this there is no unit test to require making the change!
    //
    //  GANT-50 raises the issue that in an Ant initiated context, the org.apache.tools.ant.Project backing
    //  the GantBuilder instance should be the already existing one so that the basedir is correct -- which
    //  it isn't up to the revision that fixes GANT-50.
    //
    //  NB The property ant.library.dir seems to be set only when Gant is started in an Ant initiated
    //  context.
    //
    final classLoader = getClass ( ).classLoader
    if ( classLoader.class.name == "org.apache.tools.ant.AntClassLoader" ) {
      //
      //  Need to get a reference to the already existing org.apache.tools.ant.Project instance.  How to do
      //  this?
      //
      //final project = classLoader.forceLoadClass ( 'org.apache.tools.ant.Project' ).newInstance ( )
      //project.init ( )
      //setVariable ( ant , new GantBuilder ( project ) )
      setVariable ( 'ant' , new GantBuilder ( ) )
    }
    else { setVariable ( 'ant' , new GantBuilder ( ) ) }
    //  Do not allow the output to escape.  The problem here is that if the output is allowed out then
    //  Ant, Gant, Maven, Eclipse and IntelliJ IDEA all behave slightly differently.  This makes testing
    //  nigh on impossible.  Also the user doesn't need to know about these.
    final outSave = System.out
    System.setOut ( new PrintStream ( new ByteArrayOutputStream ( ) ) )
    ant.property ( environment : 'environment' )
    System.setOut ( outSave )
    //  Ensure Ant as well as ant is available to ensure backward compatibility.
    setVariable ( 'Ant' , getVariable ( 'ant' ) )
    setVariable ( 'includeTargets' , new IncludeTargets ( this ) )
    setVariable ( 'includeTool' , new IncludeTool ( this ) )
    setVariable ( 'target' , { Map<String, String> map , Closure closure ->
        switch ( map.size ( ) ) {
         case 0 : throw new RuntimeException ( 'Target specified without a name.' )
         case 1 : break
         default : throw new RuntimeException ( 'Target specified with multiple names.' )
        }
        def targetName = map.keySet ( ).iterator ( ).next ( )
        try {
          owner.getVariable ( targetName )
          //
          //  Exceptions thrown in this Closure appear not to cause execution to enter an error path.  Must
          //  find out how to throw an exception from a Closure.
          //
          //throw new RuntimeException ( "Attempt to redefine " + targetName )
          //
          System.err.println ( 'Warning, target causing name overwriting of name ' + targetName )
          //System.exit ( -101 )
        }
        catch ( MissingPropertyException ) { }
        def targetDescription = map.get ( targetName )
        if ( targetDescription ) { targetDescriptions.put ( targetName , targetDescription ) }
        closure.metaClass = new GantMetaClass ( closure.metaClass , owner )
        owner.setVariable ( targetName , closure )
        owner.setVariable ( targetName + '_description' , targetDescription )
      } )
    setVariable ( 'task' , { Map<String, String> map , Closure closure ->
        System.err.println ( "task has now been removed from Gant, please update your Gant files to use target instead of task." )
        System.exit ( -99 ) ;
      } )             
    setVariable ( 'targetDescriptions' , new TreeMap ( ) )
    setVariable ( 'message' , { String tag , Object message ->
        def padding = 9 - tag.length ( )
        if ( padding < 0 ) { padding = 0 }
        println ( "           ".substring ( 0 , padding ) + '[' + tag + '] ' + message )
      } )
    setVariable ( 'setDefaultTarget' , { defaultTarget -> // Deal with Closure or String arguments.
         switch ( defaultTarget.getClass ( ) ) {
          case Closure :
          def targetName = null
          owner.variables.each { key , value -> if ( value.is ( defaultTarget ) ) { targetName = key } }
          if ( targetName == null ) { throw new RuntimeException ( 'Parameter to setDefaultTarget method is not a known target.  This can never happen!' ) }
          owner.target.call ( 'default' : targetName ) { defaultTarget ( ) }
          break
          case String :
          def failed = true
          try {
            def targetClosure = owner.getVariable ( defaultTarget )
            if ( targetClosure != null ) { owner.target.call ( 'default' : defaultTarget ) { targetClosure ( ) } ; failed = false }
          }
          catch ( MissingPropertyException mpe ) { }
          if ( failed ) { throw new RuntimeException ( "Target ${defaultTarget} does not exist so cannot be made the default." ) }
          break
          default :
          throw new RuntimeException ( 'Parameter to setDefaultTarget is of the wrong type -- must be a target reference or a string.' )
          break
         }
      } )
    setVariable ( 'cacheEnabled' , false )
    def item = System.getenv ( ).GANTLIB ;
    if ( item == null ) { gantLib = [ ] }
    else { gantLib = Arrays.asList ( item.split ( System.properties.'path.separator' ) ) }
    initializing = false
  }
  /**
   *  The method for getting values from the binding.  Ensures that Ant properties appear to be in the binding object.
   */
  Object getVariable ( final String name ) {
    Object returnValue = null
    try { returnValue = super.getVariable ( name ) }
    catch ( final MissingPropertyException mpe ) {
      returnValue = super.getProperty ( 'ant' ).getProject ( ).getProperty ( name )
      if ( returnValue == null ) { throw mpe }
    }
    returnValue
  }
  /**
   *  The method for setting values in the binding.  Ensures that read-only values cannot be reset after
   *  initialization.
   *
   *  @param name The symbol to define.
   *  @param value The value to associate with the name.
   */
  void setVariable ( final String name , final Object value ) {
    if ( ! initializing && [ 'target' , 'message' ].contains ( name ) ) { throw new RuntimeException ( 'Cannot redefine symbol ' + name ) }
    super.setVariable ( name , value )
  }
}
