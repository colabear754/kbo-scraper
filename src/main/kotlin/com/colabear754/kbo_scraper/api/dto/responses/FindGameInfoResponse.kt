package com.colabear754.kbo_scraper.api.dto.responses

import com.colabear754.kbo_scraper.api.domain.GameInfo
import java.time.LocalDate
import java.time.LocalTime

data class FindGameInfoResponse(
    val gameKey: String,
    val seriesType: String,
    val date: LocalDate,
    val time: LocalTime?,
    val homeTeam: String,
    val awayTeam: String,
    val homeScore: Int?,
    val awayScore: Int?,
    val stadium: String?,
    val relay: List<String>,
    val gameStatus: String,
    val cancellationReason: String?
) {
    companion object {
        fun from(gameInfo: GameInfo) = FindGameInfoResponse(
            gameInfo.gameKey,
            gameInfo.seriesType.seriesName,
            gameInfo.date,
            gameInfo.time,
            gameInfo.homeTeam.teamFullName,
            gameInfo.awayTeam.teamFullName,
            gameInfo.homeScore,
            gameInfo.awayScore,
            gameInfo.stadium,
            gameInfo.relay?.split(",") ?: listOf(),
            gameInfo.gameStatus.statusName,
            gameInfo.cancellationReason?.reasonName
        )
    }
}