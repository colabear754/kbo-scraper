package com.colabear754.kbo_scraper.api.services

import com.colabear754.kbo_scraper.api.domain.GameInfo
import com.colabear754.kbo_scraper.api.domain.SeriesType
import com.colabear754.kbo_scraper.api.dto.responses.CollectDataResponse
import com.colabear754.kbo_scraper.api.exceptions.InvalidMonthRangeException
import com.colabear754.kbo_scraper.api.properties.GameScheduleProperties
import com.colabear754.kbo_scraper.api.scrapers.launchChromium
import com.colabear754.kbo_scraper.api.scrapers.navigateAndBlock
import com.colabear754.kbo_scraper.api.scrapers.parseGameSchedule
import com.colabear754.kbo_scraper.api.scrapers.selectOptionAndWaitForDomChange
import com.microsoft.playwright.Browser
import kotlinx.coroutines.*
import org.springframework.stereotype.Service
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds

@Service
class CollectGameScheduleService(
    private val gameScheduleProperties: GameScheduleProperties,
    private val gameInfoDataService: GameInfoDataService
) {
    suspend fun collectAndSaveSeasonGameInfo(
        season: Int,
        seriesType: SeriesType? = null
    ): CollectDataResponse {
        return coroutineScope { scrapeAndSaveGameInfo(seriesType, season, 1, 12) }
    }

    suspend fun collectAndSaveCurrentAndNextMonthGameInfo(
        season: Int,
        month: Int,
        seriesType: SeriesType? = null
    ): CollectDataResponse {
        return coroutineScope { scrapeAndSaveGameInfo(seriesType, season, month, month + 1) }
    }

    /**
     * 시즌 연도와 월 범위에 해당하는 경기 일정을 스크래핑하고 저장한다.
     *
     * @param seriesType 스크래핑할 시리즈 타입 (null이면 전체 시리즈)
     * @param season 시즌 연도
     * @param startMonth 시작 월 (1~12)
     * @param endMonth 종료 월 (1~12)
     * @return 스크래핑된 경기 일정 목록
     */
    private suspend fun scrapeAndSaveGameInfo(
        seriesType: SeriesType?,
        season: Int,
        startMonth: Int,
        endMonth: Int
    ): CollectDataResponse {
        if (startMonth !in 1..12 || endMonth !in 1..12 || startMonth > endMonth) {
            throw InvalidMonthRangeException(startMonth, endMonth)
        }

        // seriesType이 null이면 전체 시리즈 수집
        val seriesTypes = seriesType?.let { listOf(it) } ?: SeriesType.entries

        val seasonGameInfo = coroutineScope {
            seriesTypes.flatMap { type ->
                (startMonth..endMonth).map { month ->
                    async(Dispatchers.IO) {
                        // KBO 서버 부하 방지를 위한 랜덤 딜레이(0.1 ~ 0.5초)
                        delay(Random.nextLong(100, 501).milliseconds)
                        launchChromium { scrapeGameInfo(season, month, type) }
                    }
                }.awaitAll().flatten()
            }
        }

        return withContext(Dispatchers.IO) { gameInfoDataService.saveOrUpdateGameInfo(seasonGameInfo) }
    }

    private fun Browser.scrapeGameInfo(season: Int, month: Int, seriesType: SeriesType): List<GameInfo> {
        return navigateAndBlock(gameScheduleProperties.url) {
            val scheduleTableLocator = locator(gameScheduleProperties.selectors.gamesTable)
            // 시즌 및 시리즈 선택
            scheduleTableLocator.selectOptionAndWaitForDomChange(gameScheduleProperties.selectors.year, "$season")
            scheduleTableLocator.selectOptionAndWaitForDomChange(gameScheduleProperties.selectors.month, "$month".padStart(2, '0'))
            scheduleTableLocator.selectOptionAndWaitForDomChange(gameScheduleProperties.selectors.series, seriesType.code)
            // 전체 row 선택 후 파싱하여 반환
            val scheduleTableRows = scheduleTableLocator.locator("tr").all()
            parseGameSchedule(scheduleTableRows, season, seriesType)
        }
    }
}
