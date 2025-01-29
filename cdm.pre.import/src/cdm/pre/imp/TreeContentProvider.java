package cdm.pre.imp;

import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import cdm.pre.imp.mod.TreeElement;

public class TreeContentProvider implements ITreeContentProvider {

   List<TreeElement>    roots;
   private IViewOptions viewOptions;

   public TreeContentProvider(IViewOptions viewOptions) {
      this.viewOptions = viewOptions;
   }

   private List<TreeElement> getChildren(final TreeElement element) {
      if (viewOptions.isAltHierarchyMode()) {
         return element.getAltChilds();
      }
      return element.getChilds();
   }

   @Override
   public void dispose() {
   }

   @SuppressWarnings("unchecked")
   @Override
   public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
      this.roots = (List<TreeElement>) newInput;
   }

   @Override
   public Object[] getElements(Object inputElement) {
      @SuppressWarnings("unchecked")
      List<TreeElement> elems = (List<TreeElement>) inputElement;
      return elems.toArray();
   }

   @Override
   public Object[] getChildren(Object parentElement) {
      TreeElement elem = (TreeElement) parentElement;
      return getChildren(elem).toArray();
   }

   @Override
   public Object getParent(Object element) {
      return null;
   }

   @Override
   public boolean hasChildren(Object element) {
      TreeElement elem = (TreeElement) element;
      return !getChildren(elem).isEmpty();
   }

}
