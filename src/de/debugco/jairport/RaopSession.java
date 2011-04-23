package de.debugco.jairport;

public class RaopSession {
  private String id;
  private byte[] aesKey;
  private byte[] aesIv;
  private AudioFormat format;
  private int timingPort;
  private int controlPort;
  private RaopServer server;

  public RaopSession(String id, byte[] aesIv, byte[] aesKey, String fmt) {
    this.id = id;
    this.aesIv = aesIv;
    this.aesKey = aesKey;
    setFmtp(fmt);
  }

  public String getId() {
    return id;
  }

  public int getTimingPort() {
    return timingPort;
  }

  public int getControlPort() {
    return controlPort;
  }

  public void setAesIv(byte[] aesIv) {
    this.aesIv = aesIv;
  }

  public void setAesKey(byte[] aesKey) {
    this.aesKey = aesKey;
  }

  public byte[] getAesKey() {
    return aesKey;
  }

  public byte[] getAesIv() {
    return aesIv;
  }

  public AudioFormat getFormat() {
    return format;
  }

  public void setFmtp(String val) {
    String[] fmt = val.split(" ");
    format = new AudioFormat();
    format.setFrameSize(Integer.parseInt(fmt[1]));
    format.setU7a(Integer.parseInt(fmt[2]));
    format.setSampleSize(Integer.parseInt(fmt[3]));
    format.setRiceHistoryMult(Integer.parseInt(fmt[4]));
    format.setRiceInitialHistory(Integer.parseInt(fmt[5]));
    format.setRiceKModifier(Integer.parseInt(fmt[6]));
    format.setU7f(Integer.parseInt(fmt[7]));
    format.setU80(Integer.parseInt(fmt[8]));
    format.setU82(Integer.parseInt(fmt[9]));
    format.setU83(Integer.parseInt(fmt[10]));
    format.setSampleRate(Integer.parseInt(fmt[11]));
  }

  public void setControlPort(int controlPort) {
    this.controlPort = controlPort;
  }

  public void setTimingPort(int timingPort) {
    this.timingPort = timingPort;
  }

  public void setServer(RaopServer server) {
    this.server = server;
  }

  public RaopServer getServer() {
    return server;
  }

  public static class AudioFormat {
    // a=fmtp:96 352 0 16 40 10 14 2 255 0 0 44100
    //   unk=96, frameSize=352, 7a=0, sample_size=16, riceHistoryMult=40, riceInitialHistory=10, riceKModifier=14, 7f=2 (channels?), 80=255, 82=0, 83=0, sampleRate=44100
    private int frameSize;
    private int u7a;
    private int sampleSize;
    private int riceHistoryMult;
    private int riceInitialHistory;
    private int riceKModifier;
    private int u7f;
    private int u80;
    private int u82;
    private int u83;
    private int sampleRate;

    public int getFrameSize() {
      return frameSize;
    }

    public void setFrameSize(int frameSize) {
      this.frameSize = frameSize;
    }

    public int getU7a() {
      return u7a;
    }

    public void setU7a(int u7a) {
      this.u7a = u7a;
    }

    public int getSampleSize() {
      return sampleSize;
    }

    public void setSampleSize(int sampleSize) {
      this.sampleSize = sampleSize;
    }

    public int getRiceHistoryMult() {
      return riceHistoryMult;
    }

    public void setRiceHistoryMult(int riceHistoryMult) {
      this.riceHistoryMult = riceHistoryMult;
    }

    public int getRiceInitialHistory() {
      return riceInitialHistory;
    }

    public void setRiceInitialHistory(int riceInitialHistory) {
      this.riceInitialHistory = riceInitialHistory;
    }

    public int getRiceKModifier() {
      return riceKModifier;
    }

    public void setRiceKModifier(int riceKModifier) {
      this.riceKModifier = riceKModifier;
    }

    public int getU7f() {
      return u7f;
    }

    public void setU7f(int u7f) {
      this.u7f = u7f;
    }

    public int getU80() {
      return u80;
    }

    public void setU80(int u80) {
      this.u80 = u80;
    }

    public int getU82() {
      return u82;
    }

    public void setU82(int u82) {
      this.u82 = u82;
    }

    public int getU83() {
      return u83;
    }

    public void setU83(int u83) {
      this.u83 = u83;
    }

    public int getSampleRate() {
      return sampleRate;
    }

    public void setSampleRate(int sampleRate) {
      this.sampleRate = sampleRate;
    }
  }
}
