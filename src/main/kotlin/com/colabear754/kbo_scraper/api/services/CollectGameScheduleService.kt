package com.colabear754.kbo_scraper.api.services

import com.colabear754.kbo_scraper.api.domain.GameInfo
import com.colabear754.kbo_scraper.api.domain.SeriesType
import com.colabear754.kbo_scraper.api.dto.responses.CollectDataResponse
import com.colabear754.kbo_scraper.api.properties.GameScheduleProperties
import com.colabear754.kbo_scraper.api.scrapers.launchChromium
import com.colabear754.kbo_scraper.api.scrapers.navigateAndBlock
import com.colabear754.kbo_scraper.api.scrapers.parseGameSchedule
import com.colabear754.kbo_scraper.api.scrapers.selectOptionAndWaitForDomChange
import com.colabear754.kbo_scraper.api.services.persistence.GameInfoWriter
import com.microsoft.playwright.Browser
import kotlinx.coroutines.*
import org.springframework.stereotype.Service
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds

@Service
class CollectGameScheduleService(
    private val gameScheduleProperties: GameScheduleProperties,
    private val gameInfoWriter: GameInfoWriter
) {
    suspend fun collectAndSaveSeasonGameInfo(season: Int): CollectDataResponse {
        return coroutineScope { scrapeAndSaveGameInfo(season, 1, 12) }
    }

    suspend fun collectAndSaveCurrentAndNextMonthGameInfo(season: Int, month: Int): CollectDataResponse {
        return coroutineScope { scrapeAndSaveGameInfo(season, month, month + 1) }
    }

    suspend fun collectAndSaveMonthGameInfo(season: Int, month: Int): CollectDataResponse {
        return coroutineScope { scrapeAndSaveGameInfo(season, month) }
    }

    /**
     * 시즌 연도와 월 범위에 해당하는 경기 일정을 스크래핑하고 저장한다.
     *
     * @param season 시즌 연도
     * @param startMonth 시작 월 (1~12)
     * @param endMonth 종료 월 (1~12). 기본값은 [startMonth]와 동일
     * @return 스크래핑된 경기 일정 목록
     */
    private suspend fun scrapeAndSaveGameInfo(
        season: Int,
        startMonth: Int,
        endMonth: Int = startMonth
    ): CollectDataResponse {
        require(startMonth in 1..12 && endMonth in 1..12) { "월 값은 1부터 12 사이여야 합니다." }
        require(startMonth <= endMonth) { "시작 월은 종료 월 이하여야 합니다." }

        val seasonGameInfo = coroutineScope {
            SeriesType.entries.flatMap { type ->
                (startMonth..endMonth).map { month ->
                    async(Dispatchers.IO) {
                        // KBO 서버 부하 방지를 위한 랜덤 딜레이(0.1 ~ 0.5초)
                        delay(Random.nextLong(100, 501).milliseconds)
                        launchChromium { scrapeGameInfo(season, month, type) }
                    }
                }.awaitAll().flatten()
            }
        }

        return withContext(Dispatchers.IO) { gameInfoWriter.saveOrUpdateGameInfo(seasonGameInfo) }
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
