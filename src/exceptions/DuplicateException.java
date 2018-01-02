/*
 * 	@author Alessandra Fais
 */
package exceptions;

/** <i>DuplicateException</i>: eccezione unchecked.
 * 	La scelta deriva dal fatto che il controllo sulla presenza di un utente non è dispendioso
 * 	su una struttura dati come una hash map; infatti ciò può essere fatto mediante chiamata 
 * 	al metodo IsUser in tempo costante.
 * 
 *  @author Alessandra Fais
 */
public class DuplicateException extends RuntimeException {
	
	private static final long serialVersionUID = 1L;

	public DuplicateException() {
		super();
	}
	
	public DuplicateException(String s) {
		super(s);
	}
	
}
