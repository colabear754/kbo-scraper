package com.colabear754.kbo_scraper.api.scrapers

import com.colabear754.kbo_scraper.api.domain.*
import com.microsoft.playwright.Locator
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * 경기 일정 테이블의 row Locator 리스트를 파싱하여 경기 정보 리스트로 반환하는 함수.
 * 테스트 코드에서 원활하게 호출할 수 있도록 단독 탑레벨 함수로 작성.
 *
 * @param locators row Locator 리스트
 * @param season 경기 시즌
 * @param seriesType 시리즈 타입
 * @return 경기 정보 리스트
 */
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

        val gameKey = "${currentDate.format(yyyyMMdd)}-$awayTeam-$homeTeam"
        val count = gameCountMap[gameKey] ?: 1
        gameCountMap[gameKey] = count + 1

        val scores = playCell.locator("em > span").allInnerTexts().mapNotNull { it.toIntOrNull() }
        val awayScore = scores.getOrNull(0)
        val homeScore = scores.getOrNull(1)

        val remainCells = row.locator("td:not([class])").all()

        val cancellationReason = CancellationReason.fromString(remainCells.last().innerText().trim())

        val gameStatus = when {
            cancellationReason != null -> GameStatus.CANCELLED
            scores.isNotEmpty() -> GameStatus.FINISHED
            else -> GameStatus.SCHEDULED
        }

        val gameInfo = GameInfo(
            gameKey = "$gameKey-$count",
            seriesType = seriesType,
            date = currentDate,
            time = time,
            awayTeam = awayTeam,
            homeTeam = homeTeam,
            awayScore = awayScore,
            homeScore = homeScore,
            relay = remainCells[1].innerHTML().replace("<br>", ",").trim(),
            stadium = remainCells[3].innerText().trim(),
            gameStatus = gameStatus,
            cancellationReason = cancellationReason
        )

        gameInfoList.add(gameInfo)
    }

    return gameInfoList
}