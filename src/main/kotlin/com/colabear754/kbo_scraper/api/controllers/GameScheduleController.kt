package com.colabear754.kbo_scraper.api.controllers

import com.colabear754.kbo_scraper.api.domain.Team
import com.colabear754.kbo_scraper.api.dto.requests.CollectDataRequest
import com.colabear754.kbo_scraper.api.dto.responses.CollectDataResponse
import com.colabear754.kbo_scraper.api.dto.responses.FindGameInfoResponse
import com.colabear754.kbo_scraper.api.services.CollectGameScheduleService
import com.colabear754.kbo_scraper.api.services.GameInfoFinderService
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@RestController
@RequestMapping("/api/game-schedule")
class GameScheduleController(
    private val collectGameScheduleService: CollectGameScheduleService,
    private val gameInfoFinderService: GameInfoFinderService
) {
    @PostMapping("/collect")
    suspend fun collectGameScheduleData(@RequestBody request: CollectDataRequest): CollectDataResponse {
        return collectGameScheduleService.collectAndSaveSeasonGameInfo(request.season, request.seriesType)
    }

    @GetMapping("/{team}/{date}")
    suspend fun findGameInfo(@PathVariable team: Team, @PathVariable date: LocalDate): List<FindGameInfoResponse> {
        return gameInfoFinderService.findGameInfoByTeamAndDate(date, team)
    }

    @GetMapping("/{gameKey}")
    suspend fun findSpecificGameInfo(@PathVariable gameKey: String): FindGameInfoResponse? {
        return gameInfoFinderService.findGameInfoByGameKey(gameKey)
    }
}