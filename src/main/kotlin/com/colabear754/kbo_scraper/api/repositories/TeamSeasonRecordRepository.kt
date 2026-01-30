package com.colabear754.kbo_scraper.api.repositories

import com.colabear754.kbo_scraper.api.domain.TeamSeasonRecord
import org.springframework.data.jpa.repository.JpaRepository

interface TeamSeasonRecordRepository : JpaRepository<TeamSeasonRecord, Long> {
    fun findBySeason(season: Int): List<TeamSeasonRecord>
}