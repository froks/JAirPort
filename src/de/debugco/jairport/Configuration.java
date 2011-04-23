package de.debugco.jairport;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Random;

public class Configuration {
  private static byte[] hwAddr;

  public static String getHostName() {
    try {
      InetAddress local = InetAddress.getLocalHost();
      return local.getHostName();
    } catch (UnknownHostException e) {
      return "unknown";
    }
  }

  public static int getPort() {
    return 5000;
  }

  public static byte[] getHardwareAddress() {
    if (hwAddr == null || hwAddr.length == 0) {
      // MAC couldn't be determined
      try {
        InetAddress local = InetAddress.getLocalHost();
        NetworkInterface ni = NetworkInterface.getByInetAddress(local);
        if (ni != null) {
          hwAddr = ni.getHardwareAddress();
          return hwAddr;
        }
      } catch (Exception e) {
        // ignore
      }

      Random rand = new Random();
      byte[] mac = new byte[8];
      rand.nextBytes(mac);
      mac[0] = 0x00;
      hwAddr = mac;
    }
    return hwAddr;
  }
}
