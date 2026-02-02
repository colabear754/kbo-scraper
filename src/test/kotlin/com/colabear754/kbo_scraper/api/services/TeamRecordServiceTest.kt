package com.colabear754.kbo_scraper.api.services

import com.colabear754.kbo_scraper.api.domain.Team
import com.colabear754.kbo_scraper.api.domain.TeamSeasonRecord
import com.colabear754.kbo_scraper.api.properties.TeamRecordProperties
import com.colabear754.kbo_scraper.api.repositories.TeamSeasonRecordRepository
import com.colabear754.kbo_scraper.api.repositories.cache.TeamSeasonRecordCacheRepository
import com.colabear754.kbo_scraper.api.scrapers.launchChromium
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify

class TeamRecordServiceTest : BehaviorSpec({
    val teamSeasonRecordRepository = mockk<TeamSeasonRecordRepository>()
    val teamSeasonRecordCacheRepository = mockk<TeamSeasonRecordCacheRepository>()
    val teamRecordProperties = mockk<TeamRecordProperties>()
    val teamRecordService = TeamRecordService(
        teamSeasonRecordRepository,
        teamSeasonRecordCacheRepository,
        teamRecordProperties
    )

    beforeSpec {
        mockkStatic("com.colabear754.kbo_scraper.api.scrapers.PlaywrightExtensionKt")
    }

    afterSpec {
        unmockkStatic("com.colabear754.kbo_scraper.api.scrapers.PlaywrightExtensionKt")
    }

    Given("팀 기록 정보가 준비되었을 때") {
        val newRecord = mockk<TeamSeasonRecord>(relaxed = true).apply { every { team } returns Team.KIA }
        val existingRecordToUpdate = mockk<TeamSeasonRecord>(relaxed = true).apply { every { team } returns Team.LG }
        val seasonRecords = listOf(newRecord, existingRecordToUpdate)

        val existingRecord = mockk<TeamSeasonRecord>(relaxed = true).apply { every { team } returns Team.LG }

        When("팀 기록 수집을 진행하면") {
            every { launchChromium(any<Function1<Any, List<TeamSeasonRecord>>>()) } returns seasonRecords
            every { teamSeasonRecordRepository.findBySeason(2025) } returns listOf(existingRecord)
            every { teamSeasonRecordRepository.saveAll(any<List<TeamSeasonRecord>>()) } answers { firstArg() }

            teamRecordService.collectTeamRecords(2025)

            Then("신규 기록은 저장되고 기존 기록은 수정된다") {
                verify(exactly = 1) { teamSeasonRecordRepository.saveAll(listOf(newRecord)) }
                verify(exactly = 1) { existingRecord.updateStats(existingRecordToUpdate) }
            }
        }
    }

    Given("팀 순위 정보가 저장되어 있을 때") {
        val seasonRecords = listOf(
            TeamSeasonRecord(
                season = 2025,
                team = Team.LG,
                teamRank = 1,
                gamesPlayed = 144,
                wins = 85,
                losses = 56,
                draws = 3,
                winRate = 0.603,
                gamesBehind = 0.0,
                recent10Games = "4승0무6패",
                streak = "3패"
            ),
            TeamSeasonRecord(
                season = 2025,
                team = Team.HANWHA,
                teamRank = 2,
                gamesPlayed = 144,
                wins = 83,
                losses = 57,
                draws = 4,
                winRate = 0.593,
                gamesBehind = 1.5,
                recent10Games = "5승1무4패",
                streak = "1패"
            )
        )

        every { teamSeasonRecordCacheRepository.findBySeason(2025) } returns seasonRecords

        When("전체 팀 순위를 조회하면") {
            val result = teamRecordService.inquiryAllTeamRankings(2025)

            Then("모든 팀의 순위 정보가 반환된다") {
                result.size shouldBe 2

                result[0].rank shouldBe 1
                result[0].teamName shouldBe "LG 트윈스"
                result[0].gamesPlayed shouldBe 144
                result[0].wins shouldBe 85
                result[0].losses shouldBe 56
                result[0].draws shouldBe 3
                result[0].winRate shouldBe 0.603
                result[0].recent10Games shouldBe "4승0무6패"

                result[1].rank shouldBe 2
                result[1].teamName shouldBe "한화 이글스"
                result[1].gamesPlayed shouldBe 144
                result[1].wins shouldBe 83
                result[1].losses shouldBe 57
                result[1].draws shouldBe 4
                result[1].winRate shouldBe 0.593
                result[1].recent10Games shouldBe "5승1무4패"
            }
        }

        When("특정 팀의 순위를 조회하면") {
            val result = teamRecordService.inquiryTeamRanking(2025, Team.LG)

            Then("해당 팀의 순위 정보가 반환된다") {
                result.rank shouldBe 1
                result.teamName shouldBe "LG 트윈스"
                result.gamesPlayed shouldBe 144
                result.wins shouldBe 85
                result.losses shouldBe 56
                result.draws shouldBe 3
                result.winRate shouldBe 0.603
                result.recent10Games shouldBe "4승0무6패"
            }
        }

        When("조회하는 팀의 순위가 존재하지 않으면") {
            Then("예외가 발생한다") {
                shouldThrow<NoSuchElementException> {
                    teamRecordService.inquiryTeamRanking(2025, Team.KT)
                }
            }
        }
    }
})