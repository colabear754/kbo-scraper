package com.colabear754.kbo_scraper.api.services

import com.colabear754.kbo_scraper.api.domain.GameInfo
import com.colabear754.kbo_scraper.api.domain.SeriesType
import com.colabear754.kbo_scraper.api.dto.responses.CollectDataResponse
import com.colabear754.kbo_scraper.api.properties.GameScheduleProperties
import com.colabear754.kbo_scraper.api.scrapers.navigateAndBlock
import com.colabear754.kbo_scraper.api.scrapers.parseGameSchedule
import com.microsoft.playwright.Browser
import com.microsoft.playwright.ElementHandle
import com.microsoft.playwright.Locator
import com.microsoft.playwright.Playwright
import com.microsoft.playwright.options.ElementState
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
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
        seriesType: SeriesType?
    ): CollectDataResponse {
        // seriesType이 null이면 전체 시리즈 수집
        val seriesTypes = seriesType?.let { listOf(it) } ?: SeriesType.entries
        // 1월부터 12월까지 해당 시즌/시리즈의 경기 일정을 비동기 수집 후 취합
        val seasonGameInfo = launchChromium { coroutineScope {
            seriesTypes.flatMap { type ->
                (1..12).map { month -> async {
                    // KBO 서버 부하 방지를 위한 랜덤 딜레이(0.1 ~ 0.5초)
                    delay(Random.nextLong(100, 500).milliseconds)
                    scrapeGameInfo(season, month, type)
                } }.awaitAll().flatten()
            }
        } }

        return gameInfoDataService.saveOrUpdateGameInfo(seasonGameInfo)
    }

    private suspend fun <R> launchChromium(action: suspend Browser.() -> R): R =
        Playwright.create().use { playwright ->
            playwright.chromium().launch().use { browser ->
                browser.action()
            }
        }

    private fun Browser.scrapeGameInfo(season: Int, month: Int, seriesType: SeriesType): List<GameInfo> {
        return navigateAndBlock(gameScheduleProperties.url) {
            val scheduleTableLocator = locator(gameScheduleProperties.selectors.gamesTable)
            // 시즌 및 시리즈 선택
            scheduleTableLocator.selectOptionAndWaitForDomChange(gameScheduleProperties.selectors.year, "$season")
            scheduleTableLocator.selectOptionAndWaitForDomChange(gameScheduleProperties.selectors.month, "$month".padStart(2, '0'))
            scheduleTableLocator.selectOptionAndWaitForDomChange(gameScheduleProperties.selectors.series, seriesType.code)
            // 전체 row 선택 후 파싱하여 반환
            parseGameSchedule(scheduleTableLocator.locator("tr").all(), season, seriesType)
        }
    }
}

private fun Locator.selectOptionAndWaitForDomChange(
    selectBoxSelector: String,
    optionValue: String
) {
    val oldElement = elementHandle()

    page().locator(selectBoxSelector).selectOption(optionValue)

    oldElement?.waitForElementState(
        ElementState.HIDDEN,
        ElementHandle.WaitForElementStateOptions().apply { timeout = 10000.0 }
    )
}