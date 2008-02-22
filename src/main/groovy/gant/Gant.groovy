//  Gant -- A Groovy build framework based on scripting Ant tasks.
//
//  Copyright © 2006-8 Russel Winder
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

package gant

import java.lang.reflect.InvocationTargetException

import org.codehaus.groovy.control.MultipleCompilationErrorsException    
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.CompilationUnit

import org.codehaus.gant.GantBuilder
import org.codehaus.gant.GantMetaClass
import org.codehaus.gant.GantState
import org.codehaus.gant.IncludeTargets
import org.codehaus.gant.IncludeTool

import org.apache.commons.cli.GnuParser
import org.apache.commons.cli.Option
import org.apache.commons.cli.OptionBuilder

/**
 *  This class provides infrastructure and an executable command for using Groovy + AntBuilder as a build
 *  tool in a way similar to Rake and SCons.  However, where Rake and SCons are dependency programming
 *  systems based on Ruby and Python respectively, Gant is simply a way of scripting Ant tasks; the Ant
 *  tasks do all the dependency management.
 *
 *  <p>A Gant build specification file (default name build.gant) is assumed to contain one or more targets.
 *  Dependencies between targets are handled as function calls within functions, or by use of the depends
 *  function. Execution of Ant tasks is by calling methods on the object called `Ant', which is predefined
 *  as an <code>GantBuilder</code> instance.</p>
 *
 *  <p>On execution of the gant command, the Gant build specification is read and executed in the context of
 *  a predefined binding.  An object called `Ant' is part of the binding so methods can use this object to
 *  get access to the Ant tasks without having to create an object explicitly.  A method called `target' is
 *  part of the predefined binding.  A target has two parameters, a single item map and a closure.  The
 *  single item map has the target name as the key and the documentation about the target as the value.
 *  This documentation is used by the `gant -T' / `gant --targets' / `gant -p' command to present a list of
 *  all the documented targets.</p>
 *
 *  <p>NB In the following example some extra spaces have had to be introduced because some of the patterns
 *  look like comment ends:-(</p>
 * 
 *  <p>A trivial example build specification is:</p>
 *
 *  <pre>
 *      target ( stuff : 'A target to do some stuff.' ) {
 *        clean ( )
 *        otherStuff ( )
 *      }
 *      target ( otherStuff : 'A target to do some other stuff' ) {
 *        depends ( clean )
 *      }
 *      target ( clean : 'Clean the directory and subdirectories' ) {
 *        delete ( dir : 'build' , quiet : 'true' )
 *        delete ( quiet : 'true' ) { fileset ( dir : '.' , includes : '** /*~,** /*.bak'  , defaultexcludes : 'false' ) }
 *      }
 *      setDefaultTarget ( stuff )
 * </pre>
 *
 *  <p>or, using some a ready made targets class:</p>
 *
 *  <pre>
 *      includeTargets << gant.targets.Clean
 *      cleanPattern << [ '** / *~' , '** / *.bak' ]
 *      cleanDirectory << 'build'
 *      target ( stuff : 'A target to do some stuff.' ) {
 *        clean ( )
 *        otherStuff ( )
 *      }
 *      target ( otherStuff : 'A target to do some other stuff' ) {
 *        depends ( clean )
 *      }
 *      setDefaultTarget ( stuff )
 *  </pre>
 *
 *  <p><em>Note that there is an space between the two asterisks and the solidus in the fileset line that
 *  should notbe there, we have to have it in the source because asterisk followed by solidus is end of
 *  comment in Groovy</em></p>
 *
 *  @author Russel Winder <russel.winder@concertant.com>
 *  @author Graeme Rocher <graeme.rocher@gmail.com>
 */
