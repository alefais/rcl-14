/*
 * 	@author Alessandra Fais
 */
package exceptions;

/** <i>PermissionException</i>: eccezione unchecked.
 * 	La scelta deriva dal fatto che il controllo sullo stato di un utente non è dispendioso
 *	poichè corrisponde a un accesso a una struttura dati di tipo hash map e ciò ha tempo costante.
 *
 *  @author Alessandra Fais
 */
public class PermissionException extends RuntimeException {
	
	private static final long serialVersionUID = 1L;

	public PermissionException() {
		super();
	}
	
	public PermissionException(String s) {
		super(s);
	}
	
}
