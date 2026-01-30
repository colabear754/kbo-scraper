package com.colabear754.kbo_scraper.api.config

import com.colabear754.kbo_scraper.api.constants.CacheType
import com.colabear754.kbo_scraper.api.domain.GameInfo
import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.Expiry
import org.checkerframework.checker.index.qual.NonNegative
import org.springframework.cache.CacheManager
import org.springframework.cache.caffeine.CaffeineCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import kotlin.time.toKotlinDuration

@Configuration
class CacheConfig {
    @Bean
    fun cacheManager(): CacheManager = CaffeineCacheManager().apply {
        registerCache(CacheType.GAME_INFO_BY_DATE,
            Caffeine.newBuilder()
                .expireAfter(GameInfoByDateExpiry)
                .maximumSize(1000L)
                .build())

        registerCache(CacheType.GAME_INFO_BY_KEY,
            Caffeine.newBuilder()
                .expireAfter(GameInfoByKeyExpiry)
                .maximumSize(1000L)
                .build())
    }

    object GameInfoByDateExpiry : Expiry<LocalDate, List<GameInfo>> {
        override fun expireAfterCreate(key: LocalDate, value: List<GameInfo>, currentTime: Long): Long {
            return value.minOfOrNull { it.getCacheExpiration() } ?: Duration.between(LocalTime.now(), LocalTime.MAX).toKotlinDuration().inWholeNanoseconds
        }

        override fun expireAfterUpdate(key: LocalDate, value: List<GameInfo>, currentTime: Long, currentDuration: @NonNegative Long): Long {
            return value.minOfOrNull { it.getCacheExpiration() } ?: Duration.between(LocalTime.now(), LocalTime.MAX).toKotlinDuration().inWholeNanoseconds
        }

        override fun expireAfterRead(key: LocalDate, value: List<GameInfo>, currentTime: Long, currentDuration: @NonNegative Long): Long {
            return currentDuration
        }
    }

    object GameInfoByKeyExpiry : Expiry<String, GameInfo> {
        override fun expireAfterCreate(key: String, value: GameInfo, currentTime: Long): Long {
            return value.getCacheExpiration()
        }

        override fun expireAfterUpdate(key: String, value: GameInfo, currentTime: Long, currentDuration: @NonNegative Long): Long {
            return value.getCacheExpiration()
        }

        override fun expireAfterRead(key: String, value: GameInfo, currentTime: Long, currentDuration: @NonNegative Long): Long {
            return currentDuration
        }
    }

    private fun <K, V> CaffeineCacheManager.registerCache(cacheType: CacheType, cache: Cache<K, V>) {
        @Suppress("UNCHECKED_CAST")
        this.registerCustomCache(cacheType.cacheName, cache as Cache<Any, Any>)
    }
}