//  Gant -- A Groovy way of scripting Ant tasks.
//
//  Copyright © 2007–2011, 2013 Russel Winder
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

import org.codehaus.gant.GantBinding
import org.codehaus.gant.GantState

/**
 *  A class to provide the Maven 2 style lifecycle targets associated with a project.
 *
 *  @author Russel Winder <russel@winder.org.uk>
 */
final class Maven {
  private final defaultJUnitVersion = '4.8.1'
  private final defaultTestNGVersion = '5.11'
  private final readOnlyKeys = [ 'binding', 'compileDependenciesClasspathId', 'testDependenciesClasspathId', 'antlibXMLns', 'mavenPOMId' ]
  private final Map<String,Object> properties = [
                                  groupId: '',
                                  artifactId: '',
                                  version: '',
                                  sourcePath: 'src',
                                  mainSourcePath: '', // Defaults to standard Maven 2 convention.  Set in constructor since it uses a GString dependent on a value in the map.
                                  testSourcePath: '', // Defaults to standard Maven 2 convention.  Set in constructor since it uses a GString dependent on a value in the map.
                                  targetPath: 'target',
                                  mainCompilePath: '', // Defaults to standard Maven 2 convention.  Set in constructor since it uses a GString dependent on a value in the map.
                                  testCompilePath: '', // Defaults to standard Maven 2 convention.  Set in constructor since it uses a GString dependent on a value in the map.
                                  testReportPath: '', // Defaults to standard Maven 2 convention.  Set in constructor since it uses a GString dependent on a value in the map.
                                  metadataPath: '', // Defaults to standard Maven 2 convention.  Set in constructor since it uses a GString dependent on a value in the map.
                                  javaCompileProperties: [ source: '1.5', target: '1.5', debug: 'false' ],
                                  groovyCompileProperties: [: ],
                                  nestedJavacCompilerArgs: [ ],
                                  compileClasspath: [ ],
                                  testClasspath: [ ],
                                  remoteRepositories: [ ],
                                  compileDependencies: [ ],
                                  testDependencies: [ ],
                                  testFramework: 'junit',
                                  testFrameworkVersion: defaultJUnitVersion,
                                  testFrameworkClassifier: 'jdk15',
                                  packaging: 'jar',
                                  deployURL: '',
                                  deploySnapshotURL: '',
                                  deployId: 'dav.codehaus.org',
                                  manifest: [: ],
                                  manifestIncludes:  [ ],
                                  (readOnlyKeys[0]): null,
                                  (readOnlyKeys[1]): 'compile.dependency.classpath',
                                  (readOnlyKeys[2]): 'test.dependency.classpath',
                                  (readOnlyKeys[3]): 'antlib:org.apache.maven.artifact.ant',
                                  (readOnlyKeys[4]): 'maven.pom'
                                  ]
  /**
   *  Constructor for the "includeTargets <<" usage.
   *
   *  @param binding The <code>GantBinding</code> to bind to.
   */
  Maven(GantBinding binding) {
    properties.binding = binding
    properties.binding.maven = this
    initialize()
  }
   /**
   *  Constructor for the "includeTargets **" usage.
   *
   *  @param binding The <code>GantBinding</code> to bind to.
   *  @param map The <code>Map</code> of initialization parameters.
   */
   Maven(GantBinding binding, Map<String,String> map) {
     properties.binding = binding
     properties.binding.maven = this
     map.each { key, value -> owner.setProperty(key, value) }
     initialize()
   }
  /**
   *  Initialize all the values given the information presented to the constructors.  This is the second
   *  stage of construction and is separated out from the constructors since the GStrings will be
   *  initialized using the extant values at teh moment of construction and the two constructors need to
   *  pre-initialize things in slightly different ways.
   */
  private initialize() {
    properties.default_mainSourcePath = "${properties.sourcePath}${System.properties.'file.separator'}main"
    properties.mainSourcePath = properties.default_mainSourcePath
    properties.default_testSourcePath = "${properties.sourcePath}${System.properties.'file.separator'}test"
    properties.testSourcePath = properties.default_testSourcePath
    properties.mainCompilePath = "${properties.targetPath}${System.properties.'file.separator'}classes"
    properties.testCompilePath = "${properties.targetPath}${System.properties.'file.separator'}test-classes"
    properties.testReportPath = "${properties.targetPath}${System.properties.'file.separator'}test-reports"
    properties.metadataPath = "${properties.mainCompilePath}${System.properties.'file.separator'}META-INF"
    try { properties.binding.testFailIgnore }
    catch (MissingPropertyException mpe) { properties.binding.testFailIgnore = false }
    properties.binding.target.call(initialize: 'Ensure all the dependencies can be met and set classpaths accordingly.') {
      if (owner.testFramework == 'testng') {
        testngInstalled = false
        //
        //  Need to find a better way of working with the JUnit and TestNG version numbers.  There is to much "magic" here.
        //
        if (owner.testFrameworkVersion == defaultJUnitVersion) { owner.testFrameworkVersion = defaultTestNGVersion }
        owner.testDependencies.each { dependency -> if (dependency.artifactId == 'testng') { testngInstalled = true } }
        if (! testngInstalled) {
          owner.testDependencies << [
                                     groupId: 'org.testng',
                                     artifactId: 'testng',
                                     version: owner.testFrameworkVersion,
                                     scope: 'test',
                                     classifier: owner.testFrameworkClassifier
                                     ] }
      }
      def createDependencyMap = { dependencyMap, map ->
        [ 'groupId', 'artifactId', 'version', 'classifier' ].each { property ->
          if (map [ property ]) { dependencyMap [ property ] =  map [ property ] }
        }
        dependencyMap
      }
      if (owner.compileDependencies) {
        owner.binding.ant."${owner.antlibXMLns}:dependencies"(pathId: owner.compileDependenciesClasspathId) {
          if (owner.remoteRepositories) { owner.remoteRepositories.each { url -> remoteRepository(url: url) } }
          owner.compileDependencies.each { item -> dependency(createDependencyMap([ scope: 'compile' ], item)) }
        }
      }
      if (owner.testDependencies) {
        owner.binding.ant."${owner.antlibXMLns}:dependencies"(pathId: owner.testDependenciesClasspathId) {
          if (owner.remoteRepositories) { owner.remoteRepositories.each { url -> remoteRepository(url: url) } }
          owner.testDependencies.each { item -> dependency(createDependencyMap([ scope: 'test' ], item)) }
        }
      }
      //  Do not allow the output of the ant.property call to escape.  If the output is allowed out then
      //  Ant, Gant, Maven, Eclipse and IntelliJ IDEA all behave slightly differently.  This makes testing
      //  nigh on impossible.  Also the user doesn't need to know about these.
      owner.binding.ant.logger.messageOutputLevel = GantState.SILENT
      owner.binding.ant.taskdef(name: 'groovyc', classname: 'org.codehaus.groovy.ant.Groovyc')
      owner.binding.ant.logger.messageOutputLevel = GantState.verbosity
      //  Interesting side effect of using properties rather than a method call in the above statement.
      //  With method call, the value of the expression was equivalent to 0 so that was the value
      //  returned by this method, which eventually became the return value of the target.  Using
      //  properties, the assignment means the value of the statement is the assigned value which is
      //  whatever it is.  In the absence of an explicit return, Groovy uses the value of the last expression,
      //  which in this case is whatever was assigned above.  This is unlikely to be 0, and anyway is
      //  liable to change -- which is a bit non-deterministic.  If control gets here assume everything is
      //  fine and explicitly return 0.
      0
    }
    /*
    properties.binding.target.call(validate: 'Validate the project is correct and all necessary information is available.') {
      throw new RuntimeException('Validate not implemented as yet.')
    }
    properties.binding.target.call('generate-sources': 'Generate any source code for inclusion in compilation.') {
      throw new RuntimeException('Generate-sources not implemented as yet.')
    }
    properties.binding.target.call('process-sources': 'Process the source code, for example to filter any values.') {
      throw new RuntimeException('Process-sources not implemented as yet.')
    }
    properties.binding.target.call('generate-resources': 'Generate resources for inclusion in the package.') {
      throw new RuntimeException('Generate-resources not implemented as yet.')
    }
    properties.binding.target.call('process-resources': 'Copy and process the resources into the destination directory, ready for packaging.') {
      throw new RuntimeException('Process-resources not implemented as yet.')
    }
    */
    properties.binding.target.call(compile: "Compile the source code in ${properties.mainSourcePath} to ${properties.mainCompilePath}.") {
      depends(owner.binding.initialize)
      owner.binding.ant.mkdir(dir: owner.mainCompilePath)
      //  If a source path has been explicitly specified then compile everything in it using the joint
      //  compiler so there is no problem with it containing Groovy as well as Java code.  Otherwise assume
      //  Maven 2 hierarchy rules.
      if (owner.mainSourcePath != owner.default_mainSourcePath) {
        owner.binding.ant.groovyc([ srcdir: owner.mainSourcePath, destdir: owner.mainCompilePath, fork: 'true' ] + owner.groovyCompileProperties) {
          javac(owner.javaCompileProperties) {
            if (owner.nestedJavacCompilerArgs) { owner.nestedJavacCompilerArgs.each { arg -> compilerarg(value: arg) } }
          }
          classpath {
            pathelement(path: owner.compileClasspath.join(System.properties.'path.separator'))
            if (owner.compileDependencies) { path(refid: owner.compileDependenciesClasspathId) }
          }
        }
      }
      else {
        try {
          new File((String) owner.mainSourcePath).eachDir { directory ->
            switch (directory.name) {
             case 'java':
             //  Need to use the joint Groovy compiler here to deal wuth the case where Groovy files are in the
             //  Java hierarchy.
             owner.binding.ant.javac([ srcdir: owner.mainSourcePath + System.properties.'file.separator' + 'java', destdir: owner.mainCompilePath, fork: 'true' ] + owner.javaCompileProperties) {
               classpath {
                 pathelement(path: owner.compileClasspath.join(System.properties.'path.separator'))
                 if (owner.compileDependencies) { path(refid: owner.compileDependenciesClasspathId) }
               }
             }
             break
             case 'groovy':
             owner.binding.ant.groovyc([ srcdir: owner.mainSourcePath + System.properties.'file.separator' + 'groovy', destdir: owner.mainCompilePath, fork: 'true' ] + owner.groovyCompileProperties) {
               javac(owner.javaCompileProperties) {
                 if (owner.nestedJavacCompilerArgs) { owner.nestedJavacCompilerArgs.each { arg -> compilerarg(value: arg) } }
               }
               classpath {
                 pathelement(path: owner.compileClasspath.join(System.properties.'path.separator'))
                 if (owner.compileDependencies) { path(refid: owner.compileDependenciesClasspathId) }
               }
             }
             break
            }
          }
        }
        catch (FileNotFoundException fnfe) { throw new RuntimeException('Error: ' + owner.mainSourcePath + ' does not exist.', fnfe) }
      }
    }
    /*
    properties.binding.target.call('process-classes', 'Post-process the generated files from compilation, for example to do bytecode enhancement on Java classes.') {
      throw new RuntimeException('Process-classes not implemented as yet.')
    }
    properties.binding.target.call('generate-test-sources', 'Generate any test source code for inclusion in compilation.') {
      throw new RuntimeException('Generate-test-sources not implemented as yet.')
    }
    properties.binding.target.call('process-test-sources', 'Process the test source code, for example to filter any values.') {
      throw new RuntimeException('Process-test-sources not implemented as yet.')
    }
    properties.binding.target.call('generate-test-resources', 'Create resources for testing.') {
      throw new RuntimeException('Generate-test-sources not implemented as yet.')
    }
    properties.binding.target.call('process-test-resources', 'Copy and process the resources into the test destination directory.') {
      throw new RuntimeException('Process-test-sources not implemented as yet.')
    }
    */
    properties.binding.target.call('test-compile': "Compile the test source code in ${properties.testSourcePath} to ${properties.testCompilePath}.") {
      depends(owner.binding.compile)
      def doTest = true
      try { doTest = !(owner.binding.skipTest == 'true') }
      catch (MissingPropertyException mpe) { /* Intentionally blank */ }
      if (doTest) {
        owner.binding.ant.mkdir(dir: owner.testCompilePath )
        if (owner.testSourcePath != owner.default_testSourcePath) {
          if ((new File((String) owner.testSourcePath)).isDirectory()) {
            owner.binding.ant.groovyc([ srcdir: owner.testSourcePath, destdir: owner.testCompilePath, fork: 'true' ] + owner.groovyCompileProperties) {
              javac(owner.javaCompileProperties) {
                if (owner.nestedJavacCompilerArgs) { owner.nestedJavacCompilerArgs.each { arg -> compilerarg(value: arg) } }
              }
              classpath {
                pathelement(location: owner.mainCompilePath)
                pathelement(path: owner.compileClasspath.join(System.properties.'path.separator'))
                pathelement(path: owner.testClasspath.join(System.properties.'path.separator'))
                if (owner.compileDependencies) { path(refid: owner.compileDependenciesClasspathId) }
                if (owner.testDependencies) { path(refid: owner.testDependenciesClasspathId) }
              }
            }
          }
        }
        else {
          if ((new File((String) owner.testSourcePath)).isDirectory()) {
            try {
              new File((String) owner.default_testSourcePath).eachDir { directory ->
                switch (directory.name) {
                 case 'java':
                  //  Need to use the joint Groovy compiler here to deal with the case where Groovy files are in the
                  //  Java hierarchy.
                  owner.binding.ant.javac([ srcdir: owner.testSourcePath + System.properties.'file.separator' + 'java', destdir: owner.testCompilePath, fork: 'true' ] + owner.javaCompileProperties) {
                    classpath {
                      pathelement(location: owner.mainCompilePath)
                      pathelement(path: owner.compileClasspath.join(System.properties.'path.separator'))
                      pathelement(path: owner.testClasspath.join(System.properties.'path.separator'))
                      if (owner.compileDependencies) { path(refid: owner.compileDependenciesClasspathId) }
                      if (owner.testDependencies) { path(refid: owner.testDependenciesClasspathId) }
                    }
                  }
                  break
                 case 'groovy':
                  owner.binding.ant.groovyc([ srcdir: owner.testSourcePath + System.properties.'file.separator' + 'groovy', destdir: owner.testCompilePath, fork: 'true' ] + owner.groovyCompileProperties) {
                    javac(owner.javaCompileProperties) {
                      if (owner.nestedJavacCompilerArgs) { owner.nestedJavacCompilerArgs.each { arg -> compilerarg(value: arg) } }
                    }
                    classpath {
                      pathelement(location: owner.mainCompilePath)
                      pathelement(path: owner.compileClasspath.join(System.properties.'path.separator'))
                      pathelement(path: owner.testClasspath.join(System.properties.'path.separator'))
                      if (owner.compileDependencies) { path(refid: owner.compileDependenciesClasspathId) }
                      if (owner.testDependencies) { path(refid: owner.testDependenciesClasspathId) }
                    }
                  }
                  break
                }
              }
            }
            catch (FileNotFoundException fnfe) { throw new RuntimeException('Error: ' + owner.testSourcePath + ' does not exist.', fnfe) }
          }
        }
      }
    }
    properties.binding.target.call(test: "Run the tests using the ${properties.testFramework} unit testing framework.") {
      depends(owner.binding.'test-compile')
      def doTest = true
      try { doTest = !(owner.binding.skipTest == 'true') }
      catch (MissingPropertyException mpe) { /* Intentionally blank */ }
      if (doTest) {
        switch (owner.testFramework) {
         case 'testng':
          owner.binding.ant.taskdef(resource: 'testngtasks') { classpath { path(refid: owner.testDependenciesClasspathId) } }
          owner.binding.ant.testng(outputdir: owner.testReportPath) {
            classpath {
              pathelement(location: owner.mainCompilePath)
              pathelement(location: owner.testCompilePath)
              pathelement(path: owner.compileClasspath.join(System.properties.'path.separator'))
              pathelement(path: owner.testClasspath.join(System.properties.'path.separator'))
              if (owner.compileDependencies) { path(refid: owner.compileDependenciesClasspathId) }
              if (owner.testDependencies) { path(refid: owner.testDependenciesClasspathId) }
            }
            classfileset(dir: owner.testCompilePath)
          }
          break
         case 'junit':
         default:
          owner.binding.ant.mkdir(dir: owner.testReportPath)
          owner.binding.ant.junit(printsummary: 'yes', failureproperty: 'testsFailed', fork: 'true', forkmode: 'once') {
            classpath {
              pathelement(location: owner.mainCompilePath)
              pathelement(location: owner.testCompilePath)
              pathelement(path: owner.compileClasspath.join(System.properties.'path.separator'))
              pathelement(path: owner.testClasspath.join(System.properties.'path.separator'))
              if (owner.compileDependencies) { path(refid: owner.compileDependenciesClasspathId) }
              if (owner.testDependencies) { path(refid: owner.testDependenciesClasspathId) }
            }
            formatter(type: 'plain')
            formatter(type: 'xml')
            sysproperty(key: 'groovy.home', value: System.properties.'groovy.home')
            batchtest(todir: owner.testReportPath) { fileset(dir: owner.testCompilePath, includes: '**/*Test.class') }
          }
          break
        }
        try {
          // owner.binding.ant.project.properties.testsFailed may not exist, hence the MissingPropertyException capture.
          if (! owner.binding.testFailIgnore && owner.binding.ant.project.properties.testsFailed) { throw new RuntimeException('Tests failed, execution terminating.') }
        }
        catch (MissingPropertyException mpe) { /* Intentionally blank */ }
      }
    }
    properties.binding.target.call('package': "Package the artefact as a ${properties.packaging} in ${properties.mainCompilePath}.") {
      [ 'groupId', 'artifactId', 'version' ].each{item ->
        if (! owner."${item}") { throw new RuntimeException("maven.${item} must be set to achieve target package.") }
      }
      depends(owner.binding.test)
      if (owner.manifest) {
        owner.binding.ant.mkdir(dir: owner.metadataPath)
        owner.binding.ant.manifest(file: owner.metadataPath + System.properties.'file.separator' + 'MANIFEST.MF') {
          owner.manifest.each{key, value -> attribute(name: key, value: value)}
        }
      }
      if (owner.manifestIncludes) {
        owner.binding.ant.mkdir(dir: owner.metadataPath)
        owner.manifestIncludes.each{item ->
          if (new File((String) item).isDirectory()) { owner.binding.ant.copy(todir: owner.metadataPath) { fileset(dir: item, includes: '*') } }
          else { owner.binding.ant.copy(todir: owner.metadataPath, file: item) }
        }
      }
      switch (owner.packaging) {
       case 'war':
       def artifactPath = owner.targetPath + System.properties.'file.separator' + owner.artifactId + '-' + owner.version
       owner.packagedArtifact = artifactPath + '.war'
       owner.binding.ant.mkdir(dir: artifactPath)
       owner.binding.ant.copy(todir: artifactPath) {
         fileset(dir: classesDir)
         fileset(dir: ['src', 'main', 'webapp'].join(System.properties.'file.separator'))
       }
       owner.binding.ant.jar(destfile: owner.packagedArtifact) { fileset(dir: artifactPath) }
       break
       case 'jar':
       owner.packagedArtifact = owner.targetPath + System.properties.'file.separator' + owner.artifactId + '-' + owner.version + '.jar'
       owner.binding.ant.jar(destfile: owner.packagedArtifact) { fileset(dir: owner.mainCompilePath) }
      }
    }
    /*
    properties.binding.target.call('integration-test': 'Process and deploy the package if necessary into an environment where integration tests can be run.') {
      throw new RuntimeException('Integration-test not implemented as yet.')
    }
    properties.binding.target.call(verify: 'Run any checks to verify the package is valid and meets quality criteria.') {
      throw new RuntimeException('Verify not implemented as yet.')
    }
    */
    properties.binding.target.call(install: 'Install the artefact into the local repository.') {
      owner.binding.ant."${owner.antlibXMLns}:pom"(id: mavenPOMId, file: 'pom.xml')
      /*
       *  It seems that there is a wierd problem.

      owner.binding.ant.property(name: 'flob.adob', value: 'weed')
      owner.binding.ant.echo('${flob.adob}')
      println(owner.binding.ant.project.properties.'flob.adob')

      *  does exactly what you would expect, weed is printed out in both cases.  However:

      owner.binding.ant.'antlib:org.apache.maven.artifact.ant:pom'(id: 'blahblah', file: 'pom.xml')
      owner.binding.ant.echo('${blahblah.version}')
      println(owner.binding.ant.project.properties.'blahblah.version')

      * prints out the version number from ant but null from Groovy:-( This means we cannot run the consistency checks between POM and Gant file.
      */
      /*
      [ 'groupId', 'artifactId', ' version' ].each { item ->
                                                       if (owner.binding.ant.project.properties."${owner.mavenPOMId}.${item}" != owner."${item}") {
                                                         throw new RuntimeException("${item} in build file and POM not the same.")
                                                       }
      }
      */
      depends(owner.binding.'package')
      owner.binding.ant."${owner.antlibXMLns}:install"(file: owner.packagedArtifact ) { pom(refid: mavenPOMId) }
    }
    properties.binding.target.call(deploy: "Deploy the artefact: copy the artefact to the remote repository ${ properties.version =~('SNAPSHOT' ? properties.deploySnapshotURL: properties.deployURL) }.") {
      def label = 'deployURL'
      if (owner.version =~ 'SNAPSHOT') { label = 'deploySnapshotURL' }
      def deployURL = owner."${label}"
       if (! deployURL) { throw new RuntimeException("maven.${label} must be set to achieve target deploy.") }
      depends(owner.binding.install)
      owner.binding.ant."${owner.antlibXMLns}:install-provider"(artifactId: 'wagon-webdav', version: '1.0-beta-2')
      //
      //  This task does not create new directories on the server if they are needed:-(
      //
      owner.binding.ant."${owner.antlibXMLns}:deploy"(file: owner.packagedArtifact ) {
        pom(refid: owner.mavenPOMId)
        remoteRepository(url: deployURL, id: owner.deployId)
      }
    }
    properties.binding.target.call(site: 'Create the website.') {
      depends(owner.binding.initialize)
      throw new RuntimeException('Site not implemented as yet.')
    }
    properties.binding.includeTargets << Clean
    properties.binding.cleanDirectory << "${properties.targetPath}"
  }
  public getProperty(String name) { properties [ name ] }
  public void setProperty(String name, value) {
    if (readOnlyKeys.contains(name)) { throw new RuntimeException("Cannot amend the property ${name}.") }
    properties[name] = value
  }
}
