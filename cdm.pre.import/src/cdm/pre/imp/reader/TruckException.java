package cdm.pre.imp.reader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cdm.pre.imp.configmap.ConfigMapUtils;

/**
 * exception class to handle exeptions thrown from the Truck specific implementation
 * @author amit.rath
 *
 */
public class TruckException extends Exception {

	
	private static final long serialVersionUID = 1L;
	   
	   private boolean forceExit;
	   
	   private String exceptionType;
	   
	   private final static Logger logger = LogManager.getLogger("cdm.pre.imp.tracelog");
	   
	   
	   public TruckException(String message) {
	      super(message);
	      this.forceExit = false;
	      this.exceptionType = null;
	   }
		   
		   
	   public TruckException (String message, boolean forceExit, String exceptionType) {
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
				System.exit(899);
			}
	   }
}
