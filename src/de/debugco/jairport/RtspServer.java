package de.debugco.jairport;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class RtspServer implements Runnable {
  private int port;

  public RtspServer(int port) {
    this.port = port;
  }

  @Override
  public void run() {
    System.out.println("Listening on Port " + port);
    ServerBootstrap bootstrap = new ServerBootstrap(
                                        new NioServerSocketChannelFactory(
                                            Executors.newCachedThreadPool(),
                                            Executors.newCachedThreadPool()
                                        )
                                    );
    bootstrap.setPipelineFactory(new RtspServerPipelineFactory());
    bootstrap.bind(new InetSocketAddress(port));
    while(!Thread.interrupted()) {
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        // ignore
      }
    }

    System.out.println("RTSP-Server shutdown");
  }
}
