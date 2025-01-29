package cdm.pre.imp;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;

import cdm.pre.imp.app.Activator;
import cdm.pre.imp.mod.TreeElement;
import cdm.pre.imp.reader.IConstants;

public class TreeCellLabelProvider extends CellLabelProvider implements IStyledLabelProvider {

   public final static int          NEW_CLR      = SWT.COLOR_RED;
   public final static int          MOD_CLR      = SWT.COLOR_DARK_GREEN;
   public final static int          MISSING_CLR  = SWT.COLOR_DARK_MAGENTA;
   private Font                     font;
   private final Map<String, Image> images       = new HashMap<String, Image>();
   private final Image              treeImageGen = Activator.getImageDescriptor("/icons/protected_co.gif")
                                                       .createImage();

   public TreeCellLabelProvider() {
      JFaceResources.getColorRegistry().put(TreeElement.State.New.toString(),
            Display.getDefault().getSystemColor(NEW_CLR).getRGB());
      JFaceResources.getColorRegistry().put(TreeElement.State.Modified.toString(),
            Display.getDefault().getSystemColor(MOD_CLR).getRGB());
      JFaceResources.getColorRegistry().put(TreeElement.State.Missing.toString(),
            Display.getDefault().getSystemColor(MISSING_CLR).getRGB());
   }

   private Image getImage(final TreeElement treeElement) {
      String clazz = treeElement.getElement().getClazz();
      if (clazz == null) {
         return treeImageGen;
      }
      Map<String, String> userValues = treeElement.getElement().getUserValues();
      boolean invalid = "+".equals(userValues.get(IConstants.j0Invalid));
      if (IConstants.j0SDPos.equals(clazz)) {
         if ("+".equals(userValues.get(IConstants.j0SmaDia2GeoPos))) {
            clazz = "j0sdposm";
            if (userValues.get(IConstants.j0ZbuNomenclature) != null) {
               clazz = "j0sdpzm";
            } else if (invalid) {
               clazz = "j0sdps05";
            }
         } else if (userValues.get(IConstants.j0ZbuNomenclature) != null) {
            clazz = "j0sdps02";
         } else if ("+".equals(userValues.get(IConstants.j0Invalid))) {
            clazz = "j0sdps01";
         }
      } else if (IConstants.j0PrtVer.equals(clazz) && "j0EeFw".equals(userValues.get(IConstants.j0PartCategory))) {
         clazz = "j0prtfer";
      } else if (IConstants.j0SDSMod.equals(clazz) && invalid) {
         clazz = "j0sdsmu";
      } else if (IConstants.j0SDMod.equals(clazz) && invalid) {
         clazz = "j0sdmodu";
      } else if (IConstants.j0SDHMod.equals(clazz) && invalid) {
         clazz = "j0sdhmu";
      } else if (IConstants.j0CTMd2D.equals(clazz)) {
         clazz = IConstants.j0CTMod;
      // executes a special loop that resolves the icon based on the mapped to class of the Truck element
      } else if(IConstants.TRUCK_CLASS_BCS.equals(clazz)) {
    	  if(treeElement.getElement() != null && treeElement.getElement().getMappedElementsMap() != null
    			  && treeElement.getElement().getMappedElementsMap().keySet().size() == 1) {
    		  if(treeElement.getElement().getMappedElementsMap().containsKey(IConstants.TC_TYPE_TRUCK)) {
    			  clazz = IConstants.TC_TYPE_TRUCK;
    		  } 
    		  else if(treeElement.getElement().getMappedElementsMap().containsKey(IConstants.TC_TYPE_CBM)) {
    			  clazz = IConstants.TC_TYPE_CBM;
    		  }
    		  else if(treeElement.getElement().getMappedElementsMap().containsKey(IConstants.TC_TYPE_DBM)) {
    			  clazz = IConstants.TC_TYPE_DBM;
    		  }
    		  else if(treeElement.getElement().getMappedElementsMap().containsKey(IConstants.TC_TYPE_NAVMOD)) {
    			  clazz = IConstants.TC_TYPE_NAVMOD;
    		  }
    		  else if(treeElement.getElement().getMappedElementsMap().containsKey(IConstants.TC_TYPE_FUNCMOD)) {
    			  clazz = IConstants.TC_TYPE_FUNCMOD;
    		  }
    		  else if(treeElement.getElement().getMappedElementsMap().containsKey(IConstants.TC_TYPE_POS)) {
    			  clazz = IConstants.TC_TYPE_POS;
    		  }
    		  else if(treeElement.getElement().getMappedElementsMap().containsKey(IConstants.TC_TYPE_LIPOS)) {
    			  clazz = IConstants.TC_TYPE_LIPOS;
    		  }
    	  }
      }
      
      Image ret = images.get(clazz);
      if (ret == null) {
         String imageName = clazz.toLowerCase() + "_small.png";
         //System.out.println("Image Name : "+imageName);
         imageName = "/icons/obj/" + imageName;
        // System.out.println("Image Name : "+imageName);
      //   ImageDescriptor imgDesc = Activator.getImageDescriptor("/icons/obj/" + imageName);
         ImageDescriptor imgDesc = Activator.getImageDescriptor(imageName);
         if (imgDesc != null) {
            ret = imgDesc.createImage();
         } else {
            ret = treeImageGen;
         }
         images.put(clazz, ret);
      }
      return ret;
   }

