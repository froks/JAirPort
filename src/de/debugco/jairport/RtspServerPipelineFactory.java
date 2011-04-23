package de.debugco.jairport;

import static org.jboss.netty.channel.Channels.*;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.handler.codec.rtsp.RtspRequestDecoder;
import org.jboss.netty.handler.codec.rtsp.RtspResponseEncoder;

public class RtspServerPipelineFactory implements ChannelPipelineFactory {
  @Override
  public ChannelPipeline getPipeline() throws Exception {
    ChannelPipeline pipeline = pipeline();

    pipeline.addLast("decoder", new RtspRequestDecoder());
    pipeline.addLast("encoder", new RtspResponseEncoder());
    pipeline.addLast("handler", new RtspRequestHandler());
    return pipeline;
  }
}
