package cdm.pre.imp;

import java.util.List;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

import cdm.pre.imp.mod.TreeElement;

public class FilterSelectionListener implements SelectionListener, KeyListener {

   private final TreeViewer treeViewer;
   private TreeViewerFilter treeFilter;
   private Button[]         diffBtns;
   private Button           invalidBtn;
   private Text             text;

   public void setDiffBtns(final Button[] diffBtns) {
      this.diffBtns = diffBtns;
      for (Button btn : diffBtns) {
         btn.addSelectionListener(this);
      }
   }

   public void setInvalidBtn(final Button invalidBtn) {
      this.invalidBtn = invalidBtn;
      invalidBtn.addSelectionListener(this);
   }

   public void setText(final Text text) {
      this.text = text;
      text.addKeyListener(this);
   }

   public FilterSelectionListener(final TreeViewer treeViewer) {
      this.treeViewer = treeViewer;
   }

   @Override
   public void widgetSelected(final SelectionEvent e) {
      if (treeFilter != null) {
         treeViewer.removeFilter(treeFilter);
         @SuppressWarnings("unchecked")
         List<TreeElement> roots = (List<TreeElement>) treeViewer.getInput();
         if (roots != null) {
            for (TreeElement root : roots) {
               root.clearFilterMatch();
               PlatformUI.getWorkbench().getDecoratorManager().update("cdm.pre.imp.tree.elem.2");
            }
         }
      }
      // treeViewer.refresh(true);
      createFilter();
      if (!treeFilter.statesEmpty() || treeFilter.isInvalidSmaDia2() || !treeFilter.modEmpty()
            || ((text.getText() != null) && !"".equals(text.getText()))) {
         treeViewer.addFilter(treeFilter);
      } else {
         treeFilter = null;
      }
   }

   @Override
   public void widgetDefaultSelected(final SelectionEvent e) {

   }

   private void createFilter() {
      treeFilter = new TreeViewerFilter();
      if ((text.getText() != null) && !"".equals(text.getText())) {
         treeFilter.setPartNmb(text.getText());
      }

      if (diffBtns[0].getSelection()) {
         treeFilter.addState(TreeElement.State.New);
      }
      if (diffBtns[1].getSelection()) {
         treeFilter.addState(TreeElement.State.Missing);
      }
      if (diffBtns[2].getSelection()) {
         treeFilter.addState(TreeElement.State.Modified);
      }
      treeFilter.setInvalidSmaDia2(invalidBtn.getSelection());
   }

   @Override
   public void keyPressed(final KeyEvent e) {

   }

   @Override
   public void keyReleased(final KeyEvent e) {
      widgetSelected(null);
   }

}
