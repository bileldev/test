package cdm.pre.imp.sendto.base;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class SendToFrame extends JFrame {
   private static final long serialVersionUID = 1L;
   private JPanel            contentPanel;
   private JTextArea         messageArea;
   private ImageIcon         logoImage;
   private JPanel            headerFrame;

   protected JPanel getHeaderFrame() {
      return headerFrame;
   }

   protected JPanel getContentPanel() {
      return contentPanel;
   }

   protected JTextArea getMessageArea() {
      return messageArea;
   }

   public SendToFrame(final String title, final String logoPath) {
      super(title);
      contentPanel = new JPanel();
      messageArea = new JTextArea();
      logoImage = new ImageIcon(getClass().getClassLoader().getResource(logoPath));
      setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
      contentPanel.setLayout(new GridBagLayout());
      setContentPane(contentPanel);
      this.setAutoRequestFocus(true);
      headerFrame = new JPanel();
      headerFrame.setLayout(new GridBagLayout());
      GridBagConstraints c = new GridBagConstraints();
      c.fill = GridBagConstraints.HORIZONTAL;
      c.insets = new Insets(0, 0, 0, 0);
      c.gridx = 0;
      c.gridy = 0;
      c.weightx = 0.0;
      c.weighty = 0.0;
      c.anchor = GridBagConstraints.NORTHWEST;
      int iconHeight = logoImage.getIconHeight();
      headerFrame.add(new JLabel(logoImage), c);
      JScrollPane scrollPane = new JScrollPane(messageArea);
      scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
      messageArea.setBackground(Color.LIGHT_GRAY);
      messageArea.setRows(4);
      messageArea.setLineWrap(true);
      messageArea.setWrapStyleWord(true);
      messageArea.setEditable(false);
      scrollPane.setBorder(BorderFactory.createLineBorder(Color.ORANGE));
      scrollPane.setPreferredSize(new Dimension(0, iconHeight));
      c.fill = GridBagConstraints.HORIZONTAL;
      c.gridx = 1;
      c.gridy = 0;
      c.weightx = 1.0;
      c.weighty = 1.0;
      headerFrame.add(scrollPane, c);
   }

   protected void enableAll(final boolean enable) {
      enableAll(contentPanel, enable);
   }

   protected void enableAll(Container cont, final boolean enable) {
      for (Component comp : cont.getComponents()) {
         comp.setEnabled(enable);
         if (comp instanceof Container) {
            enableAll((Container) comp, enable);
         }
      }
   }

   protected void setErrorMessage(Throwable e) {
      StringWriter sw = new StringWriter();
      e.printStackTrace(new PrintWriter(sw));
      try {
         sw.close();
      } catch (IOException ignore) {
      }
      setErrorMessage(sw.toString());
   }

   protected void setErrorMessage(final String msg) {
      messageArea.setForeground(Color.RED);
      messageArea.setText(msg);
   }

   protected void setSingleMessage(final String msg) {
      messageArea.setText(msg);
      messageArea.setCaretPosition(messageArea.getDocument().getLength());
   }

   protected void setMessage(final String msg) {
      messageArea.setForeground(Color.BLACK);
      String msgNew = messageArea.getText();
      if (!msgNew.isEmpty()) {
         msgNew += '\n';
      }
      msgNew += msg;
      messageArea.setText(msgNew);
      messageArea.setCaretPosition(messageArea.getDocument().getLength());
   }

   public static void setLAF() {
      try {
         UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
      } catch (Exception ignore) {
         try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
         } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
         }
      }
   }
}
