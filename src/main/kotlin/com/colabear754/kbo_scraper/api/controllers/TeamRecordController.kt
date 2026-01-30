package com.colabear754.kbo_scraper.api.controllers

import com.colabear754.kbo_scraper.api.domain.Team
import com.colabear754.kbo_scraper.api.dto.requests.CollectDataRequest
import com.colabear754.kbo_scraper.api.dto.responses.TeamRecordResponse
import com.colabear754.kbo_scraper.api.services.TeamRecordService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/team-record")
class TeamRecordController(
    private val teamRecordService: TeamRecordService
) {
    @PostMapping("/collect")
    fun collectTeamRecords(@RequestBody request: CollectDataRequest) {
        teamRecordService.collectTeamRecords(request.season)
    }

    @GetMapping("/{season}")
    fun findAllTeamsRecords(@PathVariable season: Int): List<TeamRecordResponse> {
        return teamRecordService.inquiryAllTeamRankings(season)
    }

    @GetMapping("/{season}/{team}")
    fun findTeamRecords(@PathVariable season: Int, @PathVariable team: Team): TeamRecordResponse {
        return teamRecordService.inquiryTeamRanking(season, team)
    }
}