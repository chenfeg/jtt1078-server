package com.cesiumai.jtt1078server.netty.handler;

import io.netty.buffer.ByteBuf;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author wuxiongbin
 */
@Slf4j
@Data
public class DataFrame {

    public static final Logger logger = LoggerFactory.getLogger(DataFrame.class);
    private int V;
    private int P;
    private int X;
    private int CC;
    private int M;
    private int PT;
    private int packageNo;
    private String SIMCardNumber;
    private int logicalChannelNumber;
    private int dataType;
    private int subcontractingTreatmentMark;
    private long timeStamp;
    private short lastIFrameInterval;
    private short lastFrameInterval;
    private short dataBodyLength;
    private byte[] dataBody;

    public static DataFrame parse(ByteBuf byteBuf) {
        if ((byteBuf.readInt() & 0x7fffffff) != 0x30316364) {
            log.error("拆包数据帧头标识不匹配，直接丢弃");
            return null;
        }
        int lengthOffsetPositionIndex = 28;
        DataFrame frame = new DataFrame();
        byte b4 = byteBuf.readByte();
        frame.V = (b4 >> 6) & 0x03;
        frame.P = (b4 >> 5) & 0x01;
        frame.X = (b4 >> 4) & 0x01;
        frame.CC = b4 & 0x0f;
        byte b5 = byteBuf.readByte();
        frame.M = b5 >> 7;
        frame.PT = b5 & 0x7f;
        frame.packageNo = byteBuf.readUnsignedShort();
        byte[] bytes = new byte[6];
        byteBuf.readBytes(bytes);
        frame.SIMCardNumber = bcd2Str(bytes);
        frame.logicalChannelNumber = byteBuf.readByte() & 0xff;
        ;
        byte b15 = byteBuf.readByte();
        frame.dataType = (b15 >> 4) & 0x0f;
        frame.subcontractingTreatmentMark = b15 & 0x0f;
        if (frame.dataType != 0b0100) {
            frame.timeStamp = byteBuf.readLong();
        } else {
            lengthOffsetPositionIndex -= 8;
        }
        if (frame.dataType != 0b0011 && frame.dataType != 0b0100) {
            frame.lastIFrameInterval = byteBuf.readShort();
            frame.lastFrameInterval = byteBuf.readShort();
        } else {
            lengthOffsetPositionIndex -= 4;
        }
        frame.dataBodyLength = byteBuf.readShort();
        if (byteBuf.readableBytes() != frame.dataBodyLength) {
            logger.error("数据帧解析错误，长度不一致, lengthOffsetPositionIndex :{}", lengthOffsetPositionIndex);
        }
        byte[] data = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(data);
        frame.dataBody = data;
        return frame;
    }

    public static String bcd2Str(byte[] bytes) {
        char[] temp = new char[bytes.length * 2];
        char val;
        for (int i = 0; i < bytes.length; i++) {
            val = (char) (((bytes[i] & 0xf0) >> 4) & 0x0f);
            temp[i * 2] = (char) (val > 9 ? val + 'A' - 10 : val + '0');
            val = (char) (bytes[i] & 0x0f);
            temp[i * 2 + 1] = (char) (val > 9 ? val + 'A' - 10 : val + '0');
        }
        return new String(temp);
    }
}
