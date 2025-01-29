package cdm.pre.imp.log;

public class ConsoleHandler extends java.util.logging.ConsoleHandler {

   public ConsoleHandler() {
      setOutputStream(System.out);
   }

}
