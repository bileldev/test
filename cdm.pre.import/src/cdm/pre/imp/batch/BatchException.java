package cdm.pre.imp.batch;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cdm.pre.imp.CDMException;
import cdm.pre.imp.configmap.ConfigMapUtils;

public class BatchException extends CDMException {

   private static final long serialVersionUID = 1L;
   
   private boolean forceExit;
   
   private String exceptionType;
   
   private final static Logger logger = LogManager.getLogger("cdm.pre.imp.tracelog");

   public BatchException(String message) {
      super(message);
      this.forceExit = false;
      this.exceptionType = null;
   }
   
   
   public BatchException (String message, boolean forceExit, String exceptionType) {
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
