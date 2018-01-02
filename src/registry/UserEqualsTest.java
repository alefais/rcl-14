/*
 * @author Alessandra Fais
 */
package registry;

/**<i>class UserEqualsTest</i>:
 * definita per testare il corretto funzionamento del metodo equals
 * ridefinito nella classe user.
 * 
 * @author Alessandra Fais
 */
public class UserEqualsTest {

	public static void main(String[] args) {
		User u1 = new User("pippo", "ONLINE", 0, "a", "a", null, 0);
		User u2 = new User("pippo", "ONLINE", 0, "a", "a", null, 0);
		User u3 = new User("pluto", "ONLINE", 0, "a", "a", null, 0);
		
		if (u1.equals(u2)) System.out.println("1 GIUSTO!");
		else System.out.println("1 NO!");
		
		if (!u1.equals(u3)) System.out.println("2 GIUSTO!");
		else System.out.println("2 NO!");
		
		if (!u1.equals("minni")) System.out.println("3 GIUSTO!");
		else System.out.println("3 NO!");
		
		if (!u1.equals(new Integer(2))) System.out.println("4 GIUSTO!");
		else System.out.println("4 NO!");
	}

}
