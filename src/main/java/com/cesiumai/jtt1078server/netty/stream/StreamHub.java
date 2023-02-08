package com.cesiumai.jtt1078server.netty.stream;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author wuxiongbin
 */
public class StreamHub {

    private static Map<String, Map<StreamFrameSink, StreamFrameSink>> sinkMap = new ConcurrentHashMap<>();

    public static Map<StreamFrameSink, StreamFrameSink> GetStream(String name) {
        if (sinkMap.containsKey(name)) {
            return sinkMap.get(name);
        }
        Map<StreamFrameSink, StreamFrameSink> map = new ConcurrentHashMap<>();
        Map<StreamFrameSink, StreamFrameSink> retMap = sinkMap.putIfAbsent(name, map);
        return retMap == null ? map : retMap;
    }

    public static void EnterStream(String name, StreamFrameSink sink) {
        GetStream(name).put(sink, sink);
    }

    public static void LeaveStream(String name, StreamFrameSink sink) {
        GetStream(name).remove(sink);
    }

    public static void WriteFrame(Map<StreamFrameSink, StreamFrameSink> map, StreamFrame frame) {
        for (StreamFrameSink key : map.keySet()) {
            key.WriteFrame(frame);
        }
    }

    public static Set<String> GetStreams() {
        return sinkMap.keySet();
    }
}
