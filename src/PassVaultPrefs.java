import java.io.*;
import java.text.*;
import java.util.*;

public class PassVaultPrefs {
  private String mPath;
  private Properties mProperties;
  
  private PassVaultPrefs(String path, Properties properties) {
    mPath = path;
    mProperties = properties;
  }
  
  public void set(String key, String value) {
    if (key == null || value == null)
      throw new IllegalArgumentException();
    
    mProperties.setProperty(key, value);
  }
  
  public void set(String key, int value) {
    if (key == null)
      throw new IllegalArgumentException();
    
    NumberFormat nf = NumberFormat.getInstance();
    mProperties.setProperty(key, nf.format(value));
  }
  
  public void set(String key, byte[] value) {
    if (key == null)
      throw new IllegalArgumentException();

    StringBuffer hexBuffer = new StringBuffer();
    for (int i = 0; i < value.length; i++) {
      int v = (int)value[i] & 0xff;
      String hexv = Integer.toHexString(v);
      if (hexv.length() == 1)
        hexv = "0" + hexv; // Oy.
      hexBuffer.append(hexv);
    }
    String valueString = hexBuffer.toString();
    mProperties.setProperty(key, valueString);
  }
  
  public String get(String key) {
    return mProperties.getProperty(key);
  }
  
  public int getInt(String key, int defaultValue) {
    int intValue = defaultValue;
    String value = mProperties.getProperty(key);
    if (value != null) {
      NumberFormat nf = NumberFormat.getInstance();
      try { intValue = nf.parse(value).intValue(); }
      catch (ParseException pe) {}
    }
    return intValue;
  }
  
  public byte[] getByteArray(String key) {
    String hexValue = mProperties.getProperty(key);
    if (hexValue == null) return null;
    byte[] value = new byte[hexValue.length() / 2];
    for (int i = 0; i < value.length; i++) {
      String hexv = hexValue.substring(i * 2, i * 2 + 2);
      int v = Integer.parseInt(hexv, 16);
      value[i] = (byte)(v & 0xff);
    }
    return value;
  }
  
  public void save() throws IOException {
    OutputStream out = new FileOutputStream(mPath);
    mProperties.store(out, "PassVaultPrefs");
  }
  
  public static PassVaultPrefs load(String path) {
    Properties properties = new Properties();
    
    File f = new File(path);
    if (f.exists() == true) {
      try { properties.load(new FileInputStream(f)); }
      catch (IOException ioe) {}
    }
    
    return new PassVaultPrefs(path, properties);
  }
}
