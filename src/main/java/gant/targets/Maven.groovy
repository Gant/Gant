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
  private final Binding binding
  private final Map properties = [
                                  sourcePath : 'src' ,
                                  mainSourcePath : '' , // Set in constructor since it uses a GString dependent on a value in the map.
                                  testSourcePath : '' , // Set in constructor since it uses a GString dependent on a value in the map.
                                  targetPath : 'target' ,
                                  mainCompilePath : '' , // Set in constructor since it uses a GString dependent on a value in the map.
                                  testCompilePath : '' , // Set in constructor since it uses a GString dependent on a value in the map.
                                  testReportPath : '' , // Set in constructor since it uses a GString dependent on a value in the map.
                                  javaCompileProperties : [ source : '1.3' , target : '1.3' , debug : 'false' ] ,
                                  groovyCompileProperties : [ : ] ,
                                  dependenciesClasspathId :  'dependency.classpath' ,
                                  classpath : [ ] ,
                                  dependencies : [ ] ,
                                  testFramework : 'junit'
                                  ]
  Maven ( Binding binding ) {
    this.binding = binding
    properties.mainSourcePath = "${properties.sourcePath}/main"
    properties.testSourcePath = "${properties.sourcePath}/test"
    properties.mainCompilePath = "${properties.targetPath}/classes"
    properties.testCompilePath = "${properties.targetPath}/test-classes"
    properties.testReportPath = "${properties.targetPath}/test-reports"
    binding.target.call ( initialize : 'Ensure all the dependencies can be met and alter classpaths accordingly.' ) {
      if ( owner.testFramework == 'testng' ) {
        owner.dependencies << [ groupId : 'org.testng' , artifactId : 'testng' , version : '5.7' , scope : 'test' , classifier : 'jdk15' ]
      }
      if ( owner.dependencies ) {
        binding.Ant.typedef ( resource : 'org/apache/maven/artifact/ant/antlib.xml' , uri : 'urn:maven-artifact-ant' ) {
          classpath { pathelement ( location : System.properties.'groovy.home' + '/lib/maven-artifact-ant-2.0.4-dep.jar' ) }
        }
        binding.Ant.'antlib:org.apache.maven.artifact.ant:dependencies' ( pathId : owner.dependenciesClasspathId ) {
          owner.dependencies.each { item ->
                                    dependency ( groupId : item.groupId , artifactId : item.artifactId , version : item.version , classifier : item.classifier )
          }
        }
      }
    }
    /*
    binding.target.call ( validate : 'Validate the project is correct and all necessary information is available.' ) {
      println ( 'Validate not implemented as yet.' )
    }
    binding.target.call ( 'generate-sources' : 'Generate any source code for inclusion in compilation.' ) {
      println ( 'Generate-sources not implemented as yet.' )
    }
    binding.target.call ( 'process-sources' : 'Process the source code, for example to filter any values.' ) {
      println ( 'Process-sources not implemented as yet.' )
    }
    binding.target.call ( 'generate-resources' : 'Generate resources for inclusion in the package.' ) {
      println ( 'Generate-resources not implemented as yet.' )
    }
    binding.target.call ( 'process-resources' : 'Copy and process the resources into the destination directory, ready for packaging.' ) {
      println ( 'Process-resources not implemented as yet.' )
    }
    */
    binding.target.call ( compile : 'Compile the source code of the project.' ) {
      depends ( binding.initialize )
      binding.Ant.mkdir ( dir : owner.mainCompilePath )
      new File ( owner.mainSourcePath ).eachDir { directory ->
        switch ( directory.name ) {
         case 'java' :
          //  Need to use the joint Groovy compiler here to deal wuth the case where Groovy files are in the
          //  Java hierarchy.
         binding.Ant.javac ( [ srcdir : owner.mainSourcePath + '/java' , destdir : owner.mainCompilePath ] + owner.javaCompileProperties ) {
           classpath {
             pathelement ( path : owner.classpath.join ( ':' ) )
             path ( refid : owner.dependenciesClasspathId )
           }
         }
         break
         case 'groovy' :
         binding.Ant.taskdef ( name : 'groovyc' , classname : 'org.codehaus.groovy.ant.Groovyc' )
         binding.Ant.groovyc ( [ srcdir : owner.mainSourcePath + '/groovy' , destdir : owner.mainCompilePath ] + owner.groovyCompileProperties ) {
           classpath {
             pathelement ( path : owner.classpath.join ( ':' ) )
             path ( refid : owner.dependenciesClasspathId )
           }
         }
         break
        }
      }
    }
    /*
    binding.target.call ( 'process-classes' , 'Post-process the generated files from compilation, for example to do bytecode enhancement on Java classes.' ) {
      println ( 'Process-classes not implemented as yet.' )
    }
    binding.target.call ( 'generate-test-sources' , 'Generate any test source code for inclusion in compilation.' ) {
      println ( 'Generate-test-sources not implemented as yet.' )
    }
    binding.target.call ( 'process-test-sources' , 'Process the test source code, for example to filter any values.' ) {
      println ( 'Process-test-sources not implemented as yet.' )
    }
    binding.target.call ( 'generate-test-resources' , 'Create resources for testing.' ) {
      println ( 'Generate-test-sources not implemented as yet.' )
    }
    binding.target.call ( 'process-test-resources' , 'Copy and process the resources into the test destination directory.' ) {
      println ( 'Process-test-sources not implemented as yet.' )
    }
    */
    binding.target.call ( 'test-compile' : 'Compile the test source code into the test destination directory.' ) {
      depends ( binding.compile )
      binding.Ant.mkdir ( dir : owner.testCompilePath  )
      new File ( owner.mainSourcePath ).eachDir { directory ->
        switch ( directory.name ) {
         case 'java' :
          //  Need to use the joint Groovy compiler here to deal wuth the case where Groovy files are in the
          //  Java hierarchy.
         binding.Ant.javac ( [ srcdir : owner.testSourcePath + '/java' , destdir : owner.testCompilePath ] + owner.javaCompileProperties ) {
           classpath {
             pathelement ( location : owner.mainCompilePath )
             pathelement ( path : owner.classpath.join ( ':' ) )
             if ( owner.dependencies ) { path ( refid : owner.dependenciesClasspathId ) }
           }
         }
         break
         case 'groovy' :
         binding.Ant.taskdef ( name : 'groovyc' , classname : 'org.codehaus.groovy.ant.Groovyc' )
         binding.Ant.groovyc ( [ srcdir : owner.testSourcePath + '/groovy' , destdir : owner.testCompilePath ] + owner.groovyCompileProperties ) {
           classpath {
             pathelement ( location : owner.mainCompilePath )
             pathelement ( path : owner.classpath.join ( ':' ) )
             if ( owner.dependencies ) { path ( refid : owner.dependenciesClasspathId ) }
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
       binding.Ant.taskdef ( resource : 'testngtasks' ) { classpath { path ( refid : owner.dependenciesClasspathId ) } }
       binding.Ant.testng ( outputdir : owner.testReportPath ) {
         classpath {
           fileset ( dir : System.properties.'groovy.home' + '/lib' , includes : '*.jar' )
           pathelement ( location : owner.mainCompilePath )
           pathelement ( location : owner.testCompilePath )
           if ( owner.dependencies ) { path ( refid : owner.dependenciesClasspathId ) }
         }
         classfileset ( dir : owner.testCompilePath )
       }
       break
       case 'junit' :
       default :
       binding.Ant.mkdir ( dir : owner.testReportPath )
       binding.Ant.junit ( printsummary : 'yes' ) {
         classpath {
           pathelement ( location : owner.mainCompilePath )
           pathelement ( location : owner.testCompilePath )
           if ( owner.dependencies ) { path ( refid : owner.dependenciesClasspathId ) }
         }
         formatter ( type : 'plain' )
         batchtest ( todir : owner.testReportPath ) {
           fileset ( dir : owner.testCompilePath , includes : '**/*Test.class' )
         }
       }
       break
      }
    }
    /*
    binding.target.call ( 'package' : 'Package the artefact: take the compiled code and package it in its distributable format, such as a JAR.' ) {
      println ( 'Package not implemented as yet.' )
    }
    binding.target.call ( 'integration-test' : 'Process and deploy the package if necessary into an environment where integration tests can be run.' ) {
      println ( 'Integration-test not implemented as yet.' )
    }
    binding.target.call ( verify : 'Run any checks to verify the package is valid and meets quality criteria.' ) {
      println ( 'Verify not implemented as yet.' )
    }
    binding.target.call ( install : 'Install the package into the local repository, for use as a dependency in other projects locally.' ) {
      println ( 'Install not implemented as yet.' )
    }
    binding.target.call ( deploy : 'Deploy the artefact: done in an integration or release environment, copies the final package to the remote repository for sharing with other developers and projects.' ) {
      println ( 'Deploy not implemented as yet.' )
    }
    */
    binding.target.call ( clean : 'Clean everything.' ) {
      binding.Ant.delete ( dir : owner.targetPath , quiet : 'true' )
      binding.Ant.delete ( quiet : 'false' ) {
        fileset ( dir : '.' , includes : '**/*~' , defaultexcludes : 'false' )
      }
    }
  }
  def getProperty ( String name ) { properties [ name ] }
  void setProperty ( String name , value ) { properties [ name ] = value }
}
