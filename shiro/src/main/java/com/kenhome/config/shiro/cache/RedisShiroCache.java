package com.kenhome.config.shiro.cache;

import com.alibaba.fastjson.JSON;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Collection;
import java.util.Set;

/**
 * @Author: cmk
 * @Description: 实现shiro缓存接口
 * @Date: 2018\7\7 0007 17:29
 */

@Component
public class RedisShiroCache<K, V> implements Cache<K, V>, Serializable {

    private static final Logger log = LoggerFactory.getLogger(RedisShiroCache.class);

    private static final String PREFIX = "SHIRO_SESSION_ID";

    @Autowired
    @Qualifier("redisTemplate")
    private RedisTemplate<String, Object> redisTemplate;


    @Override
    public V get(K k) throws CacheException {
        V v = (V) redisTemplate.opsForValue().get(PREFIX + k);
        log.info("{}从缓存中获取{}",k, JSON.toJSONString(v));
        return v;
    }


    @Override
    public V put(K k, V v) throws CacheException {
        log.info("{}存储到缓存",k);
        redisTemplate.opsForValue().set(PREFIX + k, v);
        return v;
    }

    @Override
    public V remove(K k) throws CacheException {
        V v = (V) redisTemplate.opsForValue().get(PREFIX + k);
        redisTemplate.delete(PREFIX + k);
        return v;
    }

    @Override
    public void clear() throws CacheException {
        Set<String> keys = redisTemplate.keys(PREFIX + "*");
        redisTemplate.delete(keys);
    }

    @Override
    public int size() {
        Set<String> keys = redisTemplate.keys(PREFIX + "*");
        return keys.size();
    }

    @Override
    public Set<K> keys() {
        Set<K> keys = (Set<K>) redisTemplate.keys(PREFIX + "*");
        return keys;
    }

    @Override
    public Collection<V> values() {
        return null;
    }
}

