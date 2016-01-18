import java.awt.datatransfer.*;
import java.io.*;

import javax.swing.*;

public class PassVault {
  public static String sName = "PassVault";
  public static String sVersion = "0.6";

  private static String sPath = ".passvault";

  private static PassVaultPrefs sPreferences;
  private static PassVaultModel sModel;

  public static void main(String... args) throws IOException {
    System.out.println(sName + " " + sVersion);

    String directoryPath = null;
    if (args.length > 0)
      directoryPath = args[0];

    initialize(directoryPath);
  }

  public static void initialize(String directoryPath) throws IOException {
    String separator = System.getProperty("file.separator");

    if (directoryPath == null) {
      String home = System.getProperty("user.home");

      // Create directory if necessary.
      directoryPath = home + separator + sPath;
    }

    File directory = new File(directoryPath);
    directory.mkdirs();

    // Load preferences.
    String prefsPath = directoryPath + separator + "preferences";
    sPreferences = PassVaultPrefs.load(prefsPath);

    // Prompt for passphrase.
    // fixme: How to get focus on the password field?
    JPasswordField pf = new JPasswordField();
    pf.addAncestorListener(new RequestFocusListener());
    int r = JOptionPane.showConfirmDialog(
        null,
        pf,
        "Enter your passphrase",
        JOptionPane.OK_CANCEL_OPTION,
        JOptionPane.QUESTION_MESSAGE);
    if (r != JOptionPane.OK_OPTION) return;
    String passphrase = new String (pf.getPassword()); // I know, I know.

    /*
    String passphrase = JOptionPane.showInputDialog(
        null,
        "Enter your passphrase",
        sName + " " + sVersion,
        JOptionPane.QUESTION_MESSAGE
        );
    if (passphrase == null) return;
    */

    // Retrieve salt and iv.
    byte[] salt = sPreferences.getByteArray("salt");
    byte[] iv = sPreferences.getByteArray("iv");
    if (salt == null) {
      System.out.print("Generating salt...");
      java.security.SecureRandom sr = new java.security.SecureRandom();
      salt = new byte[8];
      sr.nextBytes(salt);
      System.out.println("done.");
      System.out.print("Generating iv...");
      iv = new byte[16];
      sr.nextBytes(iv);
      System.out.println("done.");
      sPreferences.set("salt", salt);
      sPreferences.set("iv", iv);
    }

    // Load password vault.
    String modelPath = sPreferences.get("modelPath");
    if (modelPath == null)
      modelPath = directoryPath + separator + "passvault";
    System.out.println("modelPath = " + modelPath);
    sModel = PassVaultModel.load(passphrase, salt, iv, modelPath);

    PassVaultFrame f = new PassVaultFrame(sPreferences, sModel);
    f.addWindowListener(new java.awt.event.WindowAdapter() {
        public void windowClosing(java.awt.event.WindowEvent e) {
          try { shutdown(); }
          catch (IOException ioe) {
            System.out.println("Shutdown failed: " + ioe);
          }
        }
    });
  }

  public static void shutdown() throws IOException {
    // Clear out the clipboard.
    Clipboard clipboard = java.awt.Toolkit.getDefaultToolkit().getSystemClipboard();
    clipboard.setContents(new Transferable() {
      public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[0];
      }

      public boolean isDataFlavorSupported(DataFlavor flavor) {
        return false;
      }

      public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
        throw new UnsupportedFlavorException(flavor);
      }
    }, new StringSelection("fake"));

    sPreferences.save();
    sModel.save();
  }
}