final class Gant {
  private buildFileName
  private buildClassName
  private final Binding binding
  private final ClassLoader classLoader
  private final GroovyShell groovyShell
  private final targetDescriptions = new TreeMap ( ) 
  private final target = { Map map , Closure closure ->
    switch ( map.size ( ) ) {
     case 0 : throw new RuntimeException ( 'Target specified without a name.' )
     case 1 : break
     default : throw new RuntimeException ( 'Target specified with multiple names.' )
    }
    def targetName = map.keySet ( ).iterator ( ).next ( )
    def targetDescription = map.get ( targetName )
    if ( targetDescription ) { targetDescriptions.put ( targetName , targetDescription ) }
    closure.metaClass = new GantMetaClass ( closure.class , binding )
    binding.setVariable ( targetName , closure )
    binding.setVariable ( targetName + '_description' , targetDescription )
  }
  private final message = { String tag , Object message ->
    def padding = 9 - tag.length ( )
    if ( padding < 0 ) { padding = 0 }
    println ( "           ".substring ( 0 , padding ) + '[' + tag + '] ' + message )
  }
  private final setDefaultTarget = { defaultTarget -> // Deal with Closure or String arguments.
    switch ( defaultTarget.getClass ( ) ) {
     case Closure :
      def targetName = null
      binding.variables.each { key , value -> if ( value.is ( defaultTarget ) ) { targetName = key } }
      if ( targetName == null ) { throw new RuntimeException ( 'Parameter to setDefaultTarget method is not a known target.  This can never happen!' ) }
      target ( 'default' : targetName ) { defaultTarget ( ) }
      break
     case String :
      def failed = true
      try {
        def targetClosure = binding.getVariable ( defaultTarget )
        if ( targetClosure != null ) { target ( 'default' : defaultTarget ) { targetClosure ( ) } ; failed = false }
      }
      catch ( MissingPropertyException mpe ) { }
      if ( failed ) { throw new RuntimeException ( "Target ${defaultTarget} does not exist so cannot be made the default." ) }
      break
     default :
      throw new RuntimeException ( 'Parameter to setDefaultTarget is of the wrong type -- must be a target reference or a string.' )
      break 
    }
  }
  private final ant
  /*
   *  There are issues with this use of instance initializers in Groovy as at r10228.  Doing a compilation
   *  from clean means this initializer does not get executed.  Causing this file to be compiled then means
   *  everything works correctly.  For the moment, as a hack, remove the use of instance initializers in
   *  favour of putting the code into the constructor.
   *
  private final List gantLib ; {
    //  System.getenv is deprecated on 1.4, so we use Ant to access the environment.  Can remove this once
    //  Groovy depends on 1.5.
    //
    // def item = System.getenv ( ).GANTLIB ;
    def item = ant.project.properties.'environment.GANTLIB'
    if ( item == null ) { gantLib = [ ] }
    else { gantLib = Arrays.asList ( item.split ( System.properties.'path.separator' ) ) }
  }
  */
  private final List gantLib = [ ]
   /*
    */
   /**
    *  Constructor that uses build.gant as the build script, creates a new instance of <code>Binding</code>
    *  for the script binding, and the default class loader.
    */
  public Gant ( ) { this ( 'build.gant' , null , null ) }
   /**
    *  Constructor that uses build.gant as the build script, the passed <code>Binding</code> for the script
    *  binding, and the default class loader.
    */
  public Gant ( Binding b ) { this (  'build.gant' , b , null ) }
   /**
    *  Constructor that uses build.gant as the build script, the passed <code>Binding</code> for the script
    *  binding, and the passed <code>ClassLoader</code> as the class loader.
    */
  public Gant ( Binding b , ClassLoader cl ) { this (  'build.gant' , b , cl ) }
   /**
    *  Constructor that uses the filename passed as a parameter as the build script, creates a new instance
    *  of <code>Binding</code> for the script binding, and uses the default class loader.
    */
  public Gant ( File f ) { this ( f.name , null , null ) }  
   /**
    *  Constructor that uses the filename passed as a parameter as the build script, the passed
    *  <code>Binding</code> for the script binding, and the default class loader.
    */
  public Gant ( File f , Binding b ) { this ( f.name , b , null ) }
   /**
    *  Constructor that uses the filename passed as a parameter as the build script, the passed
    *  <code>Binding</code> for the script binding, the passed <code>ClassLoader</code> as the class loader.
    */
  public Gant ( File f , Binding b , ClassLoader cl ) { this ( f.name , b , cl ) }
   /**
    *  Constructor that uses the filename passed as a parameter as the build script, creates a new instance
    *  of <code>Binding</code> for the script binding and uses the default class loader.
    */
  public Gant ( String s ) { this ( s , null , null ) }
   /**
    *  Constructor that uses the filename passed as a parameter as the build script, the passed
    *  <code>Binding</code> for the script binding, and uses the default class loader.
    */
  public Gant ( String s , Binding b ) { this ( s , b , null ) }  
   /**
    *  Constructor that uses the filename passed as a parameter as the build script, the passed
    *  <code>Binding</code> for the script binding, the passed <code>ClassLoader</code> as the class loader.
    */
  public Gant ( String s , Binding b , ClassLoader cl ) {
    //
    //  When this class is instantiated from a Gant command line or via a Groovy script then the classloader
    //  is a org.codehaus.groovy.tools.RootLoader, and is used to load all the Ant related classes.  This
    //  means that all Ant classes already know about all the Groovy jars in the classpath.  When this class is
    //  instantiated from the Gant Ant Task, all the Ant classes have already been loaded using an instance
    //  of URLLoader and have no knowledge of the Groovy jars.  Fortunately, this class has to have been
    //  loaded by an org.apache.tools.ant.AntClassLoader which does have all the necessary classpath
    //  information.  In this situation we must force a reload of the org.apache.tools.ant.Project class so
    //  that it has the right classpath.
    //
    final classLoader = getClass ( ).classLoader

    System.err.println ( classLoader.class.name )
    System.err.println ( classLoader )

    if ( classLoader.class.name == "org.apache.tools.ant.AntClassLoader" ) {
      //final project = classLoader.forceLoadClass ( 'org.apache.tools.ant.Project' ).newInstance ( )
      //project.init ( )
      //ant = new GantBuilder ( project )
      ant = new GantBuilder ( )
    }
    else { ant = new GantBuilder ( ) } 
    /*
     *  Move things here from the instance initializers.
     */
    ant.property ( environment : 'environment' )
    /*
     */
    buildFileName = s
    buildClassName = buildFileName.replaceAll ( '\\.' , '_' )
    binding = ( b != null ) ? b : new Binding ( )
    this.classLoader = ( cl != null ) ? cl : classLoader
    groovyShell = new GroovyShell ( this.classLoader , binding )
    binding.gantLib = gantLib
    binding.Ant = ant
    binding.groovyShell = groovyShell
    binding.includeTargets = new IncludeTargets ( binding )
    binding.includeTool = new IncludeTool ( binding )
    binding.targetDescriptions = targetDescriptions
    binding.target = target
    binding.task = { Map map , Closure closure -> System.err.println ( 'Deprecation warning: Use of task instead of target is deprecated.' ) ; target ( map , closure ) }
    binding.message = message
    binding.setDefaultTarget = setDefaultTarget
    binding.cacheEnabled = false
  }
  /**
   *  The function that implements the creation of the list of targets for the -p and -T options.
   */
  private int targetList ( targets ) {
    def max = 0
    targetDescriptions.entrySet ( ).each { item ->
      if ( item.key != 'default' ) {
        def size = item.key.size ( )
        if ( size > max ) { max = size }
      }
    }
    println ( )
    targetDescriptions.entrySet ( ).each { item ->
      if ( item.key != 'default' ) {
        println ( ' ' + item.key + ' ' * ( max - item.key.size ( ) ) + '  ' + item.value )
      }
    }
    println ( )
    def message = targetDescriptions [ 'default' ]
    if ( message != null ) { println ( 'Default target is ' + message + '.' ) ; println ( ) }
    0
  }
  private void printDispatchExceptionMessage ( target , method , message ) {
    println ( ( target == method ) ? "Target ${method} does not exist." : "Could not execute method ${method}.\n${message}" )
  }
  /**
   *  The function that handles actioning the targets.
   */
  private int dispatch ( targets ) {
    def returnCode = 0
    try {
      if ( targets.size ( ) > 0 ) {
        targets.each { target ->
          try { binding.getVariable ( target ).run ( ) }
          catch ( MissingPropertyException mme ) {
            printDispatchExceptionMessage ( target , mme.property , mme.message )
            returnCode = 11
          }
        }
      }
      else {
        try { binding.getVariable ( 'default' ).run ( ) }
        catch ( MissingPropertyException mme ) {
          printDispatchExceptionMessage ( 'default' , mme.property , mme.message )
          returnCode = 12
        }
      }
    }
    catch ( Exception e ) {
      println ( e.message )
      returnCode = 13
    }
    returnCode
  }
  /**
   *  Process the command line options and then call the function to process the targets.
   */
  public int processArgs ( String[] args ) {
    final rootLoader = classLoader.rootLoader
    //
    //  Commons CLI is broken.  1.0 has one set of ideas about multiple args and is broken.  1.1 has a
    //  different set of ideas about multiple args and is broken.  For the moment we leave things so that
    //  they work in 1.0.
    //
    //  1.0 silently absorbs unknown single letter options.
    //
    //  1.0 cannot deal with options having only a long form as the access mechanism that works only works
    //  for short form.
    //
    //  The PosixParser does not handle incorrectly formed options at all well.  Also the standard printout
    //  actually assumes GnuParser form.  So although PosixParser is the default for CliBuilder, we actually
    //  want GnuParser.
    //
    def cli = new CliBuilder ( usage : 'gant [option]* [target]*' , parser : new GnuParser ( ) )
    cli.c ( longOpt : 'usecache' , 'Whether to cache the generated class and perform modified checks on the file before re-compilation.' )
    cli.d ( longOpt : 'cachedir' , args : 1 , argName : 'cache-file' , 'The directory where to cache generated classes to.' )
    cli.f ( longOpt : 'gantfile' , args : 1 , argName : 'build-file' , 'Use the named build file instead of the default, build.gant.' )
    cli.h ( longOpt : 'help' , 'Print out this message.' )
    //  This options should have "args : Option.UNLIMITED_VALUES" but that doesn't work.
    cli.l ( longOpt : 'gantlib' , args : 1 , argName : 'library' , 'A directory that contains classes to be used as extra Gant modules,' )
    cli.n ( longOpt : 'dry-run' , 'Do not actually action any tasks.' )
    cli.p ( longOpt : 'projecthelp' , 'Print out a list of the possible targets.' )
    cli.q ( longOpt : 'quiet' , 'Do not print out much when executing.' )
    cli.s ( longOpt : 'silent' , 'Print out nothing when executing.' )
    cli.v ( longOpt : 'verbose' , 'Print lots of extra information.' )
    //  This options should have "args : Option.UNLIMITED_VALUES" but that doesn't work.
    cli.D ( argName : 'name>=<value' , args : 1 , 'Define <name> to have value <value>.  Creates a variable named <name> for use in the scripts and a property named <name> for the Ant tasks.' )
    cli.L ( longOpt : 'lib' , args : 1 , argName : 'path' , 'Add a directory to search for jars and classes.' )
    cli.P ( longOpt : 'classpath' , args : 1 , argName : 'path' , 'Specify a path to search for jars and classes.' )
    cli.T ( longOpt : 'targets' , 'Print out a list of the possible targets.' )
    cli.V ( longOpt : 'version' , 'Print the version number and exit.' )
    def options = cli.parse ( args )
    if ( options == null ) { println ( 'Error in processing command line options.' ) ; return 1 }
    binding.cacheEnabled = options.c ? true : false
    binding.cacheDirectory = binding.cacheEnabled && options.d ? new File ( options.d ) : new File ( "${System.properties.'user.home'}/.gant/cache" )
    if ( options.f ) {
      buildFileName = options.f
      buildClassName = buildFileName.replaceAll ( '\\.' , '_' ) 
    }
    if ( options.h ) { cli.usage ( ) ; return 0 }
    if ( options.l ) { gantLib << options.l.split ( System.properties.'path.separator' ) }
    if ( options.n ) { GantState.dryRun = true }
    def function =  ( options.p || options.T ) ? 'targetList' : 'dispatch'
    if ( options.q ) { GantState.verbosity = GantState.QUIET }
    if ( options.s ) { GantState.verbosity = GantState.SILENT }
    if ( options.v ) { GantState.verbosity = GantState.VERBOSE }
    if ( options.D ) {
      options.Ds.each { definition ->
        def pair = definition.split ( '=' ) as List
        if ( pair.size ( ) < 2 ) { pair << '' }
        binding.Ant.property ( name : pair[0] , value : pair[1] )
        binding.setVariable ( pair[0] , pair[1] )
      }
    }
    if ( options.L ) { options.Ls.each { directoryName ->
        def directory = new File ( directoryName )
        if ( directory.isDirectory ( ) ) { directory.eachFile { item -> rootLoader?.addURL ( item.toURL ( ) ) } }
        else {
          println ( 'Parameter to -L|--lib option is not a directory: ' + directory.name )
        }
      } }
    if ( options.P ) { options.P.split ( System.properties.'path.separator' ).each { pathitem -> rootLoader?.addURL ( ( new File ( pathitem ) ).toURL ( ) ) } }
    if ( options.V ) {
      def version = ''
      final gantPackage = Package.getPackage ( 'gant' )
      if ( gantPackage != null ) { version = gantPackage.getImplementationVersion ( ) }
      println ( 'Gant version ' + ( ( version == null ) ? '<unknown>' : version ) )
      return 0
    }
    //  The rest of the arguments appear to be delivered as a single string as the first item in a list.  This is surely an error but
    //  with Commons CLI 1.0 it is the case.  So we must partition.  NB the split method delivers an array
    //  of Strings so we cast to a List.
    def targets = options.arguments ( )
    if ( ( targets != null ) && ( targets.size ( ) == 1 ) ) { targets = targets[ 0 ].split ( ' ' ) as List }
    def gotUnknownOptions = false ;
    targets.each { target ->
      if ( target[0] == '-' ) {
        println ( 'Unknown option: ' + target ) 
        gotUnknownOptions = true
      }
    }
    if ( gotUnknownOptions ) { cli.usage ( ) ; return 1 ; }
    def userAntLib = new File ( "${System.properties.'user.home'}/.ant/lib" )
    if ( userAntLib.isDirectory ( ) ) { userAntLib.eachFile { file -> rootLoader?.addURL ( file.toURL ( ) ) } }
    def userGantLib = new File ( "${System.properties.'user.home'}/.gant/lib" )
    if ( userGantLib.isDirectory ( ) ) { userGantLib.eachFile { file -> rootLoader?.addURL ( file.toURL ( ) ) } }
    //def antHome = System.getenv ( ).'ANT_HOME'
    def antHome = ant.project.properties.'environment.ANT_HOME'
    if ( ( antHome != null ) && ( antHome != '' ) ) {
      def antLib = new File ( antHome + '/lib' )
      if ( antLib.isDirectory ( ) ) { antLib.eachFileMatch ( ~/ant-.*\.jar/ ) { file -> rootLoader?.addURL ( file.toURL ( ) ) } }
    }
    processTargets ( function , targets )
  }
  public int processTargets ( ) { processTargets ( 'dispatch' , [ ] ) }
  public int processTargets ( String s ) { processTargets ( 'dispatch' , [ s ] ) }
  public int processTargets ( List l ) { processTargets ( 'dispatch' , l ) }
  /**
   *  Process the targets, but first deal with getting the build script loaded, either by compiling the text
   *  of the file or standard input, or using the cached compiled file.
   */
  protected int processTargets ( String function , List targets ) {
    def buildFileText = ''
    def buildFileModified = -1  
    def buildFile = null
    def standardInputClassName = 'standard_input'
    if ( buildFileName == '-' ) {
      buildFileText = System.in.text
      buildClassName = standardInputClassName
    }
    else {
      buildFile = new File ( buildFileName ) 
      if ( ! buildFile.isFile ( ) ) { println ( 'Cannot open file ' + buildFileName ) ; return 3 }
      buildClassName = buildFile.name.replaceAll ( /\./ , '_' )
      buildFileModified = buildFile.lastModified ( )
    }
    if ( binding.cacheEnabled ) {       
      if ( buildFile == null ) { println 'Caching can only be used in combination with the -f option.' ; return 1 }
      def cacheDirectory = binding.cacheDirectory
      if ( classLoader instanceof URLClassLoader ) { classLoader.addURL ( cacheDirectory.toURL ( ) ) }
      else { rootLoader?.addURL ( cacheDirectory.toURL ( ) ) }      
      def loadClassFromCache = { className , fileLastModified , file  ->
        try {      
          def url = classLoader.getResource ( "${className}.class" )
          if ( url ) {
            if ( fileLastModified > url.openConnection ( ).lastModified ) {
              compileScript ( cacheDirectory , file.text , className )
            }
          }
          def script = classLoader.loadClass ( className ).newInstance ( )
          script.binding = binding
          script.run ( )
        }
        catch ( Exception e) {
          def fileText = file.text
          compileScript ( cacheDirectory , fileText , className )
          groovyShell.evaluate ( fileText , className )			
        }
      }
      binding.loadClassFromCache =  loadClassFromCache
      loadClassFromCache ( buildClassName , buildFileModified , buildFile )
    }
    else {
      if ( buildFile )  { buildFileText = buildFile.text }
      try { groovyShell.evaluate ( buildFileText , buildClassName ) }
      catch ( Exception e ) {
        for ( stackEntry in e.stackTrace ) {
          if ( stackEntry.fileName == buildClassName ) {
            def sourceName = ( buildClassName == standardInputClassName ) ? 'Standard input' : buildFile.name
            print ( sourceName + ', line ' + stackEntry.lineNumber + ' -- ' )
          }
        }
        println ( 'Error evaluating Gantfile: ' + ( e instanceof InvocationTargetException ? e.cause.message : e.message  ) )
        return 2
      }
    }
    invokeMethod ( function , targets )
  }
  /**
   *  Compile a script in the context of dealing with cached compiled build scripts.
   */
  private void compileScript ( destDir , buildFileText , buildClassName ) {
    if ( ! destDir.exists ( ) ) { destDir.mkdirs ( ) }
    def configuration = new CompilerConfiguration ( )
    configuration.setTargetDirectory ( destDir )
    def unit = new CompilationUnit ( configuration , null , new GroovyClassLoader ( classLoader ) )
    unit.addSource ( buildClassName , new ByteArrayInputStream ( buildFileText.bytes ) )
    unit.compile ( )				
  }
  /**
   *  The entry point for command line invocation.
   */
  public static void main ( String[] args ) { System.exit ( ( new Gant ( ) ).processArgs ( args ) ) }
}
