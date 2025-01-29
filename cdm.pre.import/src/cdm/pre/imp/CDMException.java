package cdm.pre.imp;

public class CDMException extends Exception {

   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   public CDMException(String message) {
      super(message);
   }

   public CDMException(String message, Throwable cause) {
      super(message, cause);
   }
}
