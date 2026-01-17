package com.colabear754.kbo_scraper.api.repositories

import com.colabear754.kbo_scraper.api.domain.GameInfo
import com.colabear754.kbo_scraper.api.domain.Team
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDate

interface GameInfoRepository : JpaRepository<GameInfo, Long> {
    fun findByGameKeyIn(gameKeys: List<String>): List<GameInfo>
    @Query("select g from GameInfo g where g.date = :date and (g.homeTeam = :team or g.awayTeam = :team)")
    fun findByDateAndTeam(date: LocalDate, team: Team): List<GameInfo>
    fun findByGameKey(gameKey: String): GameInfo
    fun countByDateBetween(from: LocalDate, to: LocalDate): Long
}