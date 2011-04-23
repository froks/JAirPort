package de.debugco.jairport;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.net.InetAddress;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;

public class Utils {
  public static String byteAddrToString(byte[] addr) {
    StringBuilder sb = new StringBuilder();
    for (byte b : addr) {
      sb.append(String.format("%02x", b));
    }
    return sb.toString();
  }

  public static byte[] getByteArrayFromStream(InputStream is) throws IOException {
    byte[] b = new byte[10000];
    int read;
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    while((read = is.read(b, 0, b.length)) > 0) {
      out.write(b, 0, read);
    }

    return out.toByteArray();
  }

  public static byte[] readFile(String filename) throws IOException {
    FileInputStream fis = new FileInputStream(filename);
    try {
      return getByteArrayFromStream(fis);
    } finally {
      fis.close();
    }
  }

  public static String getChallengeResponse(String challengeStr, InetAddress address, byte[] hwAddress) {
    try {
      byte[] challenge = decodeBase64(challengeStr);
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      // Challenge
      out.write(challenge);
      // IP-Address
      out.write(address.getAddress());
      // HW-Addr
      out.write(hwAddress);
      // Pad to 32 Bytes
      int padLen = 32 - out.size();
      for(int i = 0; i < padLen; ++i) {
        out.write(0x00);
      }

      String response = encodeBase64(encrypt(out.toByteArray()));
      return response.replace("=", "").replace("\r", "").replace("\n", ""); // remove padding and other chars
    } catch(Exception e) {
      throw new RuntimeException(e);
    }
  }

  private static Key getKey() {
    try {
      InputStream fis = ClassLoader.getSystemResourceAsStream("key.pk8");
      if(fis == null) {
        throw new RuntimeException("Cannot find keyfile");
      }
      try {
        // openssl pkcs8 -inform pem -outform der -topk8 -nocrypt -in key.pem -out key.pk8
        return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(getByteArrayFromStream(fis)));
      } finally {
        fis.close();
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private static Cipher getRsaCipher(String padding) throws NoSuchAlgorithmException, NoSuchPaddingException, NoSuchProviderException {
    return Cipher.getInstance("RSA/ECB/" + padding);
  }

  public static String encodeBase64(byte[] data) {
    return new BASE64Encoder().encode(data);
  }

  public static byte[] decodeBase64(String base64Data) {
    try {
      return new BASE64Decoder().decodeBuffer(base64Pad(base64Data));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static byte[] decryptBase64(String base64encodedData) {
    return decrypt(decodeBase64(base64encodedData));
  }

  public static byte[] decrypt(byte[] data) {
    try {
      Cipher c = getRsaCipher("OAEPWITHSHA1ANDMGF1PADDING");
      c.init(Cipher.DECRYPT_MODE, getKey());

      return c.doFinal(data);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static byte[] encrypt(byte[] data) {
    try {
      Cipher c = getRsaCipher("PKCS1PADDING");
      c.init(Cipher.ENCRYPT_MODE, getKey());
      return c.doFinal(data);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Pads a given String s to be usable for BASE64Decoder (if it isn't padded the resulting deoceded data may be wrong)
   * @param s
   * @return
   */
  public static String base64Pad(String s) {
    int toPad = s.length() % 4;
    for(int i = 0; i < toPad; ++i) {
      s = s + "=";
    }

    return s;
  }

  public static boolean isNullOrEmpty(String client) {
    return client == null || "".equals(client.trim());
  }
}
