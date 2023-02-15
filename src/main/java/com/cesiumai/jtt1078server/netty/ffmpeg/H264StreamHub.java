package com.cesiumai.jtt1078server.netty.ffmpeg;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author wuxiongbin
 */
public class H264StreamHub {

    private static final ConcurrentHashMap<String, H264ToStreamService> tagHashMap = new ConcurrentHashMap<>();

    public static H264ToStreamService getH264ToStreamInstance(String tag) {
        if (tagHashMap.containsKey(tag)) {
            return tagHashMap.get(tag);
        }
        return null;
    }

    public static H264ToStreamService createH264ToStreamInstance(String ffmpegPath, String tag) {
        H264ToStreamService instance = getH264ToStreamInstance(tag);
        if (null != instance) {
            return instance;
        }
        H264ToStreamService service = new H264ToStreamService(ffmpegPath, tag);
        tagHashMap.put(tag, service);
        return service;
    }

    public void closeFfmpegTheadForTag(String tag) {
        if (tagHashMap.containsKey(tag)) {
            H264ToStreamService service = tagHashMap.get(tag);
            service.stopPush();
            tagHashMap.remove(tag);
        }
    }
}
