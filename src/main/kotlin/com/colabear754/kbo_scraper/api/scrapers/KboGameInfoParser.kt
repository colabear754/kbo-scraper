package com.colabear754.kbo_scraper.api.scrapers

import com.colabear754.kbo_scraper.api.domain.CancellationReason
import com.colabear754.kbo_scraper.api.domain.GameInfo
import com.colabear754.kbo_scraper.api.domain.GameStatus
import com.colabear754.kbo_scraper.api.domain.SeriesType
import com.colabear754.kbo_scraper.api.domain.Team
import com.microsoft.playwright.Locator
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

internal fun parseGameSchedule(locators: List<Locator>, season: Int, seriesType: SeriesType): List<GameInfo> {
    val yyyyMMdd = DateTimeFormatter.ofPattern("yyyyMMdd")
    val gameCountMap = mutableMapOf<String, Int>()
    val gameInfoList = mutableListOf<GameInfo>()

    var currentDate = LocalDate.MIN  // 날짜 지정용 변수

    for (row in locators) {
        // 경기 정보가 없으면 스킵
        val playCell = row.locator("td.play")
        if (playCell.count() == 0) {
            continue
        }

        val dayCell = row.locator("td.day")
        if (dayCell.count() > 0) {
            val (month, day) = dayCell.innerText().take(5).trim().split('.').map { it.toInt() }
            currentDate = LocalDate.of(season, month, day)
        }

        val time = LocalTime.parse(row.locator("td.time").innerText().trim())

        val (awayTeam, homeTeam) = playCell.locator("> span").allInnerTexts().map { Team.findByTeamName(it) }
        val scoreSpans = playCell.locator("em span").allInnerTexts()
        var awayScore: Int? = null
        var homeScore: Int? = null
        if (scoreSpans.size > 1) {
            awayScore = scoreSpans.first().toIntOrNull()
            homeScore = scoreSpans.last().toIntOrNull()
        }

        val gameKey = "${currentDate.format(yyyyMMdd)}-$awayTeam-$homeTeam"
        val count = gameCountMap[gameKey] ?: 1
        gameCountMap[gameKey] = count + 1

        val remainCells = row.locator("td:not([class])").all()

        val cancellationReason = CancellationReason.fromString(remainCells.last().innerText().trim())
        val gameStatus = when {
            awayScore != null -> GameStatus.FINISHED
            cancellationReason != null -> GameStatus.CANCELED
            else -> GameStatus.SCHEDULED
        }

        val gameInfo = GameInfo(
            "$gameKey-$count",
            seriesType,
            currentDate,
            time,
            homeTeam,
            awayTeam,
            homeScore,
            awayScore,
            remainCells[3].innerText().trim(),
            remainCells[1].innerHTML().replace("<br>", ",").trim(),
            gameStatus,
            cancellationReason
        )

        gameInfoList.add(gameInfo)
    }

    return gameInfoList
}