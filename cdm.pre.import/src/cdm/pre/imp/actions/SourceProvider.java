package cdm.pre.imp.actions;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.ui.AbstractSourceProvider;
import org.eclipse.ui.ISources;

/**
 * This SourceProvider manages the properties which are used to enable or
 * disable certain UI widgets.
 * 
 * @author wikeim
 * 
 */
public class SourceProvider extends AbstractSourceProvider {
   public final static String S1             = "cdm.pre.import.showS1";
   public final static String S2             = "cdm.pre.import.showS2";
   public final static String R1             = "cdm.pre.import.root1.active";
   public final static String R2             = "cdm.pre.import.root2.active";

   public final static String DELTA          = "cdm.pre.import.delta.active";
   public final static String EXPVAL         = "cdm.pre.import.delta.expval";

   public final static String ALTHIER_ACTIVE = "cdm.pre.import.althier.active";
   public final static String ALTHIER_SET    = "cdm.pre.import.althier.set";

   private boolean            showS1;
   private boolean            showS2;
   private boolean            showDelta;
   private boolean            altHierActive;
   private boolean            altHierSet;
   private boolean            deltaExpVal;
   private boolean            root1Active;
   private boolean            root2Active;

   public SourceProvider() {
   }

   @Override
   public void dispose() {
   }

   @SuppressWarnings({ "rawtypes", "unchecked" })
   @Override
   public Map getCurrentState() {
      Map ret = new HashMap();
      ret.put(S1, showS1);
      ret.put(S2, showS2);
      ret.put(R1, root1Active);
      ret.put(R2, root2Active);
      ret.put(DELTA, showDelta);
      ret.put(EXPVAL, deltaExpVal);
      ret.put(ALTHIER_ACTIVE, altHierActive);
      ret.put(ALTHIER_SET, altHierSet);
      return ret;
   }

   @Override
   public String[] getProvidedSourceNames() {
      return new String[] { S1, S2, R1, R2, DELTA, EXPVAL, ALTHIER_ACTIVE, ALTHIER_SET };
   }

   /**
    * Enable the show PLMXML 1 buttons.
    * 
    * @param enabled
    *           <code>true</code> enable the buttons.
    */
   public void enableS1(final boolean enabled) {
      showS1 = enabled;
      fireSourceChanged(ISources.WORKBENCH, S1, showS1);
   }

   /**
    * Enable the show PLMXML 2 buttons.
    * 
    * @param enabled
    *           <code>true</code> enable the buttons.
    */
   public void enableS2(final boolean enabled) {
      showS2 = enabled;
      fireSourceChanged(ISources.WORKBENCH, S2, showS2);
   }

   /**
    * Activate the reload PLMXML 1 button.
    * 
    * @param active
    *           <code>true</code> activate the button.
    */
   public void setR1(final boolean active) {
      root1Active = active;
      fireSourceChanged(ISources.WORKBENCH, R1, root1Active);
   }

   /**
    * Activate the reload PLMXML 2 button.
    * 
    * @param active
    *           <code>true</code> activate the button.
    */
   public void setR2(final boolean active) {
      root2Active = active;
      fireSourceChanged(ISources.WORKBENCH, R2, root2Active);
   }

   /**
    * Set the delta mode.
    * 
    * @param pDelta
    *           <code>true</code> if delta mode.
    */
   public void setDelta(final boolean pDelta) {
      showDelta = pDelta;
      fireSourceChanged(ISources.WORKBENCH, DELTA, showDelta);
   }

   /**
    * Enables the export Import XML file buttons.
    * 
    * @param pValid
    *           <code>true</code> if the button can be enabled.
    */
   public void setDeltaExpVal(final boolean pValid) {
      deltaExpVal = pValid;
      fireSourceChanged(ISources.WORKBENCH, EXPVAL, deltaExpVal);
   }

   /**
    * Set the alternative hierarchy mode.
    * 
    * @param pAltHier
    *           <code>true</code> if alternative hierarchy mode.
    */
   public void enableAltHier(final boolean pAltHier) {
      altHierActive = pAltHier;
      fireSourceChanged(ISources.WORKBENCH, ALTHIER_ACTIVE, altHierActive);
   }

   public void setAltHier(final boolean pAltHier) {
      altHierSet = pAltHier;
      fireSourceChanged(ISources.WORKBENCH, ALTHIER_SET, altHierSet);
   }
}
