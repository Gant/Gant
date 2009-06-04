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

package gant

import java.lang.reflect.InvocationTargetException

import org.apache.commons.cli.GnuParser

import org.apache.tools.ant.BuildListener
import org.apache.tools.ant.Project

import org.codehaus.gant.GantBinding
import org.codehaus.gant.GantEvent
import org.codehaus.gant.GantState

import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.CompilerConfiguration
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
 *  @author Peter Ledbrook
 */
final class Gant {
  /**
   *  The class name to use for a script provided as standard input.
   */
  private final standardInputClassName = 'standard_input'
  /**
   *  The class name to use for a script provided as an input stream.
   */
  private final streamInputClassName = 'stream_input'
  /**
   *  The class name to use for a script provided as plain text.
   */
  private final textInputClassName = 'text_input'

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
    catch ( Exception e ) {
      def fileText = url.text
      compileScript ( cacheDirectory , fileText , className )
      return binding.groovyShell.parse ( fileText , buildClassName )
    }
  }
  /**
   *  The name of the class actually used for compiling the script.
   */
  String buildClassName
  /**
   *  Determines whether Gant performs a dry-run or does it for real.
   */
  boolean dryRun = false
  /**
   *  The verbosity of Gant's output. Defaults to { @link GantState#NORMAL }.
   */
  int verbosity = GantState.NORMAL
  /**
   *  Determines whether the scripts are cached or not. Defaults to <code>false</code>.
   */
  boolean useCache = false
  /**
   *  The location where the compiled scripts are cached. Defaults to "$USER_HOME/.gant/cache".
   */
  File cacheDirectory = new File ( "${System.properties.'user.home'}/.gant/cache" )
  /**
   *  A list of strings containing the locations of Gant modules.
   */
  List gantLib = [ ]
  /**
   *  The script that will be run when { @link #processTargets ( ) } is called. It is initialised when a
   *  script is loaded. Note that it has a dynamic type because the script may be loaded from a different
   *  class loader than the one used to load the Gant class. If we declared it as Script, there would likely
   *  be ClassCastExceptions.
   */
  def script
  /**
   *  A bit of state to say whether to output a message about the build result.
   */
  private static boolean outputBuildTime = false
  /**
   *  The binding object used for this run of Gant.  This binding object replaces the standard one to ensure
   *  that all the Gant specific things appear in the binding the script executes with.
   */
  private final GantBinding binding
  /**
   *  Default constructor -- creates a new instance of <code>GantBinding</code> for the script binding,
   *  and the default class loader.
   */
  public Gant ( ) { this ( (GantBinding) null ) }
  /**
   *  Constructor that uses the passed <code>GantBinding</code> for the script binding, and the default
   *  class loader.
   *
   *  @param b the <code>GantBinding</code> to use.
   */
  public Gant ( GantBinding b ) { this ( b , null ) }
  /**
   *  Constructor that uses the passed <code>GantBinding</code> for the script binding, and the passed
   *  <code>ClassLoader</code> as the class loader.
   *
   *  @param b the <code>GantBinding</code> to use.
   *  @param cl the <code>ClassLoader</code> to use.
   */
  public Gant ( GantBinding b , ClassLoader cl ) {
    binding = b ?: new GantBinding ( )
    binding.classLoader = cl ?: getClass ( ).classLoader
    binding.groovyShell = new GroovyShell ( (ClassLoader) binding.classLoader , binding )
    final gantPackage = binding.classLoader.getPackage ( 'gant' )
    binding.'gant.version' = gantPackage?.implementationVersion
  }
  /**
   *  Constructor intended for use in code to be called from the Groovy Ant Task.
   *
   *  @param p the <code>org.apache.tools.ant.Project</code> to use.
   */
  public Gant ( org.apache.tools.ant.Project p ) { this ( new GantBinding ( p ) ) }
  /**
   *  Add a <code>BuildListener</code> instance to this <code>Gant</code> instance.
   */
  public void addBuildListener ( final BuildListener buildListener ) {
    binding.addBuildListener ( buildListener )
  }
  /**
   *  Remove a <code>BuildListener</code> instance from this <code>Gant</code> instance
   */
  public void removeBuildListener ( final BuildListener buildListener ) {
    binding.removeBuildListener ( buildListener )
  }
  /**
   *  Treats the given text as a Gant script and loads it.
   *
   *  @params text The text of the Gant script to load.
   */
  public Gant loadScript ( String text ) {
    if ( ! buildClassName ) { buildClassName = textInputClassName }
    script = binding.groovyShell.parse ( text , buildClassName )
    binding.'gant.file' = '<text>'
    return this
  }
  /**
   *  Loads a Gant script from the given input stream, using the default Groovy encoding to convert the
   *  bytes to characters.
   *
   *  @params scriptSource The stream containing the Gant script source, i.e. the Groovy code, not the
   *  compiled class.
   */
  public Gant loadScript ( InputStream scriptSource ) {
    if ( ! buildClassName ) { buildClassName = streamInputClassName }
    script = binding.groovyShell.parse ( scriptSource , buildClassName )
    binding.'gant.file' = '<stream>'
    return this
  }
  /**
   *  Loads a Gant script from the given file, using the default Groovy encoding to convert the bytes
   *  to characters.
   *
   *  @params scriptFile The file containing the Gant script source, i.e. the Groovy code, not the
   *  compiled class.
   */
  public Gant loadScript ( File scriptFile ) {
    return loadScript ( scriptFile.toURI ( ).toURL ( ) )
  }
  /**
   *  Loads a Gant script from the given URL, using the default Groovy encoding to convert the bytes
   *  to characters.
   *
   *  @params scriptUrl The URL where the the Gant script source is located.
   */
  public Gant loadScript ( URL scriptUrl ) {
    if ( ! buildClassName ) {
      def filename = scriptUrl.path.substring ( scriptUrl.path.lastIndexOf ( "/" ) + 1 )
      buildClassName = classNameFromFileName ( filename )
    }
    if ( useCache ) {
      if ( binding.classLoader instanceof URLClassLoader ) { binding.classLoader.addURL ( cacheDirectory.toURI ( ).toURL ( ) ) }
      else { binding.classLoader.rootLoader?.addURL ( cacheDirectory.toURI ( ).toURL ( ) ) }
      binding.loadClassFromCache =  loadClassFromCache
      script = loadClassFromCache ( buildClassName , scriptUrl.openConnection ( ).lastModified , scriptUrl )
    }
    else { loadScript ( scriptUrl.openStream ( ) ) }
    binding.'gant.file' = scriptUrl.toString ( )
    return this
  }
  /**
   *  Loads a pre-compiled Gant script using the configured class loader.
   *
   *  @params className The fully qualified name of the class to load.
   */
  public Gant loadScriptClass ( String className ) {
    script = binding.classLoader.loadClass ( className ).newInstance ( )
    binding.'gant.file' = '<class>'
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
   *  build.groovy got transformed to build_groovy and this caused problems in Eclipse, cf. GANT-30.</p>
   */
  private String classNameFromFileName ( fileName ) {
    def index = fileName.lastIndexOf ( '.' )
    if ( fileName[index..-1] in [ '.groovy' , '.gant' ] ) { fileName = fileName[0..<index] }
    return fileName.replaceAll ( '\\.' , '_' )
  }
  /**
   *  Filter the stacktrace of the exception so as to create a printable message with the line number of the
   *  line in the script being executed that caused the exception.
   */
  private String constructMessageFrom ( exception ) {
    final buffer = new StringBuilder ( )
    if ( exception instanceof GantException ) { exception = exception.cause }
    for ( stackEntry in exception.stackTrace ) {
      if ( ( stackEntry.fileName == buildClassName ) && ( stackEntry.lineNumber  != -1 ) ) {
        def sourceName = ( buildClassName == standardInputClassName ) ? 'Standard input' : buildClassName
        buffer.append ( sourceName + ', line ' + stackEntry.lineNumber + ' -- ' )
      }
    }
    if ( exception instanceof InvocationTargetException ) { exception = exception.cause }
    if ( exception instanceof InvokerInvocationException ) { exception = exception.cause }
    buffer.append ( 'Error evaluating Gantfile: ' + ( ( exception instanceof RuntimeException ) ? exception.message : exception.toString ( ) ) )
    buffer.toString ( )
  }
  /**
   *  The function that implements the printing of the list of targets for the -p and -T options.
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
  private Integer dispatch ( List targets ) {
    Integer returnCode = 0
    final processDispatch = { target ->
      try {
        owner.binding.forcedSettingOfVariable ( 'initiatingTarget' , target )
        def returnValue = owner.binding.getVariable ( target ).call ( )
        returnCode = ( returnValue instanceof Number ) ? returnValue.intValue ( ) : 0
      }
      catch ( MissingPropertyException mme ) {
        if ( target == mme.property ) { throw new MissingTargetException ( "Target ${target} does not exist." , mme ) }
        else throw new TargetMissingPropertyException ( mme.message , mme )
      }
      catch ( Exception e ) { throw new TargetExecutionException ( e.toString ( ) , e ) }
    }
    //  To support GANT-44 the script must have access to the targets and be able to edit it, this means
    //  iterating over the list of targets but knowing that if might change during execution.  So replace
    //  the original code:
    //
    //    if ( targets.size ( ) > 0 ) { withBuildListeners { targets.each { target -> processDispatch ( target ) } } }
    //
    //  with something a little more amenable to alteration of the list mid loop.
    binding.forcedSettingOfVariable ( 'targets' , targets )
    if ( targets.size ( ) > 0 ) {
      withBuildListeners {
        while ( targets.size ( ) > 0 ) {
          processDispatch ( targets[0] )
          targets.remove ( 0 )
        }
      }
    }
    else { withBuildListeners { processDispatch ( 'default' ) } }
    returnCode
  }
  /**
   *  Execute a dispatch with all the <code>BuildListener</code>s informed.
   */
  private withBuildListeners ( Closure callable ) {
      def event = new GantEvent ( (Project) binding.ant.antProject , (GantBinding) binding )
      try {
        binding.buildListeners.each { BuildListener listener -> listener.buildStarted ( event ) }
        callable.call ( )
        binding.buildListeners.each { BuildListener listener -> listener.buildFinished ( event ) }
      }
      catch ( Exception e ) {
        event.exception = e
        binding.buildListeners.each { BuildListener listener -> listener.buildFinished ( event ) }
        throw e
      }
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
      else { buildSource = new File ( (String) options.f ) }
    }
    if ( options.h ) { cli.usage ( ) ; return 0 }
    if ( options.l ) { gantLib.addAll ( options.l.split ( System.properties.'path.separator' ) as List ) }
    if ( options.n ) { dryRun = true }
    def function =  ( options.p || options.T ) ? 'targetList' : 'dispatch'
    if ( options.d ) { verbosity = GantState.DEBUG }
    if ( options.q ) { verbosity = GantState.ERRORS_ONLY }
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
    if ( options.V ) { println ( 'Gant version ' + ( binding.'gant.version' ?: '<unknown>' ) ) ; return 0 }
    //  The rest of the arguments appear to be delivered as a single string as the first item in a list.  This is surely an error but
    //  with Commons CLI 1.0 it is the case.  So we must partition.  NB the split method delivers an array
    //  of Strings so we cast to a List.
    def targets = options.arguments ( )
    if ( ( targets != null ) && ( targets.size ( ) == 1 ) ) { targets = targets[0].split ( ' ' ) as List }
    def gotUnknownOptions = false ;
    targets.each { target ->
      if ( target[0] == '-' ) {
        println ( 'Unknown option: ' + target )
        gotUnknownOptions = true
      }
    }
    if ( gotUnknownOptions ) { cli.usage ( ) ; return -1 ; }
    //
    //  Nota Bene: the Ant logger puts error level messages out on stderr and all other levels of message
    //  out on stdout.  Currently the tests expect all information out on stdout.  Therefore put error
    //  message out as warnings.
    //
    try { loadScript ( buildSource ) }
    catch ( FileNotFoundException fnfe ) { binding.ant.project.log ( 'Cannot open file ' + buildSource.name , Project.MSG_WARN ) ; return -3 }
    catch ( Exception e ) { binding.ant.project.log ( constructMessageFrom ( e ) , Project.MSG_WARN ) ; return -2 }
    def defaultReturnCode = targets?.size ( ) > 0 ? -11 : -12
    outputBuildTime = function == 'dispatch'
    try { return processTargets ( function , targets ) }
    catch ( TargetExecutionException tee ) {
      if ( verbosity > GantState.NORMAL ) { tee.printStackTrace ( ) }
      else { binding.ant.project.log ( tee.message , Project.MSG_WARN ) }
      return -13
    }
    catch ( MissingTargetException mte ) {
      if ( verbosity > GantState.NORMAL ) { mte.printStackTrace ( ) }
      else { binding.ant.project.log ( mte.message , Project.MSG_WARN ) }
      return defaultReturnCode
    }
    catch ( TargetMissingPropertyException tmpe ) {
      if ( verbosity > GantState.NORMAL ) { tmpe.printStackTrace ( ) }
      else { binding.ant.project.log ( constructMessageFrom ( tmpe ) , Project.MSG_WARN ) }
      return defaultReturnCode
    }
    catch ( Exception e ) {
      if ( verbosity > GantState.NORMAL ) { e.printStackTrace ( ) }
      else { binding.ant.project.log ( constructMessageFrom ( e ) , Project.MSG_WARN ) }
      return -4
    }
    //  Cannot get here.
    assert 1 == 0
  }
  public Integer processTargets ( ) { processTargets ( 'dispatch' , [ ] ) }
  public Integer processTargets ( String s ) { processTargets ( 'dispatch' , [ s ] ) }
  public Integer processTargets ( List l ) { processTargets ( 'dispatch' , l ) }
  /**
   *  Process the targets, but first execute the build script so all the targets and other code are available.
   */
  protected Integer processTargets ( String function , List targets ) {
    // Configure the build based on this instance's settings.
    if ( dryRun ) { GantState.dryRun = true }
    if ( verbosity != GantState.verbosity ) { GantState.verbosity = verbosity ; binding.ant.setMessageOutputLevel ( ) }
    binding.cacheEnabled = useCache
    binding.gantLib = gantLib
    if ( script == null ) { throw new RuntimeException ( "No script has been loaded!" ) }
    script.binding = binding
    script.run ( )
    return (Integer) invokeMethod ( function , targets )
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
  public static void main ( String[] args ) {
    def startTime = System.nanoTime ( )
    def gant = new Gant ( )
    def returnValue = gant.processArgs ( args )
    if ( outputBuildTime ) {
      def elapseTime = ( System.nanoTime ( ) - startTime ) / 1e9
      def project = gant.binding.ant.project
      project.log ( '\nBUILD ' + ( returnValue == 0 ? 'SUCCESSFUL' : 'FAILED' ) , GantState.WARNINGS_ERRORS )
      project.log ( 'Total time: ' + String.format ( '%.2f' , elapseTime ) + ' seconds' , GantState.WARNINGS_ERRORS )
    }
    System.exit ( returnValue )
  }
}