   private Color getColor(final TreeElement treeElement) {
      TreeElement.State state = treeElement.getState();
      if (state != null) {
         int color = -1;
         switch (state) {
         case Missing:
            color = MISSING_CLR;
            break;
         case New:
            color = NEW_CLR;
            break;
         case Modified:
            color = MOD_CLR;
            break;
         }
         if (color != -1) {
            return Display.getCurrent().getSystemColor(color);
         }
      }
      return null;
   }

   protected static String getColumnText(final TreeElement treeElement, final int columnIndex) {
      String text = null;
      switch (columnIndex) {
      case 0:
         text = treeElement.getDisplayText();
         break;
      case 1:
         text = treeElement.getPartNumber();
         break;
      case 2:
         text = treeElement.getPartName();
         break;
      case 3:
         text = treeElement.getRevision();
         break;
      case 4:
         text = treeElement.getSequence();
         break;
      case 5:
         if (treeElement.getFolderDefinition() != null) {
            text = treeElement.getFolderDefinition();
         } else if (treeElement.getFolderType() != null) {
            text = treeElement.getFolderType().toString();
         } else {
            text = "";
         }
         break;
      case 6:
         int modType = treeElement.getModType();
         StringBuilder sb = new StringBuilder();
         if ((modType & TreeElement.CHANGE_NAME) != 0) {
            sb.append("BEN");
         }
         if ((modType & TreeElement.CHANGE_TRANS) != 0) {
            if (sb.length() > 0) {
               sb.append(",");
            }
            sb.append("TRANS");
         }
         if ((modType & TreeElement.CHANGE_VERSION) != 0) {
            if (sb.length() > 0) {
               sb.append(",");
            }
            sb.append("VERS");
         }
         if ((modType & TreeElement.CHANGE_OBID) != 0) {
            if (sb.length() > 0) {
               sb.append(",");
            }
            sb.append("OBID");
         }
         if ((modType & TreeElement.CHANGE_PROJECT) != 0) {
            if (sb.length() > 0) {
               sb.append(",");
            }
            sb.append("PROJ");
         }
         if ((modType & TreeElement.CHANGE_DYNDIA) != 0) {
            if (sb.length() > 0) {
               sb.append(",");
            }
            sb.append("DYNDIA");
         }
         if ((modType & TreeElement.CHANGE_LCS) != 0) {
            if (sb.length() > 0) {
               sb.append(",");
            }
            sb.append("LCS");
         }
         if ((modType & TreeElement.CHANGE_HNUMBER) != 0) {
            if (sb.length() > 0) {
               sb.append(",");
            }
            sb.append("HNUMBER");
         }
         text = sb.toString();
         break;
      case 7:
         String relCount = treeElement.getRelCount();
         text = relCount == null ? "" : relCount;
         break;
      case 8:
         text = treeElement.getClazz();
         break;
      case 9:
         text = treeElement.getElement().getId();
         break;
      case 10:
         text = treeElement.getProject();
         break;
      case 11:
         if (treeElement.getTransform() != null) {
            sb = new StringBuilder();
            double[] ms = treeElement.getTransform();
            for (int i = 0; i < ms.length; i++) {
               if (i > 0) {
                  sb.append(" ");
               }
               sb.append(ms[i]);
            }
            text = sb.toString();
         } else {
            text = "";
         }
      }
      if (text == null) {
         text = "";
      }
      return text;
   }

   @Override
   public void update(final ViewerCell cell) {
      int index = cell.getColumnIndex();
      TreeElement treeElement = (TreeElement) cell.getElement();
      String text = getColumnText(treeElement, index);
      // do column specific stuff
      switch (index) {
      case 0:
         cell.setImage(getImage(treeElement));
         break;
      default:
         break;
      }
      if (text != null) {
         cell.setText(text);
      }
      cell.setForeground(getColor(treeElement));
   }

   @Override
   public String getToolTipText(final Object element) {
      TreeElement treeElement = (TreeElement) element;
      String changes = treeElement.getModificationText();
      if (changes.length() > 0) {
         return changes;
      }
      return null;
   }

   @Override
   public Point getToolTipShift(final Object object) {
      return new Point(5, 5);
   }

   @Override
   public int getToolTipDisplayDelayTime(final Object object) {
      return 100;
   }

   @Override
   public Font getToolTipFont(final Object object) {
      if (font == null) {
         font = new Font(Display.getCurrent(), new FontData("Courier New", 8, SWT.NORMAL));
      }
      return font;
   }

   @Override
   public StyledString getStyledText(final Object element) {
      TreeElement treeElement = (TreeElement) element;
      TreeElement.State state = treeElement.getState();
      if (state == null) {
         return new StyledString(treeElement.getDisplayText());
      }
      return new StyledString(treeElement.getDisplayText(), StyledString.createColorRegistryStyler(state.toString(),
            null));
   }

   @Override
   public Image getImage(final Object element) {
      return getImage((TreeElement) element);
   }

}
