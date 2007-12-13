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
  private final Map properties = [
                                  binding : null ,
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
                                  javaCompileProperties : [ source : '1.3' , target : '1.3' , debug : 'false' ] ,
                                  groovyCompileProperties : [ : ] ,
                                  compileDependenciesClasspathId :  'compile.dependency.classpath' ,
                                  compileClasspath : [ ] ,
                                  testDependenciesClasspathId :  'test.dependency.classpath' ,
                                  testClasspath : [ ] ,
                                  compileDependencies : [ ] ,
                                  testDependencies : [ ] ,
                                  testFramework : 'junit' ,
                                  testFailIgnore : false ,
                                  packaging : 'jar'
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
    properties.binding.target.call ( initialize : 'Ensure all the dependencies can be met and set classpaths accordingly.' ) {
      if ( owner.testFramework == 'testng' ) {
        testngInstalled = false
        owner.testDependencies.each { dependency -> if ( dependency.artifactId == 'testng' ) { testngInstalled = true } }
        if ( ! testngInstalled ) { owner.testDependencies << [ groupId : 'org.testng' , artifactId : 'testng' , version : '5.7' , scope : 'test' , classifier : 'jdk15' ] }
      }
      if ( owner.compileDependencies || owner.testDependencies  ) {
        owner.binding.Ant.typedef ( resource : 'org/apache/maven/artifact/ant/antlib.xml' , uri : 'urn:maven-artifact-ant' ) {
          classpath { pathelement ( location : System.properties.'groovy.home' + System.properties.'file.separator' + 'lib' + System.properties.'file.separator' + 'maven-artifact-ant-2.0.4-dep.jar' ) }
        }
        if ( owner.compileDependencies ) {
          owner.binding.Ant.'antlib:org.apache.maven.artifact.ant:dependencies' ( pathId : owner.compileDependenciesClasspathId ) {
            owner.compileDependencies.each { item ->
                                                        dependency ( groupId : item.groupId , artifactId : item.artifactId , version : item.version , classifier : item.classifier )
            }
          }
        }
        if ( owner.testDependencies ) {
          owner.binding.Ant.'antlib:org.apache.maven.artifact.ant:dependencies' ( pathId : owner.testDependenciesClasspathId ) {
            owner.testDependencies.each { item ->
                                                     dependency ( groupId : item.groupId , artifactId : item.artifactId , version : item.version , classifier : item.classifier )
            }
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
    properties.binding.target.call ( compile : 'Compile the source code of the project.' ) {
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
    properties.binding.target.call ( 'test-compile' : 'Compile the test source code into the test destination directory.' ) {
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
    properties.binding.target.call ( test : "Run tests using the ${properties.testFramework} unit testing framework." ) {
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
    properties.binding.target.call ( 'package' : "Package the artefact as a ${properties.packaging}." ) {
       if ( ! owner.groupId ) { throw new RuntimeException ( 'Maven.groupId must be set to achieve target package.' ) }
       if ( ! owner.artifactId ) { throw new RuntimeException ( 'Maven.artifactId must be set to achieve target package.' ) }
       if ( ! owner.version ) { throw new RuntimeException ( 'Maven.version must be set to achieve target package.' ) }
      depends ( owner.binding.test )
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
      depends ( owner.binding.'package' )
      def mavenProjectId = 'maven.project'
      owner.binding.Ant.'antlib:org.apache.maven.artifact.ant:pom' ( id : mavenProjectId , file : 'pom.xml' )
      owner.binding.Ant.'antlib:org.apache.maven.artifact.ant:install' ( file : owner.packagedArtifact  ) { pom ( refid : mavenProjectId ) }
    }
    properties.binding.target.call ( deploy : 'Deploy the artefact: copy the artefact to the remote repository.' ) {
       if ( ! owner.deployURL ) { throw new RuntimeException ( 'Maven.deployURL must be set to achieve target deploy.' ) }
      depends ( owner.binding.'package' )
      def mavenProjectId = 'maven.project'
      owner.binding.Ant.'antlib:org.apache.maven.artifact.ant:pom' ( id : mavenProjectId , file : 'pom.xml' )
      owner.binding.Ant.'antlib:org.apache.maven.artifact.ant:deploy' ( file : owner.packagedArtifact  ) {
        pom ( refid : mavenProjectId )
        remoteRepository ( url : owner.deployURL )
      }
    }
    properties.binding.includeTargets << Clean
    properties.binding.cleanDirectory << "${properties.targetPath}"
  }
  public getProperty ( String name ) { properties [ name ] }
  public void setProperty ( String name , value ) { properties [ name ] = value }
}
