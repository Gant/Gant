//  Gant -- A Groovy way of scripting Ant tasks.
//
//  Copyright  © 2013  Russel Winder
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

package org.codehaus.gant.tests.bugs.subPackage

import org.codehaus.gant.GantBinding

class GANT_29_SampleTool {
  private final Map<String,String> properties = [name: '']
  GANT_29_SampleTool(GantBinding binding) { properties.binding = binding }
  public getProperty(String name) { properties[name] }
  public void setProperty(String name, value) { properties[name] = value }
}
