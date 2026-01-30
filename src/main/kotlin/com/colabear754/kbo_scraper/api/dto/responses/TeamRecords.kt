package com.colabear754.kbo_scraper.api.dto.responses

import com.colabear754.kbo_scraper.api.domain.TeamSeasonRecord

data class TeamRecords(
    val rank: Int,
    val teamName: String,
    val gamesPlayed: Int,
    val wins: Int,
    val losses: Int,
    val draws: Int,
    val winRate: Double,
    val recent10Games: String
) {
    companion object {
        fun from(teamSeasonRecord: TeamSeasonRecord) = TeamRecords(
            rank = teamSeasonRecord.teamRank,
            teamName = teamSeasonRecord.team.teamName,
            gamesPlayed = teamSeasonRecord.gamesPlayed,
            wins = teamSeasonRecord.wins,
            losses = teamSeasonRecord.losses,
            draws = teamSeasonRecord.draws,
            winRate = teamSeasonRecord.winRate,
            recent10Games = teamSeasonRecord.recent10Games
        )
    }
}