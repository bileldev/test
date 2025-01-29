package cdm.pre.imp.actions;

import org.eclipse.core.commands.ExecutionEvent;

public class S1Handler extends BaseHandler {

   public S1Handler() {
   }

   @Override
   public Object execute(ExecutionEvent event) throws org.eclipse.core.commands.ExecutionException {
      getStructView(event).showPLMXML1();
      return null;
   }

}
