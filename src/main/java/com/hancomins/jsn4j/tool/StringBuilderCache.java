package com.hancomins.jsn4j.tool;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * StringBuilder 인스턴스를 재사용하기 위한 ThreadLocal 기반 캐시
 * 각 스레드별로 독립적인 StringBuilder 풀을 유지합니다.
 */
public class StringBuilderCache {
    
    private static final ThreadLocal<Deque<StringBuilder>> CACHE = 
        ThreadLocal.withInitial(ArrayDeque::new);
    
    private static volatile boolean cacheEnabled = true;
    private static final int INITIAL_CAPACITY = 256;
    private static int MAX_CACHE_SIZE = 32;
    private static int MAX_BUILDER_SIZE = 1024 * 1024; // 1MB

    /**
     * 캐시의 최대 크기를 설정합니다. 이 값의 이상으로 StringBuilder 인스턴스가 캐시되지 않습니다.
     * @param maxCacheSize 최대 캐시 크기 (양수)
     */
    public static void setMaxCacheSize(int maxCacheSize) {
        if (maxCacheSize <= 0) {
            throw new IllegalArgumentException("Max cache size must be greater than 0");
        }
        MAX_CACHE_SIZE = maxCacheSize;
    }

    /**
     * StringBuilder의 최대 크기를 설정합니다. 이 크기 이상의 StringBuilder는 캐시되지 않습니다.
     * @param maxBuilderSize 최대 StringBuilder 크기 (양수)
     */
    public static void setMaxBuilderSize(int maxBuilderSize) {
        if (maxBuilderSize <= 0) {
            throw new IllegalArgumentException("Max builder size must be greater than 0");
        }
        MAX_BUILDER_SIZE = maxBuilderSize;
    }


    
    /**
     * 캐시에서 StringBuilder를 가져오거나 새로 생성합니다.
     * @return 사용 가능한 StringBuilder 인스턴스
     */
    public static StringBuilder acquire() {
        if (!cacheEnabled) {
            return new StringBuilder(INITIAL_CAPACITY);
        }
        
        Deque<StringBuilder> deque = CACHE.get();
        StringBuilder sb = deque.pollFirst();
        
        if (sb == null) {
            sb = new StringBuilder(INITIAL_CAPACITY);
        } else {
            sb.setLength(0); // 내용을 비우고 재사용
        }
        
        return sb;
    }
    
    /**
     * 사용이 끝난 StringBuilder를 캐시에 반환합니다.
     * @param sb 반환할 StringBuilder
     */
    public static void release(StringBuilder sb) {
        if (!cacheEnabled || sb == null) {
            return;
        }
        
        // 너무 큰 StringBuilder는 캐시하지 않음
        if (sb.capacity() > MAX_BUILDER_SIZE) {
            return;
        }
        
        Deque<StringBuilder> deque = CACHE.get();
        if (deque.size() < MAX_CACHE_SIZE) {
            deque.offerFirst(sb);
        }
    }
    
    /**
     * 현재 스레드의 캐시를 모두 제거합니다.
     */
    public static void clearCache() {
        CACHE.remove();
    }
    
    /**
     * 모든 스레드의 캐시를 제거합니다.
     * 주의: 이 메서드는 현재 스레드의 캐시만 제거할 수 있습니다.
     * 다른 스레드의 캐시는 해당 스레드에서 clearCache()를 호출해야 합니다.
     */
    public static void clearCurrentThreadCache() {
        CACHE.remove();
    }
    
    /**
     * 캐시 사용 여부를 설정합니다.
     * @param enabled true면 캐시 사용, false면 캐시 미사용
     */
    public static void setCacheEnabled(boolean enabled) {
        cacheEnabled = enabled;
        if (!enabled) {
            clearCurrentThreadCache();
        }
    }
    
    /**
     * 캐시 사용 여부를 반환합니다.
     * @return 캐시 사용 중이면 true
     */
    public static boolean isCacheEnabled() {
        return cacheEnabled;
    }
    
    /**
     * 현재 스레드의 캐시 크기를 반환합니다.
     * @return 캐시된 StringBuilder 개수
     */
    public static int getCacheSize() {
        if (!cacheEnabled) {
            return 0;
        }
        Deque<StringBuilder> deque = CACHE.get();
        return deque != null ? deque.size() : 0;
    }
    
    /**
     * 캐시 통계 정보를 반환합니다.
     * @return 캐시 상태 정보 문자열
     */
    public static String getCacheStats() {
        return String.format("Cache enabled: %s, Current thread cache size: %d/%d", 
            cacheEnabled, getCacheSize(), MAX_CACHE_SIZE);
    }
}