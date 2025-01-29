package cdm.pre.imp.actions;

import org.eclipse.core.commands.ExecutionEvent;

public class CollapseHandler extends BaseHandler {

   public CollapseHandler() {
   }

   @Override
   public Object execute(ExecutionEvent event) throws org.eclipse.core.commands.ExecutionException {
      getStructView(event).getTreeViewer().collapseAll();
      return null;
   }
}
