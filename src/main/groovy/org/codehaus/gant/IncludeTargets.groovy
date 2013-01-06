//  Gant -- A Groovy way of scripting Ant tasks.
//
//  Copyright © 2006–2008, 2010, 2013  Russel Winder
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

/**
 *  An instance of this class is provided to each Gant script for including targets.  Targets can be
 *  provided by Gant (sub)scripts, Groovy classes, or Java classes.
 *
 *  @author Russel Winder <russel@winder.org.uk>
 *  @author Graeme Rocher <graeme.rocher@gmail.com>
 */
class IncludeTargets extends AbstractInclude {
  /**
   *  Constructor.
   *
   *  @param binding The <code>GantBinding</code> to associate with.
   */
  IncludeTargets(GantBinding binding) { super(binding) }
  /**
   *  Implementation of the << operator taking a <code>Class</code> parameter.
   *
   *  @param theClass The <code>Class</code> to load and instantiate.
   *  @return The includer object to allow for << chaining.
   */
  def leftShift(final Class<?> theClass) {
    def className = theClass.name
    if (!(className in loadedClasses)) {
      createInstance(theClass)
      loadedClasses << className
    }
    this
  }
  /**
   *  Implementation of the << operator taking a <code>File</code> parameter.
   *
   *  @param file The <code>File</code> to load, compile, and instantiate.
   *  @return The includer object to allow for << chaining.
   */
  def leftShift(final File file) {
    def className = file.name
    if (!(className in loadedClasses)) {
      if (binding.cacheEnabled) {
        //  Class name will likely have packages, but this is not acceptable for a single name in the
        //  binding, so convert any dots to underscores.
        def script = binding.loadClassFromCache.call(className.replaceAll(/\./, '_'), file.lastModified(), file)
        script.binding = binding
        script.run()
      }
      else { readFile(file) }
      loadedClasses << className
    }
    this
  }
  /**
   *  Implementation of the << operator taking a <code>String</code> parameter.
   *
   *  @param s The <code>String</code> to compile and instantiate.
   *  @return The includer object to allow for << chaining.
   */
  def leftShift(final String s) { binding.groovyShell.evaluate(s); this }
  /**
   *  Implementation of the * operator taking a <code>Map</code> parameter.  This operator only makes
   *  sense immediately after a ** operator, since only then is there a <code>Class</code> to instantiate.
   *
   *  @param keywordParameter The <code>Map</code> of parameters to the constructor.
   *  @return The includer object to allow for ** * operator chaining.
   */
  def multiply(final Map<String,String> keywordParameters) {
    if (pendingClass != null) {
      def className = pendingClass.name
      if (!(className in loadedClasses)) {
        createInstance(pendingClass, keywordParameters)
        loadedClasses << className
      }
      pendingClass = null
    }
    this
  }
}
