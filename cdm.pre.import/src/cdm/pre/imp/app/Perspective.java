package cdm.pre.imp.app;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

import cdm.pre.imp.StructView;


public class Perspective implements IPerspectiveFactory 
{
   @Override
   public void createInitialLayout(final IPageLayout layout) 
   {
      layout.setEditorAreaVisible(false);
      layout.setFixed(true);
      layout.addView(StructView.ID, IPageLayout.LEFT, 1.f, layout.getEditorArea());
      layout.getViewLayout(StructView.ID).setCloseable(false);
      layout.getViewLayout(StructView.ID).setMoveable(false);
   }
}
