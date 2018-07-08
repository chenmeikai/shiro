package com.kenhome.config.shiro.cache;

import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;
import org.apache.shiro.cache.CacheManager;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @Author: cmk
 * @Description:
 * @Date: 2018\7\7 0007 18:16
 */
public class MyCacheManager implements CacheManager {


    private final ConcurrentMap<String, Cache> caches = new ConcurrentHashMap<String, Cache>();

    @Autowired
    private  RedisShiroCache redisShiroCache;

    @Override
    public <K, V> Cache<K, V> getCache(String s) throws CacheException {

        Cache cache = redisShiroCache;
        return cache;
    }
}
