package com.example.cache

import com.example.config.AppSettings
import redis.clients.jedis.JedisPooled

interface CacheService {
    fun set(key: String, value: String, ttlSeconds: Long)
    fun get(key: String): String?
    fun delete(key: String)
}

class RedisCacheService(settings: AppSettings) : CacheService {
    private val jedis = JedisPooled(settings.redisHost, settings.redisPort)

    override fun set(key: String, value: String, ttlSeconds: Long) {
        jedis.setex(key, ttlSeconds, value)
    }

    override fun get(key: String): String? = jedis.get(key)

    override fun delete(key: String) {
        jedis.del(key)
    }
}

class NoopCacheService : CacheService {
    private val storage = mutableMapOf<String, String>()

    override fun set(key: String, value: String, ttlSeconds: Long) {
        storage[key] = value
    }

    override fun get(key: String): String? = storage[key]

    override fun delete(key: String) {
        storage.remove(key)
    }
}
