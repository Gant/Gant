//  Gant -- A Groovy way of scripting Ant tasks.
//
//  Copyright © 2008 Russel Winder
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

import org.apache.tools.ant.BuildEvent
import org.apache.tools.ant.Project
import org.apache.tools.ant.Target
import org.apache.tools.ant.Task

/**
 * Extended version of the BuildEvent class that provides access to the GantBinding
 *
 * @author Graeme Rocher
 * @since 1.6
 * 
 * Created: Dec 18, 2008
 */
public class GantEvent extends BuildEvent {

  public GantEvent(Project project, GantBinding binding) {
    super(project);
    this.binding = binding
  }

  public GantEvent(Target target, GantBinding binding) {
    super(target);
    this.binding = binding
  }

  public GantEvent(Task task, GantBinding binding) {
    super(task);
    this.binding = binding
  }

  GantBinding binding
}