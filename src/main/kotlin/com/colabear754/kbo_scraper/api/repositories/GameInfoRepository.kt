package com.colabear754.kbo_scraper.api.repositories

import com.colabear754.kbo_scraper.api.domain.GameInfo
import org.springframework.data.jpa.repository.JpaRepository

interface GameInfoRepository : JpaRepository<GameInfo, Long> {
    fun findByGameKeyIn(gameKeys: List<String>): List<GameInfo>
}