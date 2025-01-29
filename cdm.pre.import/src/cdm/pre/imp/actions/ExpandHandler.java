package cdm.pre.imp.actions;

import org.eclipse.core.commands.ExecutionEvent;

public class ExpandHandler extends BaseHandler {

   public ExpandHandler() {
   }

   @Override
   public Object execute(ExecutionEvent event) throws org.eclipse.core.commands.ExecutionException {
      getStructView(event).getTreeViewer().expandAll();
      return null;
   }

}
