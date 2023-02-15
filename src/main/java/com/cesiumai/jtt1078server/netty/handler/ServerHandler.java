package com.cesiumai.jtt1078server.netty.handler;

import com.cesiumai.jtt1078server.netty.ffmpeg.H264StreamHub;
import com.cesiumai.jtt1078server.netty.ffmpeg.H264ToStreamService;
import com.cesiumai.jtt1078server.netty.stream.StreamFrame;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * @author wuxiongbin
 */
@Slf4j
public class ServerHandler extends SimpleChannelInboundHandler<DataFrame> {
    private String strId = "unsetted";
    private int packageSize = 0;
    private final ByteBuf byteBuf = Unpooled.buffer();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DataFrame dataFrame) {
        //log.info("接收到视频流数据：\n {}", dataFrame);
        String tag = dataFrame.getSIMCardNumber() + "-" + dataFrame.getLogicalChannelNumber();
        //log.info("tag : {}", tag);
        //Map<StreamFrameSink, StreamFrameSink> map = StreamHub.GetStream(tag);
        H264ToStreamService h264ToStreamInstance = H264StreamHub.getH264ToStreamInstance(tag);
        if(null==h264ToStreamInstance){
            throw new RuntimeException("未初始化ffmpeg推流实例");
        }
        int subMark = dataFrame.getSubcontractingTreatmentMark();
        if (subMark == 0b0000) {
            ByteBuf buf = Unpooled.buffer();
            buf.writeInt(dataFrame.getDataBody().length);
            buf.writeBytes(dataFrame.getDataBody());
            StreamFrame frame = new StreamFrame(buf);
            //log.info("接收到视频的独立数据帧");
            packageSize = 0;
            frame.dwTime = dataFrame.getTimeStamp();
            frame.bIsKey = dataFrame.getDataType() == 0;
            //StreamHub.WriteFrame(map, frame);
            h264ToStreamInstance.writeH264Data(dataFrame.getDataBody());
        } else if (subMark == 0b0001) {
            byteBuf.clear();
            //log.info("接收到视频数据帧的第一个包");
            byteBuf.writeBytes(dataFrame.getDataBody());
            packageSize++;
            h264ToStreamInstance.writeH264Data(dataFrame.getDataBody());
        } else if (subMark == 0b0010) {
            packageSize++;
            //log.info("接收到视频数据帧的最后一个包，该数据帧共有：{} 个包", packageSize);
            byteBuf.writeBytes(dataFrame.getDataBody());
            ByteBuf buf = Unpooled.buffer();
            buf.writeInt(byteBuf.readableBytes());
            buf.writeBytes(byteBuf);
            StreamFrame frame = new StreamFrame(buf);
            frame.dwTime = dataFrame.getTimeStamp();
            frame.bIsKey = dataFrame.getDataType() == 0;
            //StreamHub.WriteFrame(map, frame);
            h264ToStreamInstance.writeH264Data(dataFrame.getDataBody());
            if (packageSize != dataFrame.getPackageNo()) {
                log.error("出现了丢包，共接收到数据包：{}，最后一个包的包序号为：{}", packageSize, dataFrame.getPackageNo());
            }
        } else if (subMark == 0b0011) {
            packageSize++;
            //log.info("接收到视频数据帧的中间包");
            byteBuf.writeBytes(dataFrame.getDataBody());
            h264ToStreamInstance.writeH264Data(dataFrame.getDataBody());
        }
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        super.channelWritabilityChanged(ctx);
        log.info("{}: writeable change : {}", strId, ctx.channel().isWritable());
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        strId = ctx.channel().id().asShortText();
        super.channelActive(ctx);
        log.info("{}: 新设备接入 {}", strId, Thread.currentThread().getName());
        log.info("{} {}", strId, ctx.channel().config().getWriteBufferWaterMark().toString());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        log.info("{}:i am dead", strId);
        ctx.channel().close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.info("exception caught");
        cause.printStackTrace();
        ctx.channel().close();
    }
}
