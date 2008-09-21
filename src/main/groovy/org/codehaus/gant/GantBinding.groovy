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
  public GantBinding ( ) {
    setVariable ( 'ant' , new GantBuilder ( ) )
    initializeGantBinding ( )
  }
  /**
   *  Constructor taking an explicit <code>Binding</code> as parameter.
   *
   *  @param binding The <code>Binding</code> to use as a base of maplets to initialize the
   *  <code>GantBinding</code> with.
   */
  //  TODO : Check to see if this constructor is ever needed.
  public GantBinding ( final Binding binding ) {
    super ( binding.variables )
    setVariable ( 'ant' , new GantBuilder ( ) )
    initializeGantBinding ( )
  }
  /**
   *  Constructor taking an explicit <code>org.apache.tools.ant.Project</code> as parameter.
   *
   *  @param p The <code>org.apache.tools.ant.Project</code> to use when initializing the
   *  <code>GantBuilder</code>.
   */
  public GantBinding ( final org.apache.tools.ant.Project p ) {
    setVariable ( 'ant' , new GantBuilder ( p ) )
    initializeGantBinding ( )
  }
  /**
   *  Method holding all the code common to all construction.
   */
  private void initializeGantBinding ( ) {
    //  Do not allow the output to escape.  The problem here is that if the output is allowed out then
    //  Ant, Gant, Maven, Eclipse and IntelliJ IDEA all behave slightly differently.  This makes testing
    //  nigh on impossible.  Also the user doesn't need to know about these.
    final outSave = System.out
    System.out = new PrintStream ( new ByteArrayOutputStream ( ) )
    ant.property ( environment : 'environment' )
    System.out = outSave
    setVariable ( 'Ant' , [
      invokeMethod : { String s , Object o ->
        System.err.println ( 'Ant is deprecated, please amend your Gant files to use ant instead of Ant.' )
        ant.invokeMethod ( s , o )
      }
    ] as GantBuilder )
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
        catch ( MissingPropertyException mpe ) { /* Intentionally empty */ }
        def targetDescription = map [ targetName ]
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
         switch ( defaultTarget.class ) {
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
           catch ( MissingPropertyException mpe ) { /* Intentionally empty. */ }
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
    def returnValue
    try { returnValue = super.getVariable ( name ) }
    catch ( final MissingPropertyException mpe ) {
      returnValue = super.getProperty ( 'ant' ).project.getProperty ( name )
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
