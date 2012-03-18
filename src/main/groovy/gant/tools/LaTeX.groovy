//  Gant -- A Groovy way of scripting Ant tasks.
//
//  Copyright © 2007-8,2010 Russel Winder
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

package gant.tools

import org.codehaus.gant.GantBinding

/**
 *  Provide support for supporting LaTeX document processing.
 *
 *  @author Russel Winder <russel@winder.org.uk>
 */
class LaTeX {
  public final ltxExtension = '.ltx'
  public final texExtension = '.tex'
  public final dviExtension = '.dvi'
  public final epsExtension = '.eps'
  public final pdfExtension = '.pdf'
  public final psExtension = '.ps'
  public final auxExtension = '.aux'
  public final bblExtension = '.bbl'
  public final blgExtension = '.blg'
  public final idxExtension = '.idx'
  public final ilgExtension = '.ilg'
  public final indExtension = '.ind'
  public final logExtension = '.log'
  public final tocExtension = '.toc'
  public final pdfBookMarkExtension = '.out'
  public intermediateExtensions = [
    auxExtension , dviExtension , logExtension , tocExtension ,
    bblExtension , blgExtension ,
    idxExtension , ilgExtension , indExtension ,
    pdfBookMarkExtension
    ]
  public final environment = [
    latexCommand : 'pdflatex' ,
    latexOptions : [ '-interaction=nonstopmode' , '-halt-on-error' ] ,
    bibtexCommand : 'bibtex' ,
    bibtexOptions : [ ] ,
    makeindexCommand : 'makeindex' ,
    makeindexOptions : [ ] ,
    dvipsCommand : 'dvips' ,
    dvipsOptions : [ ] ,
    ps2pdfCommand : 'ps2pdf' ,
    ps2pdfOptions : [ ] ,
    root : '' ,
    dependents : [ ]
    ]
  protected final GantBinding binding
  protected final Execute executor
  /**
   *  Constructor for the "includeTool <<" usage.
   *
   *  @param binding The <code>GantBinding</code> to bind to.
   */
  public LaTeX ( final GantBinding binding ) {
    this.binding = binding
    executor = new Execute ( binding )
  }
 /**
   *  Constructor for the "includeTool **" usage.
   *
   *  @param binding The <code>GantBinding</code> to bind to.
   *  @param map The <code>Map</code> of initialization parameters.
   */
  public LaTeX ( final GantBinding binding , final Map<String,String> map ) {
    this.binding = binding
    executor = new Execute ( binding )
    map.each { key , value -> addOption ( key , value ) }
  }
  private void addOption ( key , option ) {
    if ( option instanceof List ) { environment[ key ] += option }
    else { environment[ key ] << option }
  }
  /**
   *  Add a LaTeX option for the build.
   *
   *  @param option The option to add.
   */
  public void addLaTeXOption ( option ) { addOption ( 'latexOptions' , option ) }
  /**
   *  Add a BibTeX option for the build.
   *
   *  @param option The option to add.
   */
  public void addBibTeXOption ( option ) { addOption ( 'bibtexOptions' , option ) }
  /**
   *  Add a Makeindex option for the build.
   *
   *  @param option The option to add.
   */
  public void addMakeindexOption ( option ) { addOption ( 'makeindexOptions' , option ) }
  /**
   *  Add a DviPS option for the build.
   *
   *  @param option The option to add.
   */
  public void addDvipsOption ( option ) { addOption ( 'dvipsOptions' , option ) }
  /**
   *  Add a Ps2Pdf option for the build.
   *
   *  @param option The option to add.
   */
  public void addPs2pdfOption ( option ) { addOption ( 'ps2pdfOptions' , option ) }
  /**
   *  Add a collection of options for the build.
   *
   *  @param keywordOptions The collection of options to add.
   */
  public void addOptions ( Map<String,String> keywordOptions ) { keywordOptions.each { key , value -> addOption ( key , value ) } }
  /**
   *  Perform the LaTeX source compilation.  The source will be processed enough times to ensure that BibTeX
   *  and Makeindex requirements are completed.
   */
  private void executeLaTeX ( ) {
    String root = (String) environment.root
    def sourceName = root + ltxExtension
    def sourceFile = new File ( sourceName )
    if ( ! sourceFile.exists ( ) ) {
      sourceFile = new File ( sourceName = root + texExtension )
      if ( ! sourceFile.exists ( ) ) { throw new FileNotFoundException ( "Neither ${root}.ltx or ${root}.tex exist." ) }
    }
    def targetExtension = environment.latexCommand == 'pdflatex' ? pdfExtension : dviExtension
    def targetName = root + targetExtension
    def targetFile = new File ( targetName )
    def needToUpdate = false
    if ( targetFile.exists ( ) ) {
      ( environment.dependents + [ sourceName ] ).each { dependent ->
        if ( ! ( dependent instanceof File ) ) { dependent = new File ( (String) dependent ) }
        if ( dependent.lastModified ( ) > targetFile.lastModified ( ) ) { needToUpdate = true }
      }
    }
    else { needToUpdate = true }
    if ( needToUpdate ) {
      def latexAction = [ environment.latexCommand , *environment.latexOptions , sourceName ]
      def runLaTeX = { executor.executable ( latexAction ) }
      def conditionallyRunLaTeX = {
        def rerun = new File ( root + logExtension ).text =~ /(Warning:.*Rerun|Warning:.*undefined citations)/
        if ( rerun ) { runLaTeX ( ) }
        rerun
      }
      runLaTeX ( )
      def currentDirectory = new File ( '.' )
      def bibTeXRun = false
      currentDirectory.eachFileMatch ( ~/.*.aux/ ) { auxFile ->
        if ( auxFile.text =~ 'bibdata' ) {
          executor.executable ( [ environment.bibtexCommand , *environment.bibtexOptions , auxFile.name ] )
          bibTeXRun = true
        }
      }
      if ( bibTeXRun ) {
        runLaTeX ( )
        if ( conditionallyRunLaTeX ( ) ) { conditionallyRunLaTeX ( ) }
      }
      def makeindexRun = false
      currentDirectory.eachFileMatch ( ~/.*.idx/ ) { idxFIle ->
        executor.executable ( [ environment.bibtexCommand , *environment.bibtexOptions , idxFile.name ] )
        makeindexRun = true
      }
      if ( makeindexRun ) { runLaTeX ( ) }
      runLaTeX ( )
      if ( conditionallyRunLaTeX ( ) ) {
        if ( ! conditionallyRunLaTeX ( ) ) {
          throw new RuntimeException ( '#### Something SERIOUSLY Wrong. ###' )
        }
      }
    }
  }
  /**
   *  Create a PDF file from a LaTeX source.
   *
   *  @param arguments a <code>Map</code> of options to add to the build environment.
   */
  public void generatePDF ( Map<String,String> arguments ) {
    arguments.each { key , value -> environment[ key ] = value }
    environment.latexCommand = 'pdflatex'
    executeLaTeX ( )
  }
  /**
   *   Create a PostScript file from a LaTeX source.
   *
   *  @param arguments a <code>Map</code> of options to add to the build environment.
   */
  public void generatePS ( Map<String,String> arguments ) {
    arguments.each { key , value -> environment[ key ] = value }
    environment.latexCommand = 'latex'
    executeLaTeX ( )
    def dviFile = new File ( (String) environment.root + dviExtension )
    def psFile = new File ( (String) environment.root + psExtension )
    if ( ( ! psFile.exists ( ) ) || ( dviFile.lastModified ( ) > psFile.lastModified ( ) ) ) {
      executor.executable ( [ environment.dvipsCommand , * environment.dvipsOptions , '-o' , psFile.name , dviFile.name ] )
    }
  }
}
