package com.colabear754.kbo_scraper.api.scrapers

import com.colabear754.kbo_scraper.api.domain.CancellationReason
import com.colabear754.kbo_scraper.api.domain.GameStatus
import com.colabear754.kbo_scraper.api.domain.SeriesType
import com.colabear754.kbo_scraper.api.domain.Team
import com.microsoft.playwright.Page
import com.microsoft.playwright.Playwright
import io.kotest.core.spec.style.StringSpec
import io.kotest.inspectors.forAll
import io.kotest.matchers.shouldBe
import java.time.LocalDate
import java.time.LocalTime

class KboParserTest : StringSpec({
    fun <R> playwrightTest(html: String, block: Page.() -> R) = Playwright.create().use { playwright ->
        playwright.chromium().launch().use { browser ->
            browser.newPage().use {
                it.setContent(html)
                it.block()
            } }
    }

    "종료된 경기 일정을 파싱한다" {
        // given
        val html = javaClass.getResource("/finished-games.html")?.readText() ?: ""
        // when
        val gameSchedule = playwrightTest(html) { parseGameSchedule(locator("#tblScheduleList > tbody").locator("tr").all(), 2025, SeriesType.REGULAR_SEASON) }
        // then
        gameSchedule.size shouldBe 5
        // 공통: 날짜, 시간, 경기 상태
        gameSchedule.forAll {
            it.seriesType shouldBe SeriesType.REGULAR_SEASON
            it.date shouldBe LocalDate.of(2025, 5, 2)
            it.time shouldBe LocalTime.of(18, 30)
            it.gameStatus shouldBe GameStatus.FINISHED
        }
        // 1경기
        gameSchedule[0].gameKey shouldBe "20250502-SSG-LG-1"
        gameSchedule[0].awayTeam shouldBe Team.SSG
        gameSchedule[0].homeTeam shouldBe Team.LG
        gameSchedule[0].awayScore shouldBe 2
        gameSchedule[0].homeScore shouldBe 1
        gameSchedule[0].relay shouldBe "SPO-T"
        gameSchedule[0].stadium shouldBe "잠실"
        // 2경기
        gameSchedule[1].gameKey shouldBe "20250502-NC-LOTTE-1"
        gameSchedule[1].awayTeam shouldBe Team.NC
        gameSchedule[1].homeTeam shouldBe Team.LOTTE
        gameSchedule[1].awayScore shouldBe 3
        gameSchedule[1].homeScore shouldBe 4
        gameSchedule[1].relay shouldBe "MS-T"
        gameSchedule[1].stadium shouldBe "사직"
        // 3경기
        gameSchedule[2].gameKey shouldBe "20250502-DOOSAN-SAMSUNG-1"
        gameSchedule[2].awayTeam shouldBe Team.DOOSAN
        gameSchedule[2].homeTeam shouldBe Team.SAMSUNG
        gameSchedule[2].awayScore shouldBe 2
        gameSchedule[2].homeScore shouldBe 6
        gameSchedule[2].relay shouldBe "SPO-2T"
        gameSchedule[2].stadium shouldBe "대구"
        // 4경기
        gameSchedule[3].gameKey shouldBe "20250502-HEROES-KT-1"
        gameSchedule[3].awayTeam shouldBe Team.HEROES
        gameSchedule[3].homeTeam shouldBe Team.KT
        gameSchedule[3].awayScore shouldBe 5
        gameSchedule[3].homeScore shouldBe 3
        gameSchedule[3].relay shouldBe "KN-T"
        gameSchedule[3].stadium shouldBe "수원"
        // 5경기
        gameSchedule[4].gameKey shouldBe "20250502-HANWHA-KIA-1"
        gameSchedule[4].awayTeam shouldBe Team.HANWHA
        gameSchedule[4].homeTeam shouldBe Team.KIA
        gameSchedule[4].awayScore shouldBe 3
        gameSchedule[4].homeScore shouldBe 2
        gameSchedule[4].relay shouldBe "SS-T"
        gameSchedule[4].stadium shouldBe "광주"
    }

    "예정된 경기와 종료된 경기 일정을 파싱한다" {
        // given
        val html = javaClass.getResource("/scheduled-games.html")?.readText() ?: ""
        // when
        val gameSchedule = playwrightTest(html) { parseGameSchedule(locator("#tblScheduleList > tbody").locator("tr").all(), 2025, SeriesType.REGULAR_SEASON) }
        // then
        gameSchedule.size shouldBe 3
        gameSchedule.forAll {
            it.seriesType shouldBe SeriesType.REGULAR_SEASON
        }
        // 1경기
        gameSchedule[0].gameKey shouldBe "20251029-LG-HANWHA-1"
        gameSchedule[0].date shouldBe LocalDate.of(2025, 10, 29)
        gameSchedule[0].time shouldBe LocalTime.of(18, 30)
        gameSchedule[0].awayTeam shouldBe Team.LG
        gameSchedule[0].homeTeam shouldBe Team.HANWHA
        gameSchedule[0].awayScore shouldBe 3
        gameSchedule[0].homeScore shouldBe 7
        gameSchedule[0].relay shouldBe "M-T"
        gameSchedule[0].stadium shouldBe "대전"
        gameSchedule[0].gameStatus shouldBe GameStatus.FINISHED
        // 2경기
        gameSchedule[1].gameKey shouldBe "20251030-LG-HANWHA-1"
        gameSchedule[1].date shouldBe LocalDate.of(2025, 10, 30)
        gameSchedule[1].time shouldBe LocalTime.of(18, 30)
        gameSchedule[1].awayTeam shouldBe Team.LG
        gameSchedule[1].homeTeam shouldBe Team.HANWHA
        gameSchedule[1].awayScore shouldBe null
        gameSchedule[1].homeScore shouldBe null
        gameSchedule[1].relay shouldBe "K-2T"
        gameSchedule[1].stadium shouldBe "대전"
        gameSchedule[1].gameStatus shouldBe GameStatus.SCHEDULED
        // 3경기
        gameSchedule[2].gameKey shouldBe "20251031-LG-HANWHA-1"
        gameSchedule[2].date shouldBe LocalDate.of(2025, 10, 31)
        gameSchedule[2].time shouldBe LocalTime.of(18, 30)
        gameSchedule[2].awayTeam shouldBe Team.LG
        gameSchedule[2].homeTeam shouldBe Team.HANWHA
        gameSchedule[2].awayScore shouldBe null
        gameSchedule[2].homeScore shouldBe null
        gameSchedule[2].relay shouldBe "S-T"
        gameSchedule[2].stadium shouldBe "대전"
        gameSchedule[2].gameStatus shouldBe GameStatus.SCHEDULED
    }

    "취소된 경기 일정을 파싱한다" {
        // given
        val html = javaClass.getResource("/cancelled-games.html")?.readText() ?: ""
        // when
        val gameSchedule = playwrightTest(html) { parseGameSchedule(locator("#tblScheduleList > tbody").locator("tr").all(), 2025, SeriesType.REGULAR_SEASON) }
        // then
        gameSchedule.size shouldBe 5
        // 공통: 날짜, 시간
        gameSchedule.forAll {
            it.seriesType shouldBe SeriesType.REGULAR_SEASON
            it.date shouldBe LocalDate.of(2025, 7, 18)
            it.time shouldBe LocalTime.of(18, 30)
        }
        // 1경기
        gameSchedule[0].gameKey shouldBe "20250718-LOTTE-LG-1"
        gameSchedule[0].awayTeam shouldBe Team.LOTTE
        gameSchedule[0].homeTeam shouldBe Team.LG
        gameSchedule[0].awayScore shouldBe 1
        gameSchedule[0].homeScore shouldBe 2
        gameSchedule[0].relay shouldBe "SPO-T"
        gameSchedule[0].stadium shouldBe "잠실"
        gameSchedule[0].gameStatus shouldBe GameStatus.FINISHED
        // 2경기
        gameSchedule[1].gameKey shouldBe "20250718-DOOSAN-SSG-1"
        gameSchedule[1].awayTeam shouldBe Team.DOOSAN
        gameSchedule[1].homeTeam shouldBe Team.SSG
        gameSchedule[1].awayScore shouldBe null
        gameSchedule[1].homeScore shouldBe null
        gameSchedule[1].relay shouldBe "SPO-2T"
        gameSchedule[1].stadium shouldBe "문학"
        gameSchedule[1].gameStatus shouldBe GameStatus.CANCELLED
        gameSchedule[1].cancellationReason shouldBe CancellationReason.GROUND_CONDITION
        // 3경기
        gameSchedule[2].gameKey shouldBe "20250718-HEROES-SAMSUNG-1"
        gameSchedule[2].awayTeam shouldBe Team.HEROES
        gameSchedule[2].homeTeam shouldBe Team.SAMSUNG
        gameSchedule[2].awayScore shouldBe null
        gameSchedule[2].homeScore shouldBe null
        gameSchedule[2].relay shouldBe "SS-T"
        gameSchedule[2].stadium shouldBe "대구"
        gameSchedule[2].gameStatus shouldBe GameStatus.CANCELLED
        gameSchedule[2].cancellationReason shouldBe CancellationReason.RAIN
        // 4경기
        gameSchedule[3].gameKey shouldBe "20250718-HANWHA-KT-1"
        gameSchedule[3].awayTeam shouldBe Team.HANWHA
        gameSchedule[3].homeTeam shouldBe Team.KT
        gameSchedule[3].awayScore shouldBe 5
        gameSchedule[3].homeScore shouldBe 0
        gameSchedule[3].relay shouldBe "KN-T"
        gameSchedule[3].stadium shouldBe "수원"
        gameSchedule[3].gameStatus shouldBe GameStatus.FINISHED
        // 5경기
        gameSchedule[4].gameKey shouldBe "20250718-NC-KIA-1"
        gameSchedule[4].awayTeam shouldBe Team.NC
        gameSchedule[4].homeTeam shouldBe Team.KIA
        gameSchedule[4].awayScore shouldBe null
        gameSchedule[4].homeScore shouldBe null
        gameSchedule[4].relay shouldBe "MS-T"
        gameSchedule[4].stadium shouldBe "광주"
        gameSchedule[4].gameStatus shouldBe GameStatus.CANCELLED
        gameSchedule[4].cancellationReason shouldBe CancellationReason.RAIN
    }

    "더블 헤더가 포함된 경기 일정을 파싱한다" {
        // given
        val html = javaClass.getResource("/double-header-games.html")?.readText() ?: ""
        // when
        val gameSchedule = playwrightTest(html) { parseGameSchedule(locator("#tblScheduleList > tbody").locator("tr").all(), 2025, SeriesType.REGULAR_SEASON) }
        // then
        gameSchedule.size shouldBe 8
        // 공통: 날짜, 경기 상태
        gameSchedule.forAll {
            it.seriesType shouldBe SeriesType.REGULAR_SEASON
            it.date shouldBe LocalDate.of(2025, 5, 11)
            it.gameStatus shouldBe GameStatus.FINISHED
        }
        // 1경기
        gameSchedule[0].gameKey shouldBe "20250511-LG-SAMSUNG-1"
        gameSchedule[0].time shouldBe LocalTime.of(14, 0)
        gameSchedule[0].awayTeam shouldBe Team.LG
        gameSchedule[0].homeTeam shouldBe Team.SAMSUNG
        gameSchedule[0].awayScore shouldBe 7
        gameSchedule[0].homeScore shouldBe 4
        gameSchedule[0].relay shouldBe "SPO-T"
        gameSchedule[0].stadium shouldBe "대구"
        // 2경기
        gameSchedule[1].gameKey shouldBe "20250511-HANWHA-HEROES-1"
        gameSchedule[1].time shouldBe LocalTime.of(14, 0)
        gameSchedule[1].awayTeam shouldBe Team.HANWHA
        gameSchedule[1].homeTeam shouldBe Team.HEROES
        gameSchedule[1].awayScore shouldBe 8
        gameSchedule[1].homeScore shouldBe 0
        gameSchedule[1].relay shouldBe "KN-T"
        gameSchedule[1].stadium shouldBe "고척"
        // 3경기
        gameSchedule[2].gameKey shouldBe "20250511-NC-DOOSAN-1"
        gameSchedule[2].time shouldBe LocalTime.of(14, 0)
        gameSchedule[2].awayTeam shouldBe Team.NC
        gameSchedule[2].homeTeam shouldBe Team.DOOSAN
        gameSchedule[2].awayScore shouldBe 11
        gameSchedule[2].homeScore shouldBe 5
        gameSchedule[2].relay shouldBe "SPO-2T,MS-T"
        gameSchedule[2].stadium shouldBe "잠실"
        // 4경기
        gameSchedule[3].gameKey shouldBe "20250511-KIA-SSG-1"
        gameSchedule[3].time shouldBe LocalTime.of(14, 0)
        gameSchedule[3].awayTeam shouldBe Team.KIA
        gameSchedule[3].homeTeam shouldBe Team.SSG
        gameSchedule[3].awayScore shouldBe 4
        gameSchedule[3].homeScore shouldBe 8
        gameSchedule[3].relay shouldBe "M-T"
        gameSchedule[3].stadium shouldBe "문학"
        // 5경기
        gameSchedule[4].gameKey shouldBe "20250511-LOTTE-KT-1"
        gameSchedule[4].time shouldBe LocalTime.of(14, 0)
        gameSchedule[4].awayTeam shouldBe Team.LOTTE
        gameSchedule[4].homeTeam shouldBe Team.KT
        gameSchedule[4].awayScore shouldBe 6
        gameSchedule[4].homeScore shouldBe 1
        gameSchedule[4].relay shouldBe "SS-T"
        gameSchedule[4].stadium shouldBe "수원"
        // 6경기 (NC vs 두산 2차전)
        gameSchedule[5].gameKey shouldBe "20250511-NC-DOOSAN-2"
        gameSchedule[5].time shouldBe LocalTime.of(17, 0)
        gameSchedule[5].awayTeam shouldBe Team.NC
        gameSchedule[5].homeTeam shouldBe Team.DOOSAN
        gameSchedule[5].awayScore shouldBe 5
        gameSchedule[5].homeScore shouldBe 2
        gameSchedule[5].relay shouldBe "SPO-2T"
        gameSchedule[5].stadium shouldBe "잠실"
        // 7경기 (KIA vs SSG 2차전)
        gameSchedule[6].gameKey shouldBe "20250511-KIA-SSG-2"
        gameSchedule[6].time shouldBe LocalTime.of(17, 0)
        gameSchedule[6].awayTeam shouldBe Team.KIA
        gameSchedule[6].homeTeam shouldBe Team.SSG
        gameSchedule[6].awayScore shouldBe 1
        gameSchedule[6].homeScore shouldBe 5
        gameSchedule[6].relay shouldBe "MS-T,SPO-T"
        gameSchedule[6].stadium shouldBe "문학"
        // 8경기 (롯데 vs KT 2차전)
        gameSchedule[7].gameKey shouldBe "20250511-LOTTE-KT-2"
        gameSchedule[7].time shouldBe LocalTime.of(17, 0)
        gameSchedule[7].awayTeam shouldBe Team.LOTTE
        gameSchedule[7].homeTeam shouldBe Team.KT
        gameSchedule[7].awayScore shouldBe 1
        gameSchedule[7].homeScore shouldBe 1
        gameSchedule[7].relay shouldBe "SS-T,KN-T"
        gameSchedule[7].stadium shouldBe "수원"
    }

    "이동일은 파싱하지 않는다" {
        // given
        val html = javaClass.getResource("/travel-day.html")?.readText() ?: ""
        // when
        val gameSchedule = playwrightTest(html) { parseGameSchedule(locator("#tblScheduleList > tbody").locator("tr").all(), 2025, SeriesType.REGULAR_SEASON) }
        // then
        gameSchedule.size shouldBe 1
        val game = gameSchedule[0]
        game.gameKey shouldBe "20251027-HANWHA-LG-1"
        game.seriesType shouldBe SeriesType.REGULAR_SEASON
        game.date shouldBe LocalDate.of(2025, 10, 27)
        game.time shouldBe LocalTime.of(18, 30)
        game.awayTeam shouldBe Team.HANWHA
        game.homeTeam shouldBe Team.LG
        game.awayScore shouldBe 5
        game.homeScore shouldBe 13
        game.relay shouldBe "S-T"
        game.stadium shouldBe "잠실"
        game.gameStatus shouldBe GameStatus.FINISHED
    }

    "데이터가 없는 경우엔 파싱 결과가 없다" {
        // given
        val html = javaClass.getResource("/no-games.html")?.readText() ?: ""
        // when
        val gameSchedule = playwrightTest(html) { parseGameSchedule(locator("#tblScheduleList > tbody").locator("tr").all(), 2025, SeriesType.REGULAR_SEASON) }
        // then
        gameSchedule.size shouldBe 0
    }
})