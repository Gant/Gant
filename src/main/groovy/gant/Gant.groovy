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

import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.CompilationUnit

import org.codehaus.gant.GantBinding
import org.codehaus.gant.GantState

import org.apache.commons.cli.GnuParser

import org.codehaus.groovy.runtime.InvokerInvocationException

/**
 *  This class provides infrastructure and an executable command for using Groovy + AntBuilder as a build
 *  tool in a way similar to Rake and SCons.  However, where Rake and SCons are dependency programming
 *  systems based on Ruby and Python respectively, Gant is simply a way of scripting Ant tasks; the Ant
 *  tasks do all the dependency management.
 *
 *  <p>A Gant build specification file (default name build.gant) is assumed to contain one or more targets.
 *  Dependencies between targets are handled as function calls within functions, or by use of the depends
 *  function. Execution of Ant tasks is by calling methods on the object referred to by `ant', which is
 *  predefined as a <code>GantBuilder</code> instance.</p>
 *
 *  <p>On execution of the gant command, the Gant build specification is read and executed in the context of
 *  a predefined binding.  An object referred to by `ant' is part of the binding so methods can use this
 *  object to get access to the Ant tasks without having to create an object explicitly.  A method called
 *  `target' is part of the predefined binding.  A target has two parameters, a single item map and a
 *  closure.  The single item map has the target name as the key and the documentation about the target as
 *  the value.  This documentation is used by the `gant -T' / `gant --targets' / `gant -p' command to
 *  present a list of all the documented targets.</p>
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
  /**
   *  The class name to use for a script provided as standard input.
   */
  private final standardInputClassName = 'standard_input'

  private final loadClassFromCache = { className , lastModified , url  ->
    try {
      def classUrl = binding.classLoader.getResource ( "${className}.class" )
      if ( classUrl ) {
        if ( lastModified > classUrl.openConnection ( ).lastModified ) {
          compileScript ( cacheDirectory , url.text , className )
        }
      }
      return binding.classLoader.loadClass ( className ).newInstance ( )
    }
    catch ( Exception e) {
      def fileText = url.text
      compileScript ( cacheDirectory , fileText , className )
      return binding.groovyShell.parse ( fileText , buildClassName )
    }
  }
  /**
   *  The name of the file used as input, - means standard input.
   */
//  private String buildFileName = 'build.gant'
  /**
   *  The <code>File</code> object for the script.
   */
//  private File buildFile
  /**
   *  The name of the class actually used for compiling the script.
   */
  String buildClassName

  boolean dryRun = false
  int verbosity = GantState.NORMAL
  boolean useCache = false
  File cacheDirectory = new File ( "${System.properties.'user.home'}/.gant/cache" )
  List gantLib = []
  def script
  /**
   *  The <code>Script</code> object used for the script if a <code>Script</code> object gets used.
   */
