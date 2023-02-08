package com.cesiumai.jtt1078server.websocket;

import com.cesiumai.jtt1078server.netty.ffmpeg.PushStreamService;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public enum WebSocketSessionManager {

    INSTANCE;

    private static ConcurrentHashMap<String, WebSocketSession> uuidSessionMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Set<WebSocketSession>> tagSessionMap = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<String, Set<WebSocketSession>> tagLiveSessionMap = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<String, Set<WebSocketSession>> tagHisSessionMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, PushStreamService> tagFfmpegTheadMap = new ConcurrentHashMap<>();

    public void addWebSocketSessionForUUID(String uuid, WebSocketSession session) {
        uuidSessionMap.put(uuid, session);
    }

    public void addWebSocketSessionForTag(String tag, WebSocketSession session) {
        Set<WebSocketSession> sessions = tagSessionMap.get(tag);
        if (null == sessions) {
            sessions = new HashSet<>();
        }
        sessions.add(session);
        tagSessionMap.put(tag, sessions);
    }

    public void addFfmpegTheadForTag(String tag, PushStreamService thread) {
        PushStreamService service = tagFfmpegTheadMap.get(tag);
        if (null != service) {
            service.close();
        }
        tagFfmpegTheadMap.put(tag, thread);
    }

    public void closeFfmpegTheadForTag(String tag) {
        PushStreamService service = tagFfmpegTheadMap.get(tag);
        if (null != service) {
            service.close();
        }
    }

    public void addLiveWebSocketSessionForTag(String tag, WebSocketSession session) {
        Set<WebSocketSession> sessions = tagSessionMap.get(tag);
        if (null == sessions) {
            sessions = new HashSet<>();
        }
        sessions.add(session);
        tagLiveSessionMap.put(tag, sessions);
    }

    public void addHisWebSocketSessionForTag(String tag, WebSocketSession session) {
        Set<WebSocketSession> sessions = tagSessionMap.get(tag);
        if (null == sessions) {
            sessions = new HashSet<>();
        }
        sessions.add(session);
        tagHisSessionMap.put(tag, sessions);
    }

    public WebSocketSession getWebSocketSessionByUUID(String uuid) {
        return uuidSessionMap.get(uuid);
    }

    public void sendMessage(String message, WebSocketSession session) {
        try {
            session.sendMessage(new TextMessage(message));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getDeviceIdByWebSocketSession(WebSocketSession webSocketSession) {
        Optional<Map.Entry<String, Set<WebSocketSession>>> optional = tagSessionMap.entrySet().stream().filter(entry -> entry.getValue().contains(webSocketSession)).findFirst();
        return optional.map(entry -> entry.getKey().split("-")[0]).orElse(null);
    }

    public boolean isExistHisByTag(String tag, int numberOfPeople) {
        return tagHisSessionMap.containsKey(tag) && tagHisSessionMap.get(tag).size() >= numberOfPeople;
    }

    public boolean isExistLiveByTag(String tag, int numberOfPeople) {
        return tagLiveSessionMap.containsKey(tag) && tagLiveSessionMap.get(tag).size() >= numberOfPeople;
    }

    public void removeLiveTags(String tag, WebSocketSession session) {
        deleteWebsocketSessionByTag(tag, session, tagLiveSessionMap);
    }

    public void removeHisTags(String tag, WebSocketSession session) {
        deleteWebsocketSessionByTag(tag, session, tagHisSessionMap);
    }

    private void deleteWebsocketSessionByTag(String tag, WebSocketSession session, ConcurrentHashMap<String, Set<WebSocketSession>> sessionMap) {
        Set<WebSocketSession> sessions = sessionMap.get(tag);
        if (null == sessions || sessions.isEmpty()) {
            sessionMap.remove(tag);
        } else {
            sessions.remove(session);
            if (sessions.isEmpty()) {
                sessionMap.remove(tag);
            }else{
                sessionMap.put(tag, sessions);
            }
        }
    }

    public void removeAllBySession(WebSocketSession session) {
        Optional<Map.Entry<String, WebSocketSession>> optionalEntry = uuidSessionMap.entrySet().stream().filter(entry -> entry.getValue().equals(session)).findFirst();
        optionalEntry.ifPresent(entry -> uuidSessionMap.remove(entry.getKey()));
        removeTagLiveSessionMapBySession(session);
        removeTagHisSessionMapBySession(session);
        removeTagSessionMapBySession(session);
    }

    public void removeTagSessionMapBySession(WebSocketSession session) {
        removeBySession(session, tagSessionMap);
    }

    public void removeTagLiveSessionMapBySession(WebSocketSession session) {
        removeBySession(session, tagLiveSessionMap);
    }

    public void removeTagHisSessionMapBySession(WebSocketSession session) {
        removeBySession(session, tagHisSessionMap);
    }

    private void removeBySession(WebSocketSession session, ConcurrentHashMap<String, Set<WebSocketSession>> sessionMap) {
        Optional<Map.Entry<String, Set<WebSocketSession>>> optional = tagSessionMap.entrySet().stream().filter(entry -> entry.getValue().contains(session)).findFirst();
        if (optional.isPresent()) {
            Map.Entry<String, Set<WebSocketSession>> entry = optional.get();
            Set<WebSocketSession> sessionSet = entry.getValue();
            String key = entry.getKey();
            PushStreamService pushStreamService = tagFfmpegTheadMap.get(key);
            if (null != pushStreamService) {
                pushStreamService.close();
            }
            if (sessionSet.size() > 1) {
                sessionSet.remove(session);
                sessionMap.put(key, sessionSet);
            } else {
                sessionMap.remove(key);
            }
        }

    }
}
