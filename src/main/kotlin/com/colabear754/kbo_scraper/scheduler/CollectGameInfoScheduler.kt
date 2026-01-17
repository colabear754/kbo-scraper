package com.colabear754.kbo_scraper.scheduler

import com.colabear754.kbo_scraper.api.services.CollectGameScheduleService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class CollectGameInfoScheduler(
    private val collectGameScheduleService: CollectGameScheduleService
) {
    @Scheduled(cron = "0 0 4 * 2-3 MON,THU")
    suspend fun collectSeasonAllGameInfo() {
        val currentSeason = LocalDate.now().year
        collectGameScheduleService.collectAndSaveSeasonGameInfo(currentSeason)
    }

    @Scheduled(cron = "0 30 4 * 3-11 *")
    suspend fun collectCurrentMonthGameInfo() {
        val now = LocalDate.now()
        val currentSeason = now.year
        val currentMonth = now.monthValue
        collectGameScheduleService.collectAndSaveCurrentAndNextMonthGameInfo(currentSeason, currentMonth)
    }
}