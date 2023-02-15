package com.cesiumai.jtt1078server.netty.ffmpeg;

import java.io.OutputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author wuxiongbin
 */
public class H264ToStreamService implements Runnable {

    private static final BlockingQueue<byte[]> queue = new LinkedBlockingQueue<>();
    private static final AtomicBoolean stop = new AtomicBoolean(false);
    private volatile Process process = null;
    private final String ffmpegPath;
    private final String rtmpUrl;

    public H264ToStreamService(String ffmpegPath, String tag) {
        this.ffmpegPath = ffmpegPath;
        this.rtmpUrl = "rtmp://localhost/live/{TAG}".replaceAll("\\{TAG}", tag);
    }

    @Override
    public void run() {
        String[] command = {ffmpegPath, "-f", "h264", "-i", "pipe:0", "-c:v", "copy", "-c:a", "copy", "-f", "flv", rtmpUrl};
        ProcessBuilder builder = new ProcessBuilder(command);
        try {
            this.process = builder.start();
            OutputStream stdin = process.getOutputStream();
            while (!stop.get()) {
                byte[] h264Data = queue.take();
                stdin.write(h264Data);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopPush() {
        stop.set(true);
        this.process.destroy();
    }

    public void writeH264Data(byte[] data) {
        try {
            queue.put(data);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
