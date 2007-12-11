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
                                  packaging : 'jar'
                                  ]
   Maven ( Binding binding ) {
     properties.Ant = binding.Ant
     constructMavenObject ( binding )
   }
   Maven ( Binding binding , Map map ) {
     properties.Ant = binding.Ant
     map.each { key , value -> owner.setProperty ( key , value ) }
     constructMavenObject ( binding )
   }
  def constructMavenObject ( binding ) {
    properties.mainSourcePath = "${properties.sourcePath}${System.properties.'file.separator'}main"
    properties.testSourcePath = "${properties.sourcePath}${System.properties.'file.separator'}test"
    properties.mainCompilePath = "${properties.targetPath}${System.properties.'file.separator'}classes"
    properties.testCompilePath = "${properties.targetPath}${System.properties.'file.separator'}test-classes"
    properties.testReportPath = "${properties.targetPath}${System.properties.'file.separator'}test-reports"
    binding.target.call ( initialize : 'Ensure all the dependencies can be met and set classpaths accordingly.' ) {
      if ( owner.testFramework == 'testng' ) {
        owner.testDependencies << [ groupId : 'org.testng' , artifactId : 'testng' , version : '5.7' , scope : 'test' , classifier : 'jdk15' ]
      }
      if ( owner.compileDependencies || owner.testDependencies  ) {
        owner.Ant.typedef ( resource : 'org/apache/maven/artifact/ant/antlib.xml' , uri : 'urn:maven-artifact-ant' ) {
          classpath { pathelement ( location : System.properties.'groovy.home' + System.properties.'file.separator' + 'lib' + System.properties.'file.separator' + 'maven-artifact-ant-2.0.4-dep.jar' ) }
        }
        if ( owner.compileDependencies ) {
          owner.Ant.'antlib:org.apache.maven.artifact.ant:dependencies' ( pathId : owner.compileDependenciesClasspathId ) {
            owner.compileDependencies.each { item ->
                                             dependency ( groupId : item.groupId , artifactId : item.artifactId , version : item.version , classifier : item.classifier )
            }
          }
        }
        if ( owner.testDependencies ) {
          owner.Ant.'antlib:org.apache.maven.artifact.ant:dependencies' ( pathId : owner.testDependenciesClasspathId ) {
            owner.testDependencies.each { item ->
                                          dependency ( groupId : item.groupId , artifactId : item.artifactId , version : item.version , classifier : item.classifier )
            }
          }
        }
      }
    }
    /*
    binding.target.call ( validate : 'Validate the project is correct and all necessary information is available.' ) {
      throw new RuntimeException ( 'Validate not implemented as yet.' )
    }
    binding.target.call ( 'generate-sources' : 'Generate any source code for inclusion in compilation.' ) {
      throw new RuntimeException ( 'Generate-sources not implemented as yet.' )
    }
    binding.target.call ( 'process-sources' : 'Process the source code, for example to filter any values.' ) {
      throw new RuntimeException ( 'Process-sources not implemented as yet.' )
    }
    binding.target.call ( 'generate-resources' : 'Generate resources for inclusion in the package.' ) {
      throw new RuntimeException ( 'Generate-resources not implemented as yet.' )
    }
    binding.target.call ( 'process-resources' : 'Copy and process the resources into the destination directory, ready for packaging.' ) {
      throw new RuntimeException ( 'Process-resources not implemented as yet.' )
    }
    */
    binding.target.call ( compile : 'Compile the source code of the project.' ) {
      depends ( binding.initialize )
      owner.Ant.mkdir ( dir : owner.mainCompilePath )
      new File ( owner.mainSourcePath ).eachDir { directory ->
        switch ( directory.name ) {
         case 'java' :
          //  Need to use the joint Groovy compiler here to deal wuth the case where Groovy files are in the
          //  Java hierarchy.
         owner.Ant.javac ( [ srcdir : owner.mainSourcePath + System.properties.'file.separator' + 'java' , destdir : owner.mainCompilePath ] + owner.javaCompileProperties ) {
           classpath {
             pathelement ( path : owner.compileClasspath.join ( System.properties.'path.separator' ) )
             if ( owner.compileDependencies ) { path ( refid : owner.compileDependenciesClasspathId ) }
           }
         }
         break
         case 'groovy' :
         owner.Ant.taskdef ( name : 'groovyc' , classname : 'org.codehaus.groovy.ant.Groovyc' )
         owner.Ant.groovyc ( [ srcdir : owner.mainSourcePath + System.properties.'file.separator' + 'groovy' , destdir : owner.mainCompilePath ] + owner.groovyCompileProperties ) {
           classpath {
             pathelement ( path : owner.compileClasspath.join ( System.properties.'path.separator' ) )
             if ( owner.compileDependencies ) { path ( refid : owner.compileDependenciesClasspathId ) }
           }
         }
         break
        }
      }
    }
    /*
    binding.target.call ( 'process-classes' , 'Post-process the generated files from compilation, for example to do bytecode enhancement on Java classes.' ) {
      throw new RuntimeException ( 'Process-classes not implemented as yet.' )
    }
    binding.target.call ( 'generate-test-sources' , 'Generate any test source code for inclusion in compilation.' ) {
      throw new RuntimeException ( 'Generate-test-sources not implemented as yet.' )
    }
    binding.target.call ( 'process-test-sources' , 'Process the test source code, for example to filter any values.' ) {
      throw new RuntimeException ( 'Process-test-sources not implemented as yet.' )
    }
    binding.target.call ( 'generate-test-resources' , 'Create resources for testing.' ) {
      throw new RuntimeException ( 'Generate-test-sources not implemented as yet.' )
    }
    binding.target.call ( 'process-test-resources' , 'Copy and process the resources into the test destination directory.' ) {
      throw new RuntimeException ( 'Process-test-sources not implemented as yet.' )
    }
    */
    binding.target.call ( 'test-compile' : 'Compile the test source code into the test destination directory.' ) {
      depends ( binding.compile )
      owner.Ant.mkdir ( dir : owner.testCompilePath  )
      new File ( owner.mainSourcePath ).eachDir { directory ->
        switch ( directory.name ) {
         case 'java' :
          //  Need to use the joint Groovy compiler here to deal wuth the case where Groovy files are in the
          //  Java hierarchy.
         owner.Ant.javac ( [ srcdir : owner.testSourcePath + System.properties.'file.separator' + 'java' , destdir : owner.testCompilePath ] + owner.javaCompileProperties ) {
           classpath {
             pathelement ( location : owner.mainCompilePath )
             pathelement ( path : owner.compileClasspath.join ( System.properties.'path.separator' ) )
             pathelement ( path : owner.testClasspath.join ( System.properties.'path.separator' ) )
             if ( owner.compileDependencies ) { path ( refid : owner.compileDependenciesClasspathId ) }
             if ( owner.testDependencies ) { path ( refid : owner.testDependenciesClasspathId ) }
           }
         }
         break
         case 'groovy' :
         owner.Ant.taskdef ( name : 'groovyc' , classname : 'org.codehaus.groovy.ant.Groovyc' )
         owner.Ant.groovyc ( [ srcdir : owner.testSourcePath + System.properties.'file.separator' + 'groovy' , destdir : owner.testCompilePath ] + owner.groovyCompileProperties ) {
           classpath {
             pathelement ( location : owner.mainCompilePath )
             pathelement ( path : owner.compileClasspath.join ( System.properties.'path.separator' ) )
             pathelement ( path : owner.testClasspath.join ( System.properties.'path.separator' ) )
             if ( owner.compileDependencies ) { path ( refid : owner.compileDependenciesClasspathId ) }
             if ( owner.testDependencies ) { path ( refid : owner.testDependenciesClasspathId ) }
           }
         }
         break
        }
      }
    }
    binding.target.call ( test : "Run tests using the ${properties.testFramework} unit testing framework. These tests should not require the code be packaged or deployed." ) {
      depends ( binding.'test-compile' )
      switch ( owner.testFramework ) {
       case 'testng' :
       owner.Ant.taskdef ( resource : 'testngtasks' ) { classpath { path ( refid : owner.testDependenciesClasspathId ) } }
       owner.Ant.testng ( outputdir : owner.testReportPath ) {
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
       owner.Ant.mkdir ( dir : owner.testReportPath )
       owner.Ant.junit ( printsummary : 'yes' ) {
         classpath {
           pathelement ( location : owner.mainCompilePath )
           pathelement ( location : owner.testCompilePath )
           pathelement ( path : owner.compileClasspath.join ( System.properties.'path.separator' ) )
           pathelement ( path : owner.testClasspath.join ( System.properties.'path.separator' ) )
           if ( owner.compileDependencies ) { path ( refid : owner.compileDependenciesClasspathId ) }
           if ( owner.testDependencies ) { path ( refid : owner.testDependenciesClasspathId ) }
         }
         formatter ( type : 'plain' )
         batchtest ( todir : owner.testReportPath ) {
           fileset ( dir : owner.testCompilePath , includes : '**/*Test.class' )
         }
       }
       break
      }
    }
    binding.target.call ( 'packageX' : 'Package the artefact: take the compiled code and package it in its distributable format, such as a JAR.' ) {
       if ( ! owner.groupId ) { throw new RuntimeException ( 'Maven.groupId must be set to achieve target package.' ) }
       if ( ! owner.artifactId ) { throw new RuntimeException ( 'Maven.artifactId must be set to achieve target package.' ) }
       if ( ! owner.version ) { throw new RuntimeException ( 'Maven.version must be set to achieve target package.' ) }
      depends ( binding.test )
      switch ( owner.packaging ) {
       case 'war' :
       def artifactPath = owner.properties.targetPath + System.properties.'file.separator' + owner.artifactId + '-' + owner.version
       owner.Ant.mkdir ( dir : artifactPath )
       owner.Ant.copy ( todir : artifactPath ) {
         fileset ( dir : classesDir )
         fileset ( dir : [ 'src' , 'main' , 'webapp' ].join ( System.properties.'file.separator' ) )
       }
       owner.Ant.jar ( destfile : artifactPath + '.war' ) { fileset ( dir : artifactPath ) }
       break
       case 'jar' :
       owner.Ant.jar ( destfile : owner.targetPath + System.properties.'file.separator' + owner.artifactId + '-' + owner.version + '.jar' ) {
         fileset ( dir : owner.mainCompilePath ) 
       }
      }
    }
    /*
    binding.target.call ( 'integration-test' : 'Process and deploy the package if necessary into an environment where integration tests can be run.' ) {
      throw new RuntimeException ( 'Integration-test not implemented as yet.' )
    }
    binding.target.call ( verify : 'Run any checks to verify the package is valid and meets quality criteria.' ) {
      throw new RuntimeException ( 'Verify not implemented as yet.' )
    }
    binding.target.call ( install : 'Install the package into the local repository, for use as a dependency in other projects locally.' ) {
      throw new RuntimeException ( 'Install not implemented as yet.' )
    }
    binding.target.call ( deploy : 'Deploy the artefact: done in an integration or release environment, copies the final package to the remote repository for sharing with other developers and projects.' ) {
      throw new RuntimeException ( 'Deploy not implemented as yet.' )
    }
    */
    binding.target.call ( clean : 'Clean everything.' ) {
      owner.Ant.delete ( dir : owner.targetPath , quiet : 'true' )
      owner.Ant.delete ( quiet : 'false' ) {
        fileset ( dir : '.' , includes : '**/*~' , defaultexcludes : 'false' )
      }
    }
  }
  def getProperty ( String name ) { properties [ name ] }
  void setProperty ( String name , value ) { properties [ name ] = value }
}
