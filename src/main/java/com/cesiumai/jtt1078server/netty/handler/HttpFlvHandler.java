package com.cesiumai.jtt1078server.netty.handler;

import com.cesiumai.jtt1078server.netty.stream.StreamFrame;
import com.cesiumai.jtt1078server.netty.stream.StreamFrameSink;
import com.cesiumai.jtt1078server.netty.stream.StreamHub;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.internal.StringUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * @author wuxiongbin
 */
@Slf4j
public class HttpFlvHandler extends SimpleChannelInboundHandler<FullHttpRequest> implements StreamFrameSink {
    protected String tag = "";
    protected Channel chn;

    @Override
    public void CloseThisClient() {
        if (this.chn.isActive()) {
            this.chn.close();
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest o) throws Exception {
        if (!o.decoderResult().isSuccess()) {
            sendError(ctx, HttpResponseStatus.BAD_REQUEST);
            return;
        }
        if (o.method() != HttpMethod.GET) {
            sendError(ctx, HttpResponseStatus.METHOD_NOT_ALLOWED);
            return;
        }
        QueryStringDecoder uri = new QueryStringDecoder(o.uri());
        log.info(uri.path());
        if (!uri.path().equals("/live/flv") || !uri.parameters().containsKey("tag")) {
            sendError(ctx, HttpResponseStatus.BAD_REQUEST);
            return;
        }
        tag = uri.parameters().get("tag").get(0);
        if (StringUtil.isNullOrEmpty(tag)) {
            sendError(ctx, HttpResponseStatus.BAD_REQUEST);
            return;
        }
        HttpResponse rsp = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        rsp.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE)
                .set(HttpHeaderNames.CONTENT_TYPE, "video/x-flv")
                .set(HttpHeaderNames.TRANSFER_ENCODING, HttpHeaderValues.CHUNKED);
        ctx.writeAndFlush(rsp);
        StreamHub.EnterStream(tag, this);
        log.info("{} enter stream {} from http\n", chn.id(), tag);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        chn = ctx.channel();
        log.info("{} new connection {}\n", chn.id(), Thread.currentThread().getName());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        if (!StringUtil.isNullOrEmpty(tag)) {
            // from stream hub clear this info
            log.info("{} will leave stream {} \n", chn.id(), tag);
            StreamHub.LeaveStream(tag, this);
            tag = "";
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.channel().close();
    }

    private static void sendError(ChannelHandlerContext ctx, HttpResponseStatus status) {
        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1, status, Unpooled.copiedBuffer("Failure: " + status + "\r\n", CharsetUtil.UTF_8));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    public boolean WriteFrame(StreamFrame frame) {
        if (this.chn.isActive() && this.chn.isWritable()) {
            ReferenceCountUtil.retain(frame);
            chn.writeAndFlush(frame);
            return true;
        }
        return false;
    }
}