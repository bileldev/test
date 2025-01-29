/**
 * 
 */
package cdm.pre.imp.configmap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * @author amit.rath
 *
 */
public class MappingException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	
	private final static Logger logger = LogManager.getLogger("cdm.pre.imp.tracelog");
	
	/**
	 * member variable that decides if the exception would lead to a termination of the pre-importer session
	 */
	private boolean forceExit = false;
	
	/**
	 * member variable that documents the type of the log message - info, warning or error
	 */
	private String exceptionType = null;
	
	/**
	 * Parameterized overridden constructor
	 * @param message	: Exception message that will be printed
	 * @param forceExit	: boolean variable that decides if the session should be terminated or not
	 */
	public MappingException (String message, boolean forceExit, String exceptionType) {
		super(message);
		this.forceExit = forceExit;
		this.exceptionType	= exceptionType;
	}

	@Override
	public void printStackTrace() {
		// call to logger to print out exception message to log file
		if(this.exceptionType.equals(ConfigMapUtils.LOG_TYPE_INFO)) {
			logger.info(this.getMessage());
		}
		else if(this.exceptionType.equals(ConfigMapUtils.LOG_TYPE_WARNING)) {
			logger.warn(this.getMessage());
		}
		else if(this.exceptionType.equals(ConfigMapUtils.LOG_TYPE_ERROR)) {
			logger.error(this.getMessage());
		}
		else if(this.exceptionType.equals(ConfigMapUtils.LOG_TYPE_OBID_ERROR)) {
			logger.error(this.getMessage());
		}
		// ends the pre importer session in case of a critical error
		if(this.forceExit) {
			/* Display display = Display.getDefault();//new Display();
			  Shell shell = new Shell(display);
			MessageBox messageBox = new MessageBox(shell, SWT.ICON_WARNING | SWT.ABORT);
	        
	        messageBox.setText("Warning");
	        messageBox.setMessage("Pre-importer Exiting. PLMXML is does not have required information.. Do you want to continue ?");
	        int buttonID = messageBox.open();
	        switch(buttonID) {
	          case SWT.YES:
	            // saves changes ...
	          {
	        	  
	          }
	          case SWT.NO:
	            // exits here ...
	            break;
	          case SWT.CANCEL:
	            // does nothing ...
	        }*/
			// error handling for missing OBID
	        if(this.exceptionType.equals(ConfigMapUtils.LOG_TYPE_OBID_ERROR)) {
  				//System.exit(999);
  			}
  			else {
  				//System.exit(899);
  			}
		}
	}
}
