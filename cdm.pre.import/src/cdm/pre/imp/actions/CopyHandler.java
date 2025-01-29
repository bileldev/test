package cdm.pre.imp.actions;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.handlers.HandlerUtil;

import cdm.pre.imp.mod.TreeElement;

public abstract class CopyHandler extends org.eclipse.core.commands.AbstractHandler {

   public CopyHandler() {
   }

   abstract protected String getTextToCopy(final TreeElement elem);

   @Override
   public Object execute(ExecutionEvent event) throws ExecutionException {
      ISelection sel = HandlerUtil.getCurrentSelection(event);
      if (sel instanceof IStructuredSelection) {
         IStructuredSelection ssel = (IStructuredSelection) sel;
         if (ssel.getFirstElement() instanceof TreeElement) {
            TreeElement elem = (TreeElement) ssel.getFirstElement();
            Clipboard cb = new Clipboard(Display.getCurrent());
            cb.setContents(new Object[] { getTextToCopy(elem) }, new Transfer[] { TextTransfer.getInstance() });
         }
      }
      return null;
   }

}
