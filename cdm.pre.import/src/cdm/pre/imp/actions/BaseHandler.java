package cdm.pre.imp.actions;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.ui.handlers.HandlerUtil;

import cdm.pre.imp.StructView;
import cdm.pre.imp.*;

public abstract class BaseHandler extends org.eclipse.core.commands.AbstractHandler {

   public BaseHandler() {
   }
cdm.pre.imp.FilterSelectionListener.
   protected StructView getStructView(ExecutionEvent event) {
      return (StructView) HandlerUtil.getActivePart(event);
   }
}
