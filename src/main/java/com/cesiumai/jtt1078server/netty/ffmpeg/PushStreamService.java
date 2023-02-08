package com.cesiumai.jtt1078server.netty.ffmpeg;

import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;

/**
 * @author wuxiongbin
 */
@Slf4j
public class PushStreamService extends Thread {

    private final String tag;
    private final int httpFlvPort;
    private final String ffmpegPath;
    private final boolean debugMode;
    Process process = null;

    public PushStreamService(String tag, int httpFlvPort, String ffmpegPath, boolean debugMode) {
        this.tag = tag;
        this.httpFlvPort = httpFlvPort;
        this.ffmpegPath = ffmpegPath;
        this.debugMode = debugMode;
    }

    @Override
    public void run() {
        InputStream stderr;
        int len;
        byte[] buff = new byte[512];
        String rtmpUrl = "rtmp://localhost/live/{TAG}".replaceAll("\\{TAG\\}", tag);  // 服务和srs4在同一台服务器
        String cmd = String.format("%s -re -i http://localhost:%d/live/flv?tag=%s -vcodec copy -acodec aac -f flv %s", ffmpegPath, httpFlvPort, tag, rtmpUrl);
        log.info("Execute: {}", cmd);
        try {
            process = Runtime.getRuntime().exec(cmd);
            stderr = process.getErrorStream();
            while ((len = stderr.read(buff)) > -1) {
                if (debugMode) System.out.print(new String(buff, 0, len));
            }
            log.info("Process FFMPEG exited...");
        } catch (Exception ex) {
            log.error("publish failed", ex);
        }
    }

    public void close() {
        try {
            if (process != null) process.destroyForcibly();
        } catch (Exception ignored) {
        }
    }
}
