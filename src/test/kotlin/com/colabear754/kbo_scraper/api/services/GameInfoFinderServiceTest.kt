package com.colabear754.kbo_scraper.api.services

import com.colabear754.kbo_scraper.api.domain.GameInfo
import com.colabear754.kbo_scraper.api.domain.GameStatus
import com.colabear754.kbo_scraper.api.domain.SeriesType
import com.colabear754.kbo_scraper.api.domain.Team
import com.colabear754.kbo_scraper.api.repositories.GameInfoRepository
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDate
import java.time.LocalTime

class GameInfoFinderServiceTest : BehaviorSpec({
    val collectGameScheduleService = mockk<CollectGameScheduleService>()
    val gameInfoRepository = mockk<GameInfoRepository>()
    val gameInfoFinderService = GameInfoFinderService(collectGameScheduleService, gameInfoRepository)

    Given("경기 정보가 저장되어 있을 때") {
        val gameList = listOf(
            GameInfo(
                gameKey = "20250511-LOTTE-KT-1",
                seriesType= SeriesType.REGULAR_SEASON,
                date = LocalDate.of(2025, 5, 11),
                time = LocalTime.of(14, 0),
                awayTeam = Team.LOTTE,
                homeTeam = Team.KT,
                awayScore = 6,
                homeScore = 1,
                relay = "SS-T",
                stadium = "수원",
                gameStatus = GameStatus.FINISHED,
                cancellationReason = null
            ),
            GameInfo(
                gameKey = "20250511-LOTTE-KT-2",
                seriesType= SeriesType.REGULAR_SEASON,
                date = LocalDate.of(2025, 5, 11),
                time = LocalTime.of(17, 0),
                awayTeam = Team.LOTTE,
                homeTeam = Team.KT,
                awayScore = 1,
                homeScore = 1,
                relay = "SS-T,KN-T",
                stadium = "수원",
                gameStatus = GameStatus.FINISHED,
                cancellationReason = null
            )
        )

        When("특정 날짜, 특정 팀의 경기 결과를 조회하면") {
            every { gameInfoRepository.findByDateAndTeam(LocalDate.of(2025, 5, 11), Team.LOTTE) } returns gameList
            val result = gameInfoFinderService.findGameInfoByTeamAndDate(LocalDate.of(2025, 5, 11), Team.LOTTE)

            Then("경기 결과가 리스트로 조회된다") {
                result.size shouldBe 2

                result[0].gameKey shouldBe "20250511-LOTTE-KT-1"
                result[0].seriesType shouldBe "정규시즌"
                result[0].date shouldBe LocalDate.of(2025, 5, 11)
                result[0].time shouldBe LocalTime.of(14, 0)
                result[0].homeTeam shouldBe "KT 위즈"
                result[0].awayTeam shouldBe "롯데 자이언츠"
                result[0].homeScore shouldBe 1
                result[0].awayScore shouldBe 6
                result[0].stadium shouldBe "수원"
                result[0].relay shouldBe listOf("SS-T")
                result[0].gameStatus shouldBe "경기 종료"
                result[0].cancellationReason shouldBe null

                result[1].gameKey shouldBe "20250511-LOTTE-KT-2"
                result[1].seriesType shouldBe "정규시즌"
                result[1].date shouldBe LocalDate.of(2025, 5, 11)
                result[1].time shouldBe LocalTime.of(17, 0)
                result[1].homeTeam shouldBe "KT 위즈"
                result[1].awayTeam shouldBe "롯데 자이언츠"
                result[1].homeScore shouldBe 1
                result[1].awayScore shouldBe 1
                result[1].stadium shouldBe "수원"
                result[1].relay shouldBe listOf("SS-T","KN-T")
                result[1].gameStatus shouldBe "경기 종료"
                result[1].cancellationReason shouldBe null
            }
        }

        When("gameKey로 경기 결과를 조회하면") {
            every { gameInfoRepository.findByGameKey("20250511-LOTTE-KT-1") } returns gameList[0]
            val result = gameInfoFinderService.findGameInfoByGameKey("20250511-LOTTE-KT-1")

            Then("단건 경기 정보가 조회된다") {
                result!! shouldNotBe null
                result.gameKey shouldBe "20250511-LOTTE-KT-1"
                result.seriesType shouldBe "정규시즌"
                result.date shouldBe LocalDate.of(2025, 5, 11)
                result.time shouldBe LocalTime.of(14, 0)
                result.homeTeam shouldBe "KT 위즈"
                result.awayTeam shouldBe "롯데 자이언츠"
                result.homeScore shouldBe 1
                result.awayScore shouldBe 6
                result.stadium shouldBe "수원"
                result.relay shouldBe listOf("SS-T")
                result.gameStatus shouldBe "경기 종료"
                result.cancellationReason shouldBe null
            }
        }
    }
})
