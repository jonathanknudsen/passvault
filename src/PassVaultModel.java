import java.io.*;
import java.security.*;
import java.security.spec.*;
import java.util.*;
import javax.crypto.*;
import javax.crypto.spec.*;

public class PassVaultModel {
  private String mPassphrase;
  private byte[] mSalt;
  private byte[] mIV;
  private String mPath;
  private Properties mProperties;
  
  private PassVaultModel(String passphrase, byte[] salt, byte[] iv, String path, Properties properties) {
    mPassphrase = passphrase;
    mSalt = salt;
    mIV = iv;
    mPath = path;
    mProperties = properties;
  }
  
  public void set(String key, String value) {
    if (key == null || value == null)
      throw new IllegalArgumentException();
    
    mProperties.setProperty(key, value);
  }
  
  public String get(String key) {
    return mProperties.getProperty(key);
  }
  
  // Returns keys that contain keyword filter. Case-insensitive.
  public String[] search(String filter) {
    Set<String> keySet = mProperties.stringPropertyNames();
    String[] keys = keySet.toArray(new String[0]);
    
    List<String> filteredKeys = new ArrayList<String>();
    
    for (int i = 0; i < keys.length; i++) {
      if (keys[i].toLowerCase().contains(filter.toLowerCase()) == true)
        filteredKeys.add(keys[i]);
    }
    
    String[] filteredArray = filteredKeys.toArray(new String[0]);
    
    return filteredArray;
  }
  
  public void remove(String key) {
    mProperties.remove(key);
  }
  
  /*
  public void savePlain() throws IOException {
    OutputStream out = new FileOutputStream(mPath);
    mProperties.store(out, "PassVaultModel");
  }
  */
  
  public void save() throws IOException {
    set("_good", "yes");
    
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    mProperties.store(baos, "PassVaultModel");
    byte[] plaintext = baos.toByteArray();
    
    // Encrypt.
    byte[] ciphertext = encrypt(mPassphrase, mSalt, mIV, plaintext);
    
    // Save.
    OutputStream out = new FileOutputStream(mPath);
    out.write(ciphertext);
    out.close();
  }
  
  /*
  public static PassVaultModel loadPlain(String path) {
    Properties properties = new Properties();
    
    File f = new File(path);
    if (f.exists() == true) {
      try { properties.load(new FileInputStream(f)); }
      catch (IOException ioe) {}
    }
    
    return new PassVaultModel(null, null, null, path, properties);
  }
  */

  public static PassVaultModel load(String passphrase, byte[] salt, byte[] iv, String path) {
    Properties properties = new Properties();
    
    File f = new File(path);
    if (f.exists() == true) {
      try {
        // Read ciphertext.
        InputStream rawIn = new FileInputStream(f);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int c;
        while ((c = rawIn.read()) >= 0)
          baos.write((byte)c);
        rawIn.close();
        byte[] ciphertext = baos.toByteArray();
        
        // Decrypt.
        byte[] plaintext = decrypt(passphrase, salt, iv, ciphertext);
        
        // Read properties.
        ByteArrayInputStream in = new ByteArrayInputStream(plaintext);
        properties.load(in);
        
        // Abort if garbage.
        String value = properties.getProperty("_good");
        if (value == null || value.equals("yes") == false)
          throw new IOException("Bad decryption key.");
        properties.remove("_good");
      }
      catch (IOException ioe) {}
      catch (Throwable t) {
          javax.swing.JOptionPane.showMessageDialog(null, "load(): " + t);
      }
    }
    
    return new PassVaultModel(passphrase, salt, iv, path, properties);
  }
  
  private static byte[] encrypt(String passphrase, byte[] salt, byte[] iv, byte[] plaintext) {
    byte[] ciphertext = null;
    
    try {
      // Construct the key from the passphrase and the salt.
      SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
      KeySpec spec = new PBEKeySpec(passphrase.toCharArray(), salt, 65536, 256);
      SecretKey tmp = factory.generateSecret(spec);
      SecretKey secret = new SecretKeySpec(tmp.getEncoded(), "AES");
      // Encrypt.
      Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
      cipher.init(Cipher.ENCRYPT_MODE, secret, new IvParameterSpec(iv));
      ciphertext = cipher.doFinal(plaintext);
    }
    catch (Exception e) {
      // Very poor form, I know.
      System.out.println("Bad bad bad: " + e);
    }
    return ciphertext;
  }
  
  private static byte[] decrypt(String passphrase, byte[] salt, byte[] iv,
      byte[] ciphertext) {
    byte[] plaintext = null;
    
    try {
      // Construct the key from the passphrase and the salt.
      SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
      KeySpec spec = new PBEKeySpec(passphrase.toCharArray(), salt, 65536, 256);
      SecretKey tmp = factory.generateSecret(spec);
      SecretKey secret = new SecretKeySpec(tmp.getEncoded(), "AES");
      // Decrypt.
      Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
      cipher.init(Cipher.DECRYPT_MODE, secret, new IvParameterSpec(iv));
      plaintext = cipher.doFinal(ciphertext);
    }
    catch (Exception e) {
      // Very poor form, I know.
      System.out.println("Bad bad bad: " + e);
    }
    return plaintext;
  }
}

