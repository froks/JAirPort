package de.debugco.jairport;

import org.jboss.netty.channel.*;
import org.jboss.netty.handler.codec.http.*;
import static org.jboss.netty.handler.codec.http.HttpHeaders.*;

import org.jboss.netty.handler.codec.rtsp.RtspMethods;
import org.jboss.netty.handler.codec.rtsp.RtspVersions;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RtspRequestHandler extends SimpleChannelUpstreamHandler {
  private static final String RSAAESKEY = "a=rsaaeskey:";
  private static final String AESIV = "a=aesiv:";
  private static final String FMTP = "a=fmtp:";

  private void setPorts(RaopSession session, String transport) {
    int controlPort = 0;
    int timingPort = 0;

    Pattern controlPortPattern = Pattern.compile(".*control_port=(\\d+).*");
    Matcher controlPortMatcher = controlPortPattern.matcher(transport);
    if(controlPortMatcher.matches()) {
      controlPort = Integer.parseInt(controlPortMatcher.group(1));
    }

    Pattern timingPortPattern = Pattern.compile(".*timing_port=(\\d+).*");
    Matcher timingPortMatcher = timingPortPattern.matcher(transport);
    if(timingPortMatcher.matches()) {
      timingPort = Integer.parseInt(timingPortMatcher.group(1));
    }

    if(controlPort == 0) {
      throw new RuntimeException("no control port");
    } else if(timingPort == 0) {
      throw new RuntimeException("no timing port");
    }

    session.setControlPort(controlPort);
    session.setTimingPort(timingPort);
  }

  @Override
  public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
    HttpRequest request = (HttpRequest) e.getMessage();
    System.out.println(request.toString());

    if(!RtspVersions.RTSP_1_0.equals(request.getProtocolVersion())) {
      HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR);
      response.addHeader("Connection", "close");
      e.getChannel().write(response);
      e.getChannel().disconnect();
      return;
    }

    HttpResponse response = new DefaultHttpResponse(RtspVersions.RTSP_1_0, HttpResponseStatus.OK);
    response.clearHeaders();

    String cSeq = request.getHeader("CSeq");
    if(cSeq != null) {
      response.addHeader("CSeq", cSeq);
    }

    response.addHeader("Audio-Jack-Status", "connected; type=analog");

    String challenge = request.getHeader("Apple-Challenge");
    if(challenge != null) {
      SocketAddress remoteAddress = e.getRemoteAddress();
      response.addHeader("Apple-Response", Utils.getChallengeResponse(challenge, ((InetSocketAddress) remoteAddress).getAddress(), Configuration.getHardwareAddress()));
    }

    String clientInstance = request.getHeader("Client-Instance");
    if (Utils.isNullOrEmpty(clientInstance)) {
      throw new RuntimeException("No Client Instance given");
    }

    HttpMethod method = request.getMethod();
    if(RtspMethods.OPTIONS.equals(method)) {
      response.addHeader("Public", "ANNOUNCE, SETUP, RECORD, PAUSE, FLUSH, TEARDOWN, OPTIONS, GET_PARAMETER, SET_PARAMETER");
    } else if(RtspMethods.ANNOUNCE.equals(method)) {
      String content = request.getContent().toString(Charset.forName("UTF-8"));
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      try {
        request.getContent().readBytes(out, request.getContent().readableBytes());
        BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(out.toByteArray())));
        String line;
        String rsaAesKey = null;
        String aesIv = null;
        String fmtp = null;
        // Get attributes of Service-Discovery-Protocol
        while ((line = reader.readLine()) != null) {
          if (line.startsWith(RSAAESKEY)) {
            rsaAesKey = line.substring(RSAAESKEY.length());
          } else if (line.startsWith(AESIV)) {
            aesIv = line.substring(AESIV.length());
          } else if (line.startsWith(FMTP)) {
            fmtp = line.substring(FMTP.length());
          }
        }

        if(Utils.isNullOrEmpty(aesIv)) {
          throw new RuntimeException("No AES Iv");
        }

        if(Utils.isNullOrEmpty(rsaAesKey)) {
          throw new RuntimeException("No RSA AES Key");
        }

        RaopSession session = RaopSessionManager.getSession(clientInstance);
        if(session == null) {
          session = new RaopSession(clientInstance, Utils.decodeBase64(aesIv), Utils.decryptBase64(rsaAesKey), fmtp);
          RaopSessionManager.addSession(clientInstance, session);
        } else {
          session.setAesIv(Utils.decodeBase64(aesIv));
          session.setAesKey(Utils.decryptBase64(rsaAesKey));
          session.setFmtp(fmtp);
        }
      } finally {
        out.close();
      }
    } else if(RtspMethods.SETUP.equals(method)) {
      RaopSession session = RaopSessionManager.getSession(clientInstance);
      if(session == null) {
        throw new RuntimeException("No Session " + clientInstance);
      }
      String transport = request.getHeader("Transport");
      setPorts(session, transport);
      RaopServer server = new RaopServer(session);
      server.setDaemon(true);
      server.start();
      session.setServer(server);

      response.setHeader("Transport", request.getHeader("Transport") + ";server_port=" + server.getPort());
      response.setHeader("Session", request.getHeader("Client-Instance"));
    } else if(RtspMethods.RECORD.equals(method)) {
      // ignore
    } else if("FLUSH".equalsIgnoreCase(method.getName())) {
      System.out.println("FLUSH");
    } else if(RtspMethods.TEARDOWN.equals(method)) {
      response.setHeader("Connection", "close");
      e.getChannel().write(response);
      RaopSessionManager.shutdownSession(clientInstance);
      e.getChannel().disconnect();
      return;
    } else if(RtspMethods.SET_PARAMETER.equals(method)) {
      // TODO set volume
    } else if(RtspMethods.GET_PARAMETER.equals(method)) {
      // ignore
    } else if("DENIED".equalsIgnoreCase(method.getName())) {
      // ignore
    } else {
      throw new RuntimeException("Unknown Method: " + method.getName());
    }

    boolean keepAlive = isKeepAlive(request);
    if(keepAlive) {
      response.setHeader("Content-Length", response.getContent().readableBytes());
    }

    ChannelFuture future = e.getChannel().write(response);

    if(!keepAlive) {
      future.addListener(ChannelFutureListener.CLOSE);
    }
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
    if(e.getCause() != null) {
      e.getCause().printStackTrace();
    }

    e.getChannel().close();
  }

}
