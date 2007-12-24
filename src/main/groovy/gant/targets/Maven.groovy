//  Gant -- A Groovy build framework based on scripting Ant tasks.
//
//  Copyright © 2007 Russel Winder
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

package gant.targets

/**
 *  A class to provide the lifecycle targets associated with a Maven 2 project.
 *
 *  @author Russel Winder <russel.winder@concertant.com>
 */
final class Maven {
  private final readOnlyKeys = [ 'binding' , 'compileDependenciesClasspathId' , 'testDependenciesClasspathId', 'antlibXMLns' , 'mavenPOMId' ]
  private final Map properties = [
                                  groupId : '' ,
                                  artifactId : '' ,
                                  version : '' ,
                                  sourcePath : 'src' ,
                                  mainSourcePath : '' , // Set in constructor since it uses a GString dependent on a value in the map.
                                  testSourcePath : '' , // Set in constructor since it uses a GString dependent on a value in the map.
                                  targetPath : 'target' ,
                                  mainCompilePath : '' , // Set in constructor since it uses a GString dependent on a value in the map.
                                  testCompilePath : '' , // Set in constructor since it uses a GString dependent on a value in the map.
                                  testReportPath : '' , // Set in constructor since it uses a GString dependent on a value in the map.
                                  metadataPath : '' , // Set in constructor since it uses a GString dependent on a value in the map.
                                  javaCompileProperties : [ source : '1.3' , target : '1.3' , debug : 'false' ] ,
                                  groovyCompileProperties : [ : ] ,
                                  compileClasspath : [ ] ,
                                  testClasspath : [ ] ,
                                  compileDependencies : [ ] ,
                                  testDependencies : [ ] ,
                                  testFramework : 'junit' ,
                                  testFailIgnore : false ,
                                  packaging : 'jar' ,
                                  deployURL : '' ,
                                  deploySnapshotURL : '' ,
                                  manifest : [ : ] ,
                                  manifestIncludes :  [ ] ,
                                  ( readOnlyKeys[0] ) : null ,
                                  ( readOnlyKeys[1] ) :  'compile.dependency.classpath' ,
                                  ( readOnlyKeys[2] ) :  'test.dependency.classpath' ,
                                  ( readOnlyKeys[3] ) : 'antlib:org.apache.maven.artifact.ant' ,
                                  ( readOnlyKeys[4] ) : 'maven.pom'
                                  ]
   Maven ( Binding binding ) {
     properties.binding = binding
     constructMavenObject ( )
   }
   Maven ( Binding binding , Map map ) {
     properties.binding = binding
     map.each { key , value -> owner.setProperty ( key , value ) }
     constructMavenObject ( )
   }
  def constructMavenObject ( ) {
    properties.mainSourcePath = "${properties.sourcePath}${System.properties.'file.separator'}main"
    properties.testSourcePath = "${properties.sourcePath}${System.properties.'file.separator'}test"
    properties.mainCompilePath = "${properties.targetPath}${System.properties.'file.separator'}classes"
    properties.testCompilePath = "${properties.targetPath}${System.properties.'file.separator'}test-classes"
    properties.testReportPath = "${properties.targetPath}${System.properties.'file.separator'}test-reports"
    properties.metadataPath = "${properties.mainComilePath}${System.properties.'file.separator'}META-INF"
    properties.binding.target.call ( initialize : 'Ensure all the dependencies can be met and set classpaths accordingly.' ) {
      if ( owner.testFramework == 'testng' ) {
        testngInstalled = false
        owner.testDependencies.each { dependency -> if ( dependency.artifactId == 'testng' ) { testngInstalled = true } }
        if ( ! testngInstalled ) { owner.testDependencies << [ groupId : 'org.testng' , artifactId : 'testng' , version : '5.7' , scope : 'test' , classifier : 'jdk15' ] }
      }
      if ( owner.compileDependencies ) {
        owner.binding.Ant."${owner.antlibXMLns}:dependencies" ( pathId : owner.compileDependenciesClasspathId ) {
          owner.compileDependencies.each { item ->
                                           dependency ( groupId : item.groupId , artifactId : item.artifactId , version : item.version , classifier : item.classifier )
          }
        }
      }
      if ( owner.testDependencies ) {
        owner.binding.Ant."${owner.antlibXMLns}:dependencies" ( pathId : owner.testDependenciesClasspathId ) {
          owner.testDependencies.each { item ->
                                        dependency ( groupId : item.groupId , artifactId : item.artifactId , version : item.version , classifier : item.classifier )
          }
        }
      }
    }
    /*
    properties.binding.target.call ( validate : 'Validate the project is correct and all necessary information is available.' ) {
      throw new RuntimeException ( 'Validate not implemented as yet.' )
    }
    properties.binding.target.call ( 'generate-sources' : 'Generate any source code for inclusion in compilation.' ) {
      throw new RuntimeException ( 'Generate-sources not implemented as yet.' )
    }
    properties.binding.target.call ( 'process-sources' : 'Process the source code, for example to filter any values.' ) {
      throw new RuntimeException ( 'Process-sources not implemented as yet.' )
    }
    properties.binding.target.call ( 'generate-resources' : 'Generate resources for inclusion in the package.' ) {
      throw new RuntimeException ( 'Generate-resources not implemented as yet.' )
    }
    properties.binding.target.call ( 'process-resources' : 'Copy and process the resources into the destination directory, ready for packaging.' ) {
      throw new RuntimeException ( 'Process-resources not implemented as yet.' )
    }
    */
    properties.binding.target.call ( compile : "Compile the source code in ${properties.mainSourcePath} to ${properties.mainCompilePath}." ) {
      depends ( owner.binding.initialize )
      owner.binding.Ant.mkdir ( dir : owner.mainCompilePath )
      new File ( owner.mainSourcePath ).eachDir { directory ->
        switch ( directory.name ) {
         case 'java' :
          //  Need to use the joint Groovy compiler here to deal wuth the case where Groovy files are in the
          //  Java hierarchy.
         owner.binding.Ant.javac ( [ srcdir : owner.mainSourcePath + System.properties.'file.separator' + 'java' , destdir : owner.mainCompilePath ] + owner.javaCompileProperties ) {
           classpath {
             pathelement ( path : owner.compileClasspath.join ( System.properties.'path.separator' ) )
             if ( owner.compileDependencies ) { path ( refid : owner.compileDependenciesClasspathId ) }
             path { fileset ( dir : System.properties.'groovy.home' + System.properties.'file.separator' + 'lib' , includes : 'junit*.jar' ) }
           }
         }
         break
         case 'groovy' :
         owner.binding.Ant.taskdef ( name : 'groovyc' , classname : 'org.codehaus.groovy.ant.Groovyc' )
         owner.binding.Ant.groovyc ( [ srcdir : owner.mainSourcePath + System.properties.'file.separator' + 'groovy' , destdir : owner.mainCompilePath ] + owner.groovyCompileProperties ) {
           javac ( owner.javaCompileProperties ) {
             classpath {
               pathelement ( path : owner.compileClasspath.join ( System.properties.'path.separator' ) )
               if ( owner.compileDependencies ) { path ( refid : owner.compileDependenciesClasspathId ) }
               path { fileset ( dir : System.properties.'groovy.home' + System.properties.'file.separator' + 'lib' , includes : '*.jar' ) }
             }
           }
           classpath {
             pathelement ( path : owner.compileClasspath.join ( System.properties.'path.separator' ) )
             if ( owner.compileDependencies ) { path ( refid : owner.compileDependenciesClasspathId ) }
             path { fileset ( dir : System.properties.'groovy.home' + System.properties.'file.separator' + 'lib' , includes : 'junit*.jar' ) }
           }
         }
         break
        }
      }
    }
    /*
    properties.binding.target.call ( 'process-classes' , 'Post-process the generated files from compilation, for example to do bytecode enhancement on Java classes.' ) {
      throw new RuntimeException ( 'Process-classes not implemented as yet.' )
    }
    properties.binding.target.call ( 'generate-test-sources' , 'Generate any test source code for inclusion in compilation.' ) {
      throw new RuntimeException ( 'Generate-test-sources not implemented as yet.' )
    }
    properties.binding.target.call ( 'process-test-sources' , 'Process the test source code, for example to filter any values.' ) {
      throw new RuntimeException ( 'Process-test-sources not implemented as yet.' )
    }
    properties.binding.target.call ( 'generate-test-resources' , 'Create resources for testing.' ) {
      throw new RuntimeException ( 'Generate-test-sources not implemented as yet.' )
    }
    properties.binding.target.call ( 'process-test-resources' , 'Copy and process the resources into the test destination directory.' ) {
      throw new RuntimeException ( 'Process-test-sources not implemented as yet.' )
    }
    */
    properties.binding.target.call ( 'test-compile' : "Compile the test source code in ${properties.testSourcePath} to ${properties.testCompilePath}." ) {
      depends ( owner.binding.compile )
      owner.binding.Ant.mkdir ( dir : owner.testCompilePath  )
      new File ( owner.testSourcePath ).eachDir { directory ->
        switch ( directory.name ) {
         case 'java' :
          //  Need to use the joint Groovy compiler here to deal wuth the case where Groovy files are in the
          //  Java hierarchy.
         owner.binding.Ant.javac ( [ srcdir : owner.testSourcePath + System.properties.'file.separator' + 'java' , destdir : owner.testCompilePath ] + owner.javaCompileProperties ) {
           classpath {
             pathelement ( location : owner.mainCompilePath )
             pathelement ( path : owner.compileClasspath.join ( System.properties.'path.separator' ) )
             pathelement ( path : owner.testClasspath.join ( System.properties.'path.separator' ) )
             if ( owner.compileDependencies ) { path ( refid : owner.compileDependenciesClasspathId ) }
             if ( owner.testDependencies ) { path ( refid : owner.testDependenciesClasspathId ) }
             path { fileset ( dir : System.properties.'groovy.home' + System.properties.'file.separator' + 'lib' , includes : 'junit*.jar' ) }
           }
         }
         break
         case 'groovy' :
         owner.binding.Ant.taskdef ( name : 'groovyc' , classname : 'org.codehaus.groovy.ant.Groovyc' )
         owner.binding.Ant.groovyc ( [ srcdir : owner.testSourcePath + System.properties.'file.separator' + 'groovy' , destdir : owner.testCompilePath ] + owner.groovyCompileProperties ) {
           javac ( owner.javaCompileProperties ) {
             classpath {
               pathelement ( location : owner.mainCompilePath )
               pathelement ( path : owner.compileClasspath.join ( System.properties.'path.separator' ) )
               pathelement ( path : owner.testClasspath.join ( System.properties.'path.separator' ) )
               if ( owner.compileDependencies ) { path ( refid : owner.compileDependenciesClasspathId ) }
               if ( owner.testDependencies ) { path ( refid : owner.testDependenciesClasspathId ) }
               path { fileset ( dir : System.properties.'groovy.home' + System.properties.'file.separator' + 'lib' , includes : '*.jar' ) }
             }
           }
           classpath {
             pathelement ( location : owner.mainCompilePath )
             pathelement ( path : owner.compileClasspath.join ( System.properties.'path.separator' ) )
             pathelement ( path : owner.testClasspath.join ( System.properties.'path.separator' ) )
             if ( owner.compileDependencies ) { path ( refid : owner.compileDependenciesClasspathId ) }
             if ( owner.testDependencies ) { path ( refid : owner.testDependenciesClasspathId ) }
             path { fileset ( dir : System.properties.'groovy.home' + System.properties.'file.separator' + 'lib' , includes : 'junit*.jar' ) }
           }
         }
         break
        }
      }
    }
    properties.binding.target.call ( test : "Run the tests using the ${properties.testFramework} unit testing framework." ) {
      depends ( owner.binding.'test-compile' )
      switch ( owner.testFramework ) {
       case 'testng' :
       owner.binding.Ant.taskdef ( resource : 'testngtasks' ) { classpath { path ( refid : owner.testDependenciesClasspathId ) } }
       owner.binding.Ant.testng ( outputdir : owner.testReportPath ) {
         classpath {
           fileset ( dir : System.properties.'groovy.home' + System.properties.'file.separator' + 'lib' , includes : '*.jar' )
           pathelement ( location : owner.mainCompilePath )
           pathelement ( location : owner.testCompilePath )
           pathelement ( path : owner.compileClasspath.join ( System.properties.'path.separator' ) )
           pathelement ( path : owner.testClasspath.join ( System.properties.'path.separator' ) )
           if ( owner.compileDependencies ) { path ( refid : owner.compileDependenciesClasspathId ) }
           if ( owner.testDependencies ) { path ( refid : owner.testDependenciesClasspathId ) }
         }
         classfileset ( dir : owner.testCompilePath )
       }
       break
       case 'junit' :
       default :
       owner.binding.Ant.mkdir ( dir : owner.testReportPath )
       owner.binding.Ant.junit ( printsummary : 'yes' , failureproperty : 'testsFailed' ) {
         classpath {
           pathelement ( location : owner.mainCompilePath )
           pathelement ( location : owner.testCompilePath )
           pathelement ( path : owner.compileClasspath.join ( System.properties.'path.separator' ) )
           pathelement ( path : owner.testClasspath.join ( System.properties.'path.separator' ) )
           if ( owner.compileDependencies ) { path ( refid : owner.compileDependenciesClasspathId ) }
           if ( owner.testDependencies ) { path ( refid : owner.testDependenciesClasspathId ) }
           path { fileset ( dir : System.properties.'groovy.home' + System.properties.'file.separator' + 'lib' , includes : '*.jar' ) }
         }
         formatter ( type : 'plain' )
         batchtest ( fork : 'true' , todir : owner.testReportPath ) {
           fileset ( dir : owner.testCompilePath , includes : '**/*Test.class' )
         }
       }
       break
      }
      try {
        // owner.binding.testFailureIgnore or owner.binding.Ant.project.properties.testsFailed or both may not exist.  Ignore the MissingPropertyException.
        if ( ! owner.binding.testFailureIgnore && owner.binding.Ant.project.properties.testsFailed ) { throw new RuntimeException ( 'Tests failed, execution terminating.' ) }
      }
      catch ( MissingPropertyException mpe ) { }
    }
    properties.binding.target.call ( 'package' : "Package the artefact as a ${properties.packaging} in ${properties.mainCompilePath}." ) {
      [ 'groupId' , 'artifactId' , 'version' ].each { item ->
        if ( ! owner."${item}" ) { throw new RuntimeException ( "Maven.${item} must be set to achieve target package." ) }
      }
      depends ( owner.binding.test )
      if ( owner.manifest ) {
        owner.binding.Ant.mkdir ( dir : owner.metadataPath )
        owner.binding.Ant.manifest ( file : owner.metadataPath + System.properties.'file.separator' + 'MANIFEST.MF' ) {
          owner.manifest.each { key , value -> attribute ( name : key , value : value ) }
        }
      }
      if ( owner.manifestIncludes ) {
        owner.binding.Ant.mkdir ( dir : owner.metadataPath )
        owner.manifestIncludes.each { item ->
          if ( new File ( item ).isDirectory ( ) ) { owner.binding.Ant.copy ( todir : owner.metadataPath ) { fileset ( dir : item , includes : '*' ) } }
          else {owner.binding.Ant.copy ( todir : owner.metadataPath , file : item ) }
        }
      }
      switch ( owner.packaging ) {
       case 'war' :
       def artifactPath = owner.targetPath + System.properties.'file.separator' + owner.artifactId + '-' + owner.version
       owner.packagedArtifact = artifactPath + '.war'
       owner.binding.Ant.mkdir ( dir : artifactPath )
       owner.binding.Ant.copy ( todir : artifactPath ) {
         fileset ( dir : classesDir )
         fileset ( dir : [ 'src' , 'main' , 'webapp' ].join ( System.properties.'file.separator' ) )
       }
       owner.binding.Ant.jar ( destfile : owner.packagedArtifact ) { fileset ( dir : artifactPath ) }
       break
       case 'jar' :
       owner.packagedArtifact = owner.targetPath + System.properties.'file.separator' + owner.artifactId + '-' + owner.version + '.jar'
       owner.binding.Ant.jar ( destfile : owner.packagedArtifact ) { fileset ( dir : owner.mainCompilePath ) }
      }
    }
    /*
    properties.binding.target.call ( 'integration-test' : 'Process and deploy the package if necessary into an environment where integration tests can be run.' ) {
      throw new RuntimeException ( 'Integration-test not implemented as yet.' )
    }
    properties.binding.target.call ( verify : 'Run any checks to verify the package is valid and meets quality criteria.' ) {
      throw new RuntimeException ( 'Verify not implemented as yet.' )
    }
    */
    properties.binding.target.call ( install : 'Install the artefact into the local repository.' ) {
      owner.binding.Ant."${owner.antlibXMLns}:pom" ( id : mavenPOMId , file : 'pom.xml' )
      /*
       *  It seems that there is a wierd problem.  
        
      owner.binding.Ant.property ( name : 'flob.adob' , value : 'weed' )
      owner.binding.Ant.echo ( '${flob.adob}' )
      println ( owner.binding.Ant.project.properties.'flob.adob' )

      *  does exactly what you would expect, weed is printed out in both cases.  However:

      owner.binding.Ant.'antlib:org.apache.maven.artifact.ant:pom' ( id : 'blahblah' , file : 'pom.xml' )
      owner.binding.Ant.echo ( '${blahblah.version}' )
      println ( owner.binding.Ant.project.properties.'blahblah.version' )

      * prints out the version umber from Ant but null from Groovy :-(  This means we cannot run the consistency checks between POM and Gant file.
      */
      /*
      [ 'groupId' , 'artifactId' , ' version' ].each { item ->
                                                       if ( owner.binding.Ant.project.properties."${owner.mavenPOMId}.${item}" != owner."${item}" ) {
                                                         throw new RuntimeException ( "${item} in build file and POM not the same." )
                                                       }
      }
      */
      depends ( owner.binding.'package' )
      owner.binding.Ant."${owner.antlibXMLns}:install" ( file : owner.packagedArtifact  ) { pom ( refid : mavenPOMId ) }
    }
    properties.binding.target.call ( deploy : "Deploy the artefact: copy the artefact to the remote repository ${ properties.version =~ 'SNAPSHOT' ? properties.deploySnapshotURL : properties.deployURL }." ) {
      def label = 'deployURL'
      if ( owner.version =~ 'SNAPSHOT' ) { label = 'deploySnapshotURL' }
      def deployURL = owner."${label}"
       if ( ! deployURL ) { throw new RuntimeException ( "Maven.${label} must be set to achieve target deploy." ) }
      depends ( owner.binding.install )
      owner.binding.Ant."${owner.antlibXMLns}:deploy" ( file : owner.packagedArtifact  ) {
        pom ( refid : owner.mavenPOMId )
        remoteRepository ( url : deployURL )
      }
    }
    properties.binding.target.call ( site : 'Create the website.' ) {
      depends ( owner.binding.initialize )
      println ( 'Site not implemented as yet.' )
    }
    properties.binding.includeTargets << Clean
    properties.binding.cleanDirectory << "${properties.targetPath}"
  }
  public getProperty ( String name ) { properties [ name ] }
  public void setProperty ( String name , value ) {
    if ( readOnlyKeys.contains ( name ) ) { throw new RuntimeException ( "Cannot amend the property ${name}." ) }
    properties [ name ] = value
  }
}
