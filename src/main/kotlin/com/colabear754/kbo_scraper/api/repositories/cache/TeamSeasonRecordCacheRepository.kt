package com.colabear754.kbo_scraper.api.repositories.cache

import com.colabear754.kbo_scraper.api.constants.TEAM_SEASON_RECORD
import com.colabear754.kbo_scraper.api.repositories.TeamSeasonRecordRepository
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Repository

@Repository
class TeamSeasonRecordCacheRepository(
    private val teamSeasonRecordRepository: TeamSeasonRecordRepository
) {
    @Cacheable(cacheNames = [TEAM_SEASON_RECORD], key = "#season")
    fun findBySeason(season: Int) = teamSeasonRecordRepository.findBySeason(season)

    @CacheEvict(cacheNames = [TEAM_SEASON_RECORD], key = "#season")
    fun clearCache(season: Int) = Unit
}