package cdm.pre.imp.actions;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.handlers.HandlerUtil;

public class AltHierHandler extends BaseHandler {

   @Override
   public Object execute(ExecutionEvent event) throws ExecutionException {
      boolean oldValue = HandlerUtil.toggleCommandState(event.getCommand());
      getStructView(event).toggleAltHier(oldValue);
      return null;
   }

}
