/**
 * 
 */
package cdm.pre.imp.configmap;

/**
 * @author amit.rath
 *
 */
public class UncheckedMappingException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	
	/**
	 * constructor of the class that invokes the parent constructor by passing the exception text as a parameter
	 * @param message - exception message as thrown by the calling method
	 */
	public UncheckedMappingException (String message) {
		super(message);
	}

	@Override
	public void printStackTrace() {
		// TODO Auto-generated method stub
		super.printStackTrace();
		System.exit(999);
	}
}
