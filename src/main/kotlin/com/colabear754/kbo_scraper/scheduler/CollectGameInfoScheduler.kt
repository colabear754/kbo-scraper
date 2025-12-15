package com.colabear754.kbo_scraper.scheduler

import com.colabear754.kbo_scraper.api.services.CollectGameScheduleService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class CollectGameInfoScheduler(
    private val collectGameScheduleService: CollectGameScheduleService
) {
    @Scheduled(cron = "0 0 3 * 2-11 *")
    suspend fun collectGameInfo() {
        val currentSeason = LocalDate.now().year
        collectGameScheduleService.collectAndSaveSeasonGameInfo(currentSeason)
    }
}