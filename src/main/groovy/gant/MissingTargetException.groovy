package gant

/**
 *  Throw when an undefined target is invoked.
 */
class MissingTargetException extends GantException {
  MissingTargetException ( ) { super ( ) }
  MissingTargetException ( String msg ) { super ( msg ) }
  MissingTargetException ( Exception e ) { super ( e ) }
  MissingTargetException ( String msg , Exception e ) { super ( msg , e ) }
}
