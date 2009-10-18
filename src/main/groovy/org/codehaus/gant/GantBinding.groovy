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

package org.codehaus.gant

import org.apache.tools.ant.BuildListener
import org.apache.tools.ant.Project
import org.apache.tools.ant.Target

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
   * A List of BuildListener instances that Gant sends events to.
   */
  private List<BuildListener> buildListeners = [ ]
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
   *  Constructor taking an explicit <code>Project</code> as parameter.
   *
   *  @param p The <code>Project</code> to use when initializing the <code>GantBuilder</code>.
   */
  public GantBinding ( final Project p ) {
    setVariable ( 'ant' , new GantBuilder ( p ) )
    initializeGantBinding ( )
  }
  /**
   *  Adds a <code>BuildListener</code> instance to this <code>Gant</code> instance
   */
  public synchronized void addBuildListener ( final BuildListener buildListener ) {
    if ( buildListener ) {
      buildListeners << buildListener
      ant.antProject.addBuildListener ( buildListener )
    }
  }
  /**
   *  Removes a <code>BuildListener</code> instance from this <code>Gant</code> instance
   */
  public synchronized void removeBuildListener ( final BuildListener buildListener ) {
    buildListeners.remove ( buildListener )
    ant.antProject.removeBuildListener ( buildListener )
  }
  /**
   *  Call a target wrapped in <code>BuildListener</code> event handler.
   */
  private withTargetEvent ( targetName , targetDescription , Closure callable ) {
    def antTarget = new Target ( name : targetName , project : ant.antProject , description : targetDescription )
    def event = new GantEvent ( antTarget , this )
    def targetResult = null
    try {
      buildListeners.each { BuildListener b -> b.targetStarted ( event ) }
      targetResult = callable.call()
      buildListeners.each { BuildListener b -> b.targetFinished ( event ) }
    }
    catch ( Exception e ) {
      event.exception = e
      buildListeners.each { BuildListener b -> b.targetFinished ( event ) }
      throw e
    }
    return targetResult
  }
  /**
   *  Method holding all the code common to all construction.
   */
  private void initializeGantBinding ( ) {
    //  Do not allow the output of the ant.property call to escape.  If the output is allowed out then Ant,
    //  Gant, Maven, Eclipse and IntelliJ IDEA all behave slightly differently.  This makes testing nigh on
    //  impossible.  Also the user doesn't need to know about these.
    ant.logger.setMessageOutputLevel ( GantState.SILENT )
    ant.property ( environment : 'environment' )
    ant.logger.setMessageOutputLevel ( GantState.verbosity )
    super.setVariable ( 'includeTargets' , new IncludeTargets ( this ) )
    super.setVariable ( 'includeTool' , new IncludeTool ( this ) )
    super.setVariable ( 'globalPreHook' , null )
    super.setVariable ( 'globalPostHook' , null )
    super.setVariable ( 'terminateHook' , { int returnValue , String elapseTime ->
        owner.ant.project.log ( '\nBUILD ' + ( returnValue == 0 ? 'SUCCESSFUL' : 'FAILED' ) )
        owner.ant.project.log ( 'Total time: ' + elapseTime )
      } )
    super.setVariable ( 'target' , { Map<String, String> map , Closure closure ->
        def targetName = ''
        def targetDescription = ''
        def nameKey = 'name'
        def descriptionKey = 'description'
        Map targetMap = [:]
        if  ( ! map || map.size ( ) == 0 ) { throw new RuntimeException ( 'Target specified without a name.' ) }
        //  target ( name : 'flob' ) is treated as a specification of target flob.  
        if ( map.size ( ) == 1 &&  ! map[nameKey] ) {
          // Implicit style of specifying a target and description.
          targetName = map.keySet ( ).iterator ( ).next ( )
          targetDescription = map[targetName]
          // Create fake name/description entries in the map so that targets closures can still take
          // advantage of it.name and it.description
          targetMap[nameKey]  = targetName
          targetMap[descriptionKey]  = targetDescription
        }
        else {
          // Explicit style of specifying target name (and possibly description)
          targetName = map[nameKey]
          targetDescription = map[descriptionKey]
          targetMap.putAll ( map )
        }
        if ( ! targetName ) { throw new RuntimeException ( 'Target specified without a name.' ) }
        try {
          owner.getVariable ( (String) targetName )
          //
          //  Exceptions thrown in this Closure appear not to cause execution to enter an error path.  Must
          //  find out how to throw an exception from a Closure.
          //
          //throw new RuntimeException ( "Attempt to redefine " + targetName )
          //
          owner.binding.ant.project.log ( 'Warning, target causing name overwriting of name ' + targetName , Project.MSG_WARN )
          //System.exit ( -101 )
        }
        catch ( MissingPropertyException mpe ) { /* Intentionally empty */ }
        if ( targetDescription ) { targetDescriptions.put ( targetName , targetDescription ) }
        closure.metaClass = new GantMetaClass ( closure.metaClass , owner )
        if ( ! targetMap.containsKey ( 'prehook' ) ) { targetMap.prehook = [ { -> owner.ant.project.log ( targetName + ':' ) } ] }
        if ( ! targetMap.containsKey ( 'posthook' ) ) { targetMap.posthook = [ { -> owner.ant.project.log ( '------ ' + targetName ) } ] }
        if ( targetMap.containsKey ( 'addprehook' ) ) {
          if ( targetMap.prehook instanceof Closure ) { targetMap.prehook = [ targetMap.prehook ] }
          if ( targetMap.addprehook instanceof List ) { targetMap.prehook += targetMap.addprehook }
          else { targetMap.prehook += [ targetMap.addprehook ] }
        }
        if ( targetMap.containsKey ( 'addposthook' ) ) {
          if ( targetMap.posthook instanceof Closure ) { targetMap.posthook = [ targetMap.posthook ] }
          if ( targetMap.addposthook instanceof List ) { targetMap.posthook = targetMap.addposthook + targetMap.posthook }
          else {  targetMap.posthook = [ targetMap.addposthook ] + targetMap.posthook }
        }
        final targetClosure =  {
          def returnCode = 0
          def runHooks = { hook , String label ->
            if ( hook ) {
              if ( hook instanceof Closure ) { hook.call ( ) }
              else if ( hook instanceof List ) {
                for ( item in hook ) {
                  if ( item instanceof Closure ) { item.call ( ) }
                  else { owner.ant.project.log ( label + ' list item is not a closure.' , Project.MSG_ERR ) }
                }
              }
              else { owner.ant.project.log ( label + ' not a closure or list (of closures).' , Project.MSG_ERR ) }
            }
          }
          runHooks ( owner.globalPreHook , 'Global prehook' )
          runHooks ( targetMap.prehook , 'Target prehook' )
          withTargetEvent ( targetName , targetDescription ) { returnCode = closure ( targetMap ) }
          runHooks ( targetMap.posthook , 'Target posthook' )
          runHooks ( owner.globalPostHook , 'Global posthook' )
          returnCode
        }
        owner.setVariable ( (String) targetName , targetClosure )
        owner.setVariable ( targetName + '_description' , targetDescription )  //  For backward compatibility.
      } )
    super.setVariable ( 'task' , { Map<String, String> map , Closure closure ->
        owner.ant.project.log ( 'task has now been removed from Gant, please update your Gant files to use target instead of task.' , Project.MSG_ERR )
        System.exit ( -99 ) ;
      } )
    super.setVariable ( 'targetDescriptions' , new TreeMap ( ) )
    super.setVariable ( 'message' , { String tag , Object message ->
        def padding = 9 - tag.length ( )
        if ( padding < 0 ) { padding = 0 }
        owner.ant.project.log ( "           ".substring ( 0 , padding ) + '[' + tag + '] ' + message )
      } )
    super.setVariable ( 'setDefaultTarget' , { defaultTarget -> // Deal with Closure or String arguments.
         switch ( defaultTarget.class ) {
          case Closure :
           String defaultTargetName = null
           owner.variables.each { key , value -> if ( value.is ( defaultTarget ) ) { defaultTargetName = key } }
           if ( defaultTargetName == null ) { throw new RuntimeException ( 'Parameter to setDefaultTarget method is not a known target.' ) }
           else { owner.forcedSettingOfVariable ( 'defaultTarget' , defaultTargetName ) }
           break
          case String :
           owner.forcedSettingOfVariable ( 'defaultTarget' , defaultTarget )
           break
          default :
           throw new RuntimeException ( 'Parameter to setDefaultTarget is of the wrong type -- must be a target reference or a string.' )
         }
      } )
    super.setVariable ( 'defaultTarget' , 'default' )
    super.setVariable ( 'setFinalizeTarget' , { finalizeTarget -> // Deal with Closure or String arguments.
         switch ( finalizeTarget.class ) {
          case Closure :
           String finalizeTargetName = null
           owner.variables.each { key , value -> if ( value.is ( finalizeTarget ) ) { finalizeTargetName = key } }
           if ( finalizeTargetName == null ) { throw new RuntimeException ( 'Parameter to setFinalizeTarget method is not a known target.' ) }
           else { owner.forcedSettingOfVariable ( 'finalizeTarget' , finalizeTargetName ) }
           break
          case String :
           owner.forcedSettingOfVariable ( 'finalizeTarget' , finalizeTarget )
           break
          default :
           throw new RuntimeException ( 'Parameter to setFinalizeTarget is of the wrong type -- must be a target reference or a string.' )
         }
      } )
    super.setVariable ( 'finalizeTarget' , 'finalize' )
    super.setVariable ( 'cacheEnabled' , false )
    final item = System.getenv ( ).GANTLIB ;
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
      returnValue = super.getVariable ( 'ant' )?.project?.getProperty ( name )
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
    if ( ! initializing && [
                            'target' ,
                            'message' ,
                            'ant' ,
                            'includeTargets' ,
                            'includeTool' ,
                            'targetDescriptions' ,
                            'setDefaultTarget' ,
                            'initiatingTarget' ,
                            'targets' ,
                            'defaultTarget' ,
                            'finalizeTarget' ,
                            ].contains ( name ) ) { throw new RuntimeException ( 'Cannot redefine symbol ' + name ) }
    super.setVariable ( name , value )
  }
  /**
   *  <code>setVariable</code> includes tests for certain names so as to make them read only as far as the
   *  Gant script is concerned.  However the implementation code needs to be able to circumvent that
   *  checking, and so we provide this method for implementation code to force things at times other than
   *  initialization.  This need came about in realizing GANT-44.
   *
   *  @param ant the <code>GantBuilder</code> to assign to the 'ant' entry in the binding.
   */
  void forcedSettingOfVariable ( final String name , final Object value ) { super.setVariable ( name , value ) }
  /**
   *  Getter for the list of build listeners.  Used in {@code gant.Gant.withBuildListeners}.
   */
  List<BuildListener> getBuildListeners ( ) { buildListeners }
}
