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

package gant.tools.tests

import org.codehaus.gant.tests.GantTestCase

/**
 *  A test to ensure that the LaTeX tool is not broken.
 *
 *  @author Russel Winder <russel@russel.org.uk>
 *  @version $Revision$ $Date$
 */
final class LaTeX_Tests extends GantTestCase {
  def optionTestGantFile ( name , key ) { """
includeTool << gant.tools.LaTeX
target ( add${name}Option : "" ) {
  LaTeX.add${name}Option ( "-blah" )
  println ( LaTeX.defaultEnvironment[ "${key}Options" ] )
}
"""
  }
  def optionListTestGantFile ( name , key ) { """
includeTool << gant.tools.LaTeX
target ( add${name}OptionList : "" ) {
  LaTeX.add${name}Option ( [ "-blah" , "--flobadob" ] )
  println ( LaTeX.defaultEnvironment[ "${key}Options" ] )
}
"""
  }
  void testAddLaTeXOption ( ) {
    System.setIn ( new StringBufferInputStream ( optionTestGantFile ( 'LaTeX' , 'latex' ) ) )
    assertEquals ( 0 , gant.process ( [ '-f' , '-' , 'addLaTeXOption' ] as String[] ) )
    assertEquals ( '''["-blah"]
''' , output.toString ( ) ) 
  }
  void testAddLaTeXOptionList ( ) {
    System.setIn ( new StringBufferInputStream ( optionListTestGantFile ( 'LaTeX' , 'latex' ) ) )
    assertEquals ( 0 , gant.process ( [ '-f' , '-' , 'addLaTeXOptionList' ] as String[] ) )
    assertEquals ( '''["-blah", "--flobadob"]
''' , output.toString ( ) ) 
  }
  void testAddBibTeXOption ( ) {
    System.setIn ( new StringBufferInputStream ( optionTestGantFile ( 'BibTeX' , 'bibtex' ) ) )
    assertEquals ( 0 , gant.process ( [ '-f' , '-' , 'addBibTeXOption' ] as String[] ) )
    assertEquals ( '''["-blah"]
''' , output.toString ( ) ) 
  }
  void testAddBibTeXOptionList ( ) {
    System.setIn ( new StringBufferInputStream ( optionListTestGantFile ( 'BibTeX' , 'bibtex' ) ) )
    assertEquals ( 0 , gant.process ( [ '-f' , '-' , 'addBibTeXOptionList' ] as String[] ) )
    assertEquals ( '''["-blah", "--flobadob"]
''' , output.toString ( ) ) 
  }
  void testAddMakeindexOption ( ) {
    System.setIn ( new StringBufferInputStream ( optionTestGantFile ( 'Makeindex' , 'makeindex' ) ) )
    assertEquals ( 0 , gant.process ( [ '-f' , '-' , 'addMakeindexOption' ] as String[] ) )
    assertEquals ( '''["-blah"]
''' , output.toString ( ) ) 
  }
  void testAddMakeindexOptionList ( ) {
    System.setIn ( new StringBufferInputStream ( optionListTestGantFile ( 'Makeindex' , 'makeindex' ) ) )
    assertEquals ( 0 , gant.process ( [ '-f' , '-' , 'addMakeindexOptionList' ] as String[] ) )
    assertEquals ( '''["-blah", "--flobadob"]
''' , output.toString ( ) ) 
  }
  void testAddDvipsOption ( ) {
    System.setIn ( new StringBufferInputStream ( optionTestGantFile ( 'Dvips' , 'dvips' ) ) )
    assertEquals ( 0 , gant.process ( [ '-f' , '-' , 'addDvipsOption' ] as String[] ) )
    assertEquals ( '''["-blah"]
''' , output.toString ( ) ) 
  }
  void testAddDvipsOptionList ( ) {
    System.setIn ( new StringBufferInputStream ( optionListTestGantFile ( 'Dvips' , 'dvips' ) ) )
    assertEquals ( 0 , gant.process ( [ '-f' , '-' , 'addDvipsOptionList' ] as String[] ) )
    assertEquals ( '''["-blah", "--flobadob"]
''' , output.toString ( ) ) 
  }
  void testAddPs2pdfOption ( ) {
    System.setIn ( new StringBufferInputStream ( optionTestGantFile ( 'Ps2pdf' , 'ps2pdf' ) ) )
    assertEquals ( 0 , gant.process ( [ '-f' , '-' , 'addPs2pdfOption' ] as String[] ) )
    assertEquals ( '''["-blah"]
''' , output.toString ( ) ) 
  }
  void testAddPs2pdfOptionList ( ) {
    System.setIn ( new StringBufferInputStream ( optionListTestGantFile ( 'Ps2pdf' , 'ps2pdf' ) ) )
    assertEquals ( 0 , gant.process ( [ '-f' , '-' , 'addPs2pdfOptionList' ] as String[] ) )
    assertEquals ( '''["-blah", "--flobadob"]
''' , output.toString ( ) ) 
  }
}
