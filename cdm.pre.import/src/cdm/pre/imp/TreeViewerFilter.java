package cdm.pre.imp;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import cdm.pre.imp.mod.TreeElement;
import cdm.pre.imp.reader.IConstants;

public class TreeViewerFilter extends ViewerFilter {
   private final Set<TreeElement.State> states = new HashSet<TreeElement.State>();
   private int                          modType;
   private String                       partNmb;
   private boolean                      invalidSmaDia2;

   public void addState(final TreeElement.State state) {
      states.add(state);
   }

   public void setPartNmb(final String partNmb) {
      this.partNmb = partNmb;
   }

   public void setInvalidSmaDia2(final boolean invalidSmaDia2) {
      this.invalidSmaDia2 = invalidSmaDia2;
   }

   public boolean isInvalidSmaDia2() {
      return invalidSmaDia2;
   }

   public void addModType(final int modType) {
      this.modType |= modType;
   }

   public boolean modEmpty() {
      return this.modType == 0;
   }

   public boolean statesEmpty() {
      return states.isEmpty();
   }

   Pattern pattern;

   private boolean matchLine(final String text, final String partNmb, final String dispText) {
      if (pattern == null) {
         StringBuffer sb = new StringBuffer();
         final int len = text.length();
         for (int i = 0; i < len; i++) {
            char c = text.charAt(i);

            if (c == '(' || c == ')' || c == '.' || c == '[' || c == ']' || c == '\\' || c == '^' || c == '$') {
               sb.append('\\');
            }
            sb.append(c);
         }
         pattern = Pattern.compile(".*" + sb.toString() + ".*");
      }
      Matcher m1 = null;
      if (partNmb != null) {
         m1 = pattern.matcher(partNmb);
      }
      Matcher m2 = pattern.matcher(dispText);
      return m1 != null && m1.matches() || m2.matches();
   }

   private boolean checkState(final TreeElement treeElement) {
      boolean ret = false;

      boolean isValid = false;
      if (!states.isEmpty()) {
         isValid |= states.contains(treeElement.getState());
      }

      if (modType != 0) {
         isValid |= (modType & treeElement.getModType()) != 0;
      }

      if (partNmb != null) {
         isValid |= matchLine(partNmb, treeElement.getPartNumber(), treeElement.getDisplayText());
      }

      if (invalidSmaDia2) {
         if (treeElement.getElement().getUserValues() != null) {
            isValid = "+".equals(treeElement.getElement().getUserValues().get(IConstants.j0Invalid));
         }
      }
      if (!isValid) {
         for (TreeElement child : treeElement.getChilds()) {
            ret = checkState(child);
            if (ret) {
               break;
            }
         }
      } else {
         ret = true;
      }
      treeElement.setFilterMatch(isValid);
      return ret;
   }

   @Override
   public boolean select(final Viewer viewer, final Object parentElement, final Object element) {
      TreeElement treeElement = (TreeElement) element;
      return checkState(treeElement);
   }

}
