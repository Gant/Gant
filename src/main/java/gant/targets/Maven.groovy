//  Gant -- A Groovy build tool based on scripting Ant tasks
//
//  Copyright © 2007 Russel Winder <russel@russel.org.uk>
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
 *  @author Russel Winder <russel@russel.org.uk>
 *  @version $Revision: 4214 $ $Date: 2006-11-13 08:59:52 +0000 (Mon, 13 Nov 2006) $
 */
final class Maven {
  private final Binding binding
  Maven ( Binding binding ) {
    this.binding = binding
    binding.mavenSourcePath = 'src'
    binding.mavenMainSourcePath = "${binding.mavenSourcePath}/main"
    binding.mavenMainSourceJavaPath = "${binding.mavenMainSourcePath}/java"
    binding.mavenTestSourcePath = "${binding.mavenSourcePath}/test"
    binding.mavenTestSourceJavaPath = "${binding.mavenTestSourcePath}/java"
    binding.mavenTargetPath = 'target'
    binding.mavenMainCompilePath = "${binding.mavenTargetPath}/classes"
    binding.mavenTestCompilePath = "${binding.mavenTargetPath}/test-classes"
    binding.mavenCompileProperties = [ source : '1.3' , target : '1.3' , debug : 'false' ]
    binding.mavenDependencyClasspathId =  'dependency.classpath'
    binding.mavenClasspath = [ ]
    binding.mavenDependency = [ ]
    binding.target.call ( initialize : 'Ensure all the dependencies can be met and alter classpaths accordingly.' ) {
      if ( binding.mavenDependency ) {
        binding.Ant.typedef ( resource : 'org/apache/maven/artifact/ant/antlib.xml' , uri : 'urn:maven-artifact-ant' ) {
          classpath { pathelement ( location : System.properties.'groovy.home' + '/lib/maven-artifact-ant-2.0.4-dep.jar' ) }
        }
        binding.Ant.'antlib:org.apache.maven.artifact.ant:dependencies' ( pathId : binding.mavenDependencyClasspathId ) {
          binding.mavenDependency.each { item ->
                                         dependency ( groupId : item [ 'groupId' ] , artifactId : item [ 'artifactId' ] , version : item [ 'version' ] , classifier : item [ 'classifier' ] )
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
      binding.Ant.mkdir ( dir : binding.mavenMainCompilePath )
      binding.Ant.javac ( [ srcdir : binding.mavenMainSourceJavaPath , destdir : binding.mavenMainCompilePath , fork : 'yes' ] + binding.mavenCompileProperties ) {
        classpath {
          pathelement ( path : binding.mavenClasspath.join ( ':' ) )
          path ( refid : binding.mavenDependencyClasspathId )
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
      binding.Ant.mkdir ( dir : binding.mavenTestCompilePath  )
      binding.Ant.javac ( [ srcdir : binding.mavenTestSourceJavaPath , destdir : binding.mavenTestCompilePath , fork : 'yes' ] + binding.mavenCompileProperties ) {
        classpath {
          pathelement ( location : binding.mavenMainCompilePath )
          pathelement ( path : binding.mavenClasspath.join ( ':' ) )
          path ( refid : binding.mavenDependencyClasspathId )
        }
      } 
    }
    binding.target.call ( test : 'Run tests using a suitable unit testing framework. These tests should not require the code be packaged or deployed.' ) {
      depends ( binding.'test-compile' )
      println ( 'Run the tests.' )
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
      binding.Ant.delete ( dir : binding.mavenTargetPath , quiet : 'true' )
      binding.Ant.delete ( quiet : 'false' ) {
        fileset ( dir : '.' , includes : '**/*~' , defaultexcludes : 'false' )
      }
    }
  }
}
