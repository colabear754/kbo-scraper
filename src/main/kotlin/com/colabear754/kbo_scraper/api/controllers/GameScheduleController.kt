package com.colabear754.kbo_scraper.api.controllers

import com.colabear754.kbo_scraper.api.dto.requests.CollectDataRequest
import com.colabear754.kbo_scraper.api.dto.responses.CollectDataResponse
import com.colabear754.kbo_scraper.api.services.CollectGameScheduleService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/game-schedule")
class GameScheduleController(
    private val collectGameScheduleService: CollectGameScheduleService,
) {
    @PostMapping("/collect")
    suspend fun collectGameScheduleData(@RequestBody request: CollectDataRequest): CollectDataResponse {
        return collectGameScheduleService.collectAndSaveSeasonGameInfo(request.season, request.seriesType)
    }
}