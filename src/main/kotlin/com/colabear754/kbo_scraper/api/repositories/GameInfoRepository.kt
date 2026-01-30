package com.colabear754.kbo_scraper.api.repositories

import com.colabear754.kbo_scraper.api.domain.GameInfo
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate

interface GameInfoRepository : JpaRepository<GameInfo, Long> {
    fun findByGameKeyIn(gameKeys: List<String>): List<GameInfo>
    fun findByDate(date: LocalDate): List<GameInfo>
    fun findByGameKey(gameKey: String): GameInfo?
    fun countByDateBetween(from: LocalDate, to: LocalDate): Long
}