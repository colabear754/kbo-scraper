package com.colabear754.kbo_scraper.api.controllers

import com.colabear754.kbo_scraper.api.domain.game.Team
import com.colabear754.kbo_scraper.api.dto.requests.CollectKboDataRequest
import com.colabear754.kbo_scraper.api.dto.responses.CollectKboDataResponse
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
    suspend fun collectGameScheduleData(@RequestBody request: CollectKboDataRequest): CollectKboDataResponse {
        return collectGameScheduleService.collectAndSaveSeasonGameInfo(request.season)
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