package cdm.pre.imp.actions;

import org.eclipse.core.commands.ExecutionEvent;

public class S2Handler extends BaseHandler {

   public S2Handler() {
   }

   @Override
   public Object execute(ExecutionEvent event) throws org.eclipse.core.commands.ExecutionException {
      getStructView(event).showPLMXML2();
      return null;
   }

}
