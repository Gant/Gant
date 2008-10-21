package gant

/**
 *  Generic Gant exception.
 */
class GantException extends RuntimeException {
  public GantException ( ) { super( ) }
  public GantException ( String msg ) { super ( msg ) }
  public GantException ( Exception e ) { super ( e ) }
  public GantException ( String msg , Exception e ) { super ( msg, e ) }
}
