import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

public class PassVaultFrame extends JFrame {
  private PassVaultPrefs mPrefs;
  private PassVaultModel mModel;
  
  private JTextField mFilterField;
  private JButton mAddButton, mRemoveButton;
  private JTable mTable;
  private ListSelectionModel mSelectionModel;
  
  public PassVaultFrame(PassVaultPrefs prefs, PassVaultModel model) {
    super(PassVault.sName + " " + PassVault.sVersion);
    
    mPrefs = prefs;
    mModel = model;
    
    createContent();
    
    // Set bounds from preferences, or center.
    int w = mPrefs.getInt("windowWidth", 400);
    int h = mPrefs.getInt("windowHeight", 400);
    Dimension ss = Toolkit.getDefaultToolkit().getScreenSize();
    int x = mPrefs.getInt("windowX", ss.width / 2 - w / 2);
    int y = mPrefs.getInt("windowY", ss.height / 2 - h / 2);
    
    setBounds(x, y, w, h);
    
    // Listen for bounds changes and save to preferences.
    addComponentListener(new ComponentAdapter() {
        public void componentResized(ComponentEvent ce) {
          mPrefs.set("windowWidth", getWidth());
          mPrefs.set("windowHeight", getHeight());
        }
        
        public void componentMoved(ComponentEvent ce) {
          mPrefs.set("windowX", getX());
          mPrefs.set("windowY", getY());
        }
    });
        
    
    setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    setVisible(true);
  }
  
  private void createContent() {
    mFilterField = new JTextField();
    mAddButton = new JButton("+");
    mRemoveButton = new JButton("-");
    mRemoveButton.setEnabled(false);
    
    Box top = Box.createHorizontalBox();
    top.add(mFilterField);
    top.add(mAddButton);
    top.add(mRemoveButton);
    
    // Each time the filter is updated, repaint the table.
    mFilterField.getDocument().addDocumentListener(new DocumentListener() {
        public void changedUpdate(DocumentEvent de) { doFilter(); }
        public void insertUpdate(DocumentEvent de) { doFilter(); }
        public void removeUpdate(DocumentEvent de) { doFilter(); }
    });
    
    // Hook up the table to our model.
    mTable = new JTable(new AbstractTableModel() {
        public int getRowCount() {
          String filter = mFilterField.getText();
          String[] keys = mModel.search(filter);
          return keys.length;
        }
        
        public int getColumnCount() {
          return 2;
        }
        
        public Object getValueAt(int row, int column) {
          String filter = mFilterField.getText();
          String[] keys = mModel.search(filter);
          if (column == 0)
            return keys[row];
          if (column == 1)
            return mModel.get(keys[row]);
          
          return "[bogus]";
        }
    });
    mTable.setTableHeader(null);
    
    // Respond to table selections.
    mSelectionModel = mTable.getSelectionModel();
    mSelectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    
    mSelectionModel.addListSelectionListener(
        new ListSelectionListener() {
          public void valueChanged(ListSelectionEvent lse) {
            mRemoveButton.setEnabled(!mSelectionModel.isSelectionEmpty());
            int s = mSelectionModel.getMinSelectionIndex();
            if (s != -1) {
              // Copy selected password to clipboard.
              String value = (String)mTable.getModel().getValueAt(s, 1);
              StringSelection ss = new StringSelection(value);
              Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
              clipboard.setContents(ss, ss);
            }
          }
        }
    );
    
    // Add a row.
    mAddButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ae) {
          //JTextField keyField = new JTextField();
          JTextField keyField = new JTextField() {
              public void addNotify() {
                  super.addNotify();
                  requestFocus();
              }
          };
          JTextField passwordField = new JTextField();
          
          JPanel p = new JPanel();
          p.setLayout(new GridLayout(2, 2));
          p.add(new JLabel("Name:"));
          p.add(keyField);
          p.add(new JLabel("Password:"));
          p.add(passwordField);
          keyField.requestFocus();
          
          String[] options = {"OK", "Cancel"};
          int r = JOptionPane.showOptionDialog(
            PassVaultFrame.this,
            p,
            "Enter a password",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            null);
          
          if (r == JOptionPane.OK_OPTION) {
            String key = keyField.getText();
            String password = passwordField.getText();
            if (key.length() > 0 && password.length() > 0) {
              mModel.set(key, password);
              mTable.repaint();
            }
          }
        }
    });
    
    // Remove a row.
    mRemoveButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ae) {
           int s = mSelectionModel.getMinSelectionIndex();
           String key = (String)mTable.getModel().getValueAt(s, 0);
           mModel.remove(key);
           mTable.repaint();
        }
    });
    
    // Final assembly.
    setLayout(new BorderLayout());
    add(top, BorderLayout.NORTH);
    JScrollPane sp = new JScrollPane(mTable);
    add(sp, BorderLayout.CENTER);
  }
  
  private void doFilter() {
    if (mTable.getModel().getRowCount() == 1)
      mTable.setRowSelectionInterval(0, 0);
    else
      mTable.getSelectionModel().clearSelection();
    mTable.repaint();
  }
}