//  private Script buildScript
  /**
   *  The binding object used for this run of Gant.  This binding object replaces the standard one to ensure
   *  that all the Gant specific things appear in the binding the script executes with.
   */
  private final GantBinding binding
   /**
    *  Constructor that uses build.gant as the build script, creates a new instance of
    *  <code>GantBinding</code> for the script binding, and the default class loader.
    */
  public Gant ( ) { this ( (GantBinding) null ) }
   /**
    *  Constructor that uses build.gant as the build script, the passed <code>GantBinding</code> for the
    *  script binding, and the default class loader.
    *
    *  @param b the <code>GantBinding</code> to use.
    */
  public Gant ( GantBinding b ) { this ( b , null ) }
   /**
    *  Constructor that uses build.gant as the build script, the passed <code>GantBinding</code> for the
    *  script binding, and the passed <code>ClassLoader</code> as the class loader.
    *
    *  @param b the <code>GantBinding</code> to use.
    *  @param cl the <code>ClassLoader</code> to use.
    */
  public Gant ( GantBinding b , ClassLoader cl ) {
    binding = b ?: new GantBinding ( )
    binding.classLoader = cl ?: getClass ( ).classLoader
    binding.groovyShell = new GroovyShell ( (ClassLoader) binding.classLoader , binding )
  }
  /**
   *  Constructor intended for use in code to be called from the Groovy Ant Task.
   *
   *  @param s the <code>String</code> comprising the Gant script to use.
   *  @param p the <code>org.apache.tools.ant.Project</code> to use.
   */
  public Gant ( org.apache.tools.ant.Project p ) { this ( new GantBinding ( p ) ) }

  public Gant loadScript ( InputStream scriptSource ) {
    //  TODO:  Sort out whether this attempt to change the metaclass is ever going to work.
    //
    //binding.groovyShell.evaluate ( buildFileText , buildClassName )
//    script = binding.groovyShell.parse ( scriptSource , buildClassName )
    // Scripts have ExpandoMetaClass as their metaclass.
    //System.err.println ( 'Gant: ' + script.class.metaClass )
    //script.metaClass = new GantMetaClass ( script.metaClass , binding )
    return loadScript( scriptSource.text )
  }

  public Gant loadScript ( File scriptFile ) {
    return loadScript ( scriptFile.toURI().toURL() )
  }

  public Gant loadScript ( URL scriptUrl ) {
    if ( ! buildClassName ) {
      def filename = scriptUrl.path.substring(scriptUrl.path.lastIndexOf("/") + 1)
      buildClassName = classNameFromFileName ( filename )
    }

    if ( useCache ) {
      if ( binding.classLoader instanceof URLClassLoader ) { binding.classLoader.addURL ( cacheDirectory.toURI ( ).toURL ( ) ) }
      else { binding.classLoader.rootLoader?.addURL ( cacheDirectory.toURI ( ).toURL ( ) ) }

      binding.loadClassFromCache =  loadClassFromCache
      script = loadClassFromCache ( buildClassName , scriptUrl.openConnection ( ).lastModified , scriptUrl )
    }
    else {
      loadScript ( scriptUrl.openStream ( ) )
    }
    return this
  }

  public Gant loadScriptClass ( String className ) {
    script = binding.classLoader.loadClass ( className ).newInstance ( )
    return this
  }
  public Gant loadScript( String text ) {
    if ( ! buildClassName ) { buildClassName = "text_script" }
    //  TODO:  Sort out whether this attempt to change the metaclass is ever going to work.
    //
    //binding.groovyShell.evaluate ( buildFileText , buildClassName )
    script = binding.groovyShell.parse ( text , buildClassName )
    // Scripts have ExpandoMetaClass as their metaclass.
    //System.err.println ( 'Gant: ' + script.class.metaClass )
    //script.metaClass = new GantMetaClass ( script.metaClass , binding )
    return this
  }
  /**
   *  Create a class name from a file name.
   *
   *  <p>File names may have an extension, e.g. .groovy or .gant, which should be removed to create a class
   *  name.  Also some characters that are valid in file names are not valid in class names and so must be
   *  transformed.</p>
   *
   *  <p>Up to Gant 1.5.0 the algorithm was to simply transform '\\.' to '_'.  However this means that
   *  build.groovy got transformed to build_groovy and this caused problems in Eclipse, cf. GANT-30.<p>
   */
  private String classNameFromFileName ( fileName ) {
    def index = fileName.lastIndexOf ( '.' )
    if ( fileName[index..-1] in [ '.groovy' , '.gant' ] ) { fileName = fileName[0..<index] }
    return fileName.replaceAll ( '\\.' , '_' )
  }
  /**
   *  Filter the stacktrace of the exception so as to print the line number of the line in the script being
   *  executed that caused the exception.
   */
  private void printMessageFrom ( exception ) {
    exception.printStackTrace ( System.err )
    for ( stackEntry in exception.stackTrace ) {
      if ( ( stackEntry.fileName == buildClassName ) && ( stackEntry.lineNumber  != -1 ) ) {
        def sourceName = ( buildClassName == standardInputClassName ) ? 'Standard input' : buildClassName
        print ( sourceName + ', line ' + stackEntry.lineNumber + ' -- ' )
      }
    }
    if ( exception instanceof InvocationTargetException ) { exception = exception.cause }
    if ( exception instanceof InvokerInvocationException ) { exception = exception.cause }
    println ( 'Error evaluating Gantfile: ' + ( ( exception instanceof RuntimeException ) ? exception.message : exception.toString ( ) ) )
  }
  /**
   *  The function that implements the creation of the list of targets for the -p and -T options.
   */
  private Integer targetList ( targets ) {
    def max = 0
    binding.targetDescriptions.entrySet ( ).each { item ->
      if ( item.key != 'default' ) {
        def size = item.key.size ( )
        if ( size > max ) { max = size }
      }
    }
    println ( )
    binding.targetDescriptions.entrySet ( ).each { item ->
      if ( item.key != 'default' ) {
        println ( ' ' + item.key + ' ' * ( max - item.key.size ( ) ) + '  ' + item.value )
      }
    }
    println ( )
    def message = binding.targetDescriptions [ 'default' ]
    if ( message != null ) { println ( 'Default target is ' + message + '.' ) ; println ( ) }
    0
  }
  /**
   *  The function that handles actioning the targets.
   */
  private Integer dispatch ( targets ) {
    Integer returnCode = 0
    final processDispatch = { target , errorReturnCode ->
      try {
        def returnValue = owner.binding.getVariable ( target ).call ( )
        returnCode = ( returnValue instanceof Number ) ? returnValue.intValue ( ) : 0
      }
      catch ( MissingPropertyException mme ) {
        if ( target == mme.property ) { println ( "Target ${target} does not exist." ) }
        else { printMessageFrom ( mme ) }
        returnCode = errorReturnCode
      }
    }
    try {
      if ( targets.size ( ) > 0 ) { targets.each { target -> processDispatch ( target , -11 ) } }
      else { processDispatch ( 'default' , -12 ) }
    }
    catch ( Exception e ) {
      println ( e.message )
      e.printStackTrace()
      returnCode = -13
    }
    returnCode
  }
  /**
   *  Process the command line options and then call the function to process the targets.
   */
  public Integer processArgs ( String[] args ) {
    final rootLoader = binding.classLoader.rootLoader
    def buildSource = new File ( "build.gant" )
    //
    //  Commons CLI 1.0 and 1.1 are broken.  1.0 has one set of ideas about multiple args and is broken.
    //  1.1 has a different set of ideas about multiple args and is broken. 1.2 appears to be actually
    //  fixed.  Multiple args are handled in the 1.0 semantics and are not broken :-)
    //
    //  1.0 PosixParser silently absorbs unknown single letter options.
    //
    //  1.0 cannot deal with options having only a long form as the access mechanism that works only works
    //  for short form.  This is fixed in 1.1 and 1.2.
    //
    //  The PosixParser does not handle incorrectly formed options at all well.  Also the standard printout
    //  actually assumes GnuParser form.  So although PosixParser is the default for CliBuilder, we actually
    //  want GnuParser.
    //
    //  We can either specify the parser explicitly or simply say "do not use the PosixParser".  The latter
    //  does of course require knowing that setting posix to false causes the GnuParser to be used.  This
    //  information is only gleanable by reading the source code.  Given that the BasicParser is more or
    //  less totally useless and there are only three parsers available, there is not a big issue here.
    //  However, be explicit for comprehensibility.
    //
    //def cli = new CliBuilder ( usage : 'gant [option]* [target]*' , posix : false )
    def cli = new CliBuilder ( usage : 'gant [option]* [target]*' , parser : new GnuParser ( ) )
    cli.c ( longOpt : 'usecache' , 'Whether to cache the generated class and perform modified checks on the file before re-compilation.' )
    cli.d ( longOpt : 'debug' , 'Print debug levels of information.' )
    cli.f ( longOpt : 'file' , args : 1 , argName : 'build-file' , 'Use the named build file instead of the default, build.gant.' )
    cli.h ( longOpt : 'help' , 'Print out this message.' )
    cli.l ( longOpt : 'gantlib' , args : 1 , argName : 'library' , 'A directory that contains classes to be used as extra Gant modules,' )
    cli.n ( longOpt : 'dry-run' , 'Do not actually action any tasks.' )
    cli.p ( longOpt : 'projecthelp' , 'Print out a list of the possible targets.' ) // Ant uses -p|-projecthelp for this.
    cli.q ( longOpt : 'quiet' , 'Do not print out much when executing.' )
    cli.s ( longOpt : 'silent' , 'Print out nothing when executing.' )
    cli.v ( longOpt : 'verbose' , 'Print lots of extra information.' )
    cli.C ( longOpt : 'cachedir' , args : 1 , argName : 'cache-file' , 'The directory where to cache generated classes to.' )
    cli.D ( argName : 'name>=<value' , args : 1 , 'Define <name> to have value <value>.  Creates a variable named <name> for use in the scripts and a property named <name> for the Ant tasks.' )
    cli.L ( longOpt : 'lib' , args : 1 , argName : 'path' , 'Add a directory to search for jars and classes.' )
    cli.P ( longOpt : 'classpath' , args : 1 , argName : 'path-list' , 'Specify a path list to search for jars and classes.' )
    cli.T ( longOpt : 'targets' , 'Print out a list of the possible targets.' ) // Rake and Rant use -T|--tasks for this.
    cli.V ( longOpt : 'version' , 'Print the version number and exit.' )
    def options = cli.parse ( args )
    if ( options == null ) { println ( 'Error in processing command line options.' ) ; return -1 }
    useCache = options.c ? true : false
    if ( options.f ) {
      if ( options.f == '-' ) { buildSource = System.in ; buildClassName = standardInputClassName }
      else { buildSource = new File ( options.f ) }
    }
    if ( options.h ) { cli.usage ( ) ; return 0 }
    if ( options.l ) { gantLib.addAll ( options.l.split ( System.properties.'path.separator' ) as List ) }
    if ( options.n ) { dryRun = true }
    def function =  ( options.p || options.T ) ? 'targetList' : 'dispatch'
    if ( options.d ) { verbosity = GantState.DEBUG }
    if ( options.q ) { verbosity = GantState.QUIET }
    if ( options.s ) { verbosity = GantState.SILENT }
    if ( options.v ) { verbosity = GantState.VERBOSE }
    if ( useCache && options.C ) { cacheDirectory = new File ( (String) options.C ) }
    if ( options.D ) {
      options.Ds.each { definition ->
        def pair = definition.split ( '=' ) as List
        if ( pair.size ( ) < 2 ) { pair << '' }
        //  Do not allow the output to escape.  The problem here is that if the output is allowed out then
        //  Ant, Gant, Maven, Eclipse and IntelliJ IDEA all behave slightly differently.  This makes testing
        //  nigh on impossible.  Also the user doesn't need to know about these.
        final outSave = System.out
        System.out = new PrintStream ( new ByteArrayOutputStream ( ) )
        binding.ant.property ( name : pair[0] , value : pair[1] )
        System.out = outSave
      }
    }
    if ( options.L ) {
      options.Ls.each { String directoryName ->
        def directory = new File ( directoryName )
        if ( directory.isDirectory ( ) ) { directory.eachFile { item -> rootLoader?.addURL ( item.toURL ( ) ) } }
        else { println ( 'Parameter to -L|--lib option is not a directory: ' + directory.name ) }
      }
    }
    if ( options.P ) { options.P.split ( System.properties.'path.separator' ).each { String pathitem -> rootLoader?.addURL ( ( new File ( pathitem ) ).toURL ( ) ) } }
    if ( options.V ) {
      def version = ''
      final gantPackage = binding.classLoader.getPackage ( 'gant' )
      if ( gantPackage != null ) { version = gantPackage.implementationVersion }
      println ( 'Gant version ' + ( ( ( version == null ) || ( version == '' ) ) ? '<unknown>' : version ) )
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
    if ( gotUnknownOptions ) { cli.usage ( ) ; return -1 ; }

    try { loadScript ( buildSource ) }
    catch ( FileNotFoundException fnfe ) { println ( 'Cannot open file ' + buildSource.name ) ; return -3 }
    catch ( Exception e ) { printMessageFrom ( e ) ; return -2 }
    
    try { processTargets ( function , targets ) }
    catch ( Exception e ) { printMessageFrom ( e ) ; return -2 }
  }
  public Integer processTargets ( ) { processTargets ( 'dispatch' , [ ] ) }
  public Integer processTargets ( String s ) { processTargets ( 'dispatch' , [ s ] ) }
  public Integer processTargets ( List l ) { processTargets ( 'dispatch' , l ) }
  /**
   *  Process the targets, but first deal with getting the build script loaded, either by compiling the text
   *  of the file or standard input, or using the cached compiled file.
   */
  protected Integer processTargets ( String function , List targets ) {
    // Configure the build based on this instance's settings.
    if ( dryRun ) { GantState.dryRun = true }
    if ( verbosity != GantState.NORMAL ) { GantState.verbosity = verbosity ; binding.ant.setMessageOutputLevel ( ) }
    binding.cacheEnabled = useCache
    binding.gantLib = gantLib

    if ( script == null ) { throw new RuntimeException ( "No script has been loaded!" ) }
    script.binding = binding
    script.run ( )
    
    invokeMethod ( function , targets )
  }
  /**
   *  Compile a script in the context of dealing with cached compiled build scripts.
   */
  private void compileScript ( destDir , buildFileText , buildClassName ) {
    if ( ! destDir.exists ( ) ) { destDir.mkdirs ( ) }
    def configuration = new CompilerConfiguration ( )
    configuration.targetDirectory = destDir
    def unit = new CompilationUnit ( configuration , null , new GroovyClassLoader ( (ClassLoader) binding.classLoader ) )
    unit.addSource ( buildClassName , new ByteArrayInputStream ( (byte[]) buildFileText.bytes ) )
    unit.compile ( )
  }
  /**
   *  The entry point for command line invocation.
   */
  public static void main ( String[] args ) { System.exit ( ( ( new Gant ( ) ).processArgs ( args ) ) ) }  // IntelliJ IDEA thinks processTargets returns an Object.
}
