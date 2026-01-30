package com.colabear754.kbo_scraper.api.repositories.cache

import com.colabear754.kbo_scraper.api.constants.GAME_INFO_BY_DATE
import com.colabear754.kbo_scraper.api.constants.GAME_INFO_BY_KEY
import com.colabear754.kbo_scraper.api.domain.GameInfo
import org.springframework.cache.CacheManager
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
class GameInfoCacheRepository(
    private val cacheManager: CacheManager
) {
    private val cacheByDate by lazy { cacheManager.getCache(GAME_INFO_BY_DATE)!! }
    private val cacheByKey by lazy { cacheManager.getCache(GAME_INFO_BY_KEY)!! }

    /**
     * 해당 일자의 경기 정보를 찾아서 반환하고, 캐시에 없을 경우 [loader]를 실행하여 데이터를 적재한 후 반환합니다.
     * @param date 경기 일자
     * @param loader 캐시 미스 시 데이터를 적재할 람다
     */
    suspend fun findOrLoadGameInfoByDate(date: LocalDate, loader: suspend () -> List<GameInfo>): List<GameInfo> {
        return cacheByDate.getTypedList(date) ?: loader().also {
            cacheByDate.put(date, it)
            it.forEach { gameInfo -> cacheByKey.put(gameInfo.gameKey, gameInfo) }
        }
    }

    /**
     * 해당 키의 경기 정보를 찾아서 반환하고, 캐시에 없을 경우 [loader]를 실행하여 데이터를 적재한 후 반환합니다.
     * @param gameKey 경기 키
     * @param loader 캐시 미스 시 데이터를 적재할 람다
     */
    suspend fun findOrLoadGameInfoByKey(gameKey: String, loader: suspend () -> GameInfo?): GameInfo {
        return cacheByKey.getTyped(gameKey) ?: loader()?.also { cacheByKey.put(gameKey, it) }
        ?: throw NoSuchElementException("경기 정보가 존재하지 않습니다.")
    }
}