//   Gant — A Groovy way of scripting Ant tasks.
//
//  Copyright © 2006–2008, 2010, 2013, 2014  Russel Winder
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
//
//  Author:  Russel Winder <russel@winder.org.uk>

package example;

import org.apache.commons.lang.WordUtils;

public class Hello {
  public static void main(final String[] args) {
    String  message = "hello ivy!";
    System.out.println("Standard message: " + message);
    System.out.println("Capitalized by " + WordUtils.class.getName()  + ": " + WordUtils.capitalizeFully(message));
  }
}
