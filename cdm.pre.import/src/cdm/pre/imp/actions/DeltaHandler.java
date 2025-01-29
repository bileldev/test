package cdm.pre.imp.actions;

import org.eclipse.core.commands.ExecutionEvent;

public class DeltaHandler extends BaseHandler {

   public DeltaHandler() {
   }

   @Override
   public Object execute(ExecutionEvent event) throws org.eclipse.core.commands.ExecutionException {
      getStructView(event).compareRoots();
      return null;
   }

}
