package cdm.pre.imp;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;

import cdm.pre.imp.mod.TreeElement;

public class TreeElementComp extends ViewerComparator {
   private int columnIndex;
   private int sortDir = 0;

   public TreeElementComp() {
   }

   public void setSortDir(final int sortDir) {
      this.sortDir = sortDir;
   }

   public void setColumnIndex(final int columnIndex) {
      this.columnIndex = columnIndex;
   }

   @Override
   public int compare(Viewer viewer, Object e1, Object e2) {
      int ret = 0;
      int colIndx = columnIndex;
      int sortValue = sortDir;
      if (sortDir == 0) {
         // sort by display text as default
         colIndx = 0;
         sortValue = 1;
      }
      String text1 = TreeCellLabelProvider.getColumnText((TreeElement) e1, colIndx);
      String text2 = TreeCellLabelProvider.getColumnText((TreeElement) e2, colIndx);
      if (text1 == null && text2 != null) {
         ret = -1;
      } else if (text1 != null && text2 == null) {
         ret = 1;
      } else if (text1 != null) {
         ret = text1.compareTo(text2);
      }
      return ret * sortValue;
   }
}
