package com.colabear754.kbo_scraper.api.services.persistence

import com.colabear754.kbo_scraper.api.domain.GameInfo
import com.colabear754.kbo_scraper.api.repositories.GameInfoRepository
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

class GameInfoWriterTest : BehaviorSpec({
    val gameInfoRepository = mockk<GameInfoRepository>()
    val gameInfoWriter = GameInfoWriter(gameInfoRepository)

    Given("경기 정보들이 신규 1건, 수정 1건, 미수정 1건 준비되었을 때") {
        val newGame = mockk<GameInfo>().apply { every { gameKey } returns "gameKey1" }
        val gameToUpdate = mockk<GameInfo>().apply { every { gameKey } returns "gameKey2" }
        val gameWithoutChange = mockk<GameInfo>().apply { every { gameKey } returns "gameKey3" }
        val inputGames = listOf(newGame, gameToUpdate, gameWithoutChange)

        val existingGame1 = mockk<GameInfo>().apply { every { gameKey } returns "gameKey2" }
        val existingGame2 = mockk<GameInfo>().apply { every { gameKey } returns "gameKey3" }

        When("경기 정보들이 혼합된 리스트를 DB에 반영하면") {
            every { existingGame1.update(gameToUpdate) } returns true
            every { existingGame2.update(gameWithoutChange) } returns false
            every { gameInfoRepository.saveAll(any<List<GameInfo>>()) } answers { firstArg() }
            every { gameInfoRepository.findByGameKeyIn(listOf("gameKey1", "gameKey2", "gameKey3")) } returns
                    listOf(existingGame1, existingGame2)

            val result = gameInfoWriter.saveOrUpdateGameInfo(inputGames)

            Then("수집 3건, 저장 1건, 수정 1건이어야 한다") {
                result.collectedCount shouldBe 3
                result.savedCount shouldBe 1
                result.modifiedCount shouldBe 1
            }

            Then("신규 경기에 대해서만 save가 실행되어야 한다") {
                verify(exactly = 1) { gameInfoRepository.saveAll(withArg<List<GameInfo>> {
                    it.size shouldBe 1
                    it shouldContain newGame
                    it shouldNotContain gameToUpdate
                    it shouldNotContain gameWithoutChange
                }) }
            }

            Then("기존 경기들만 update가 실행되어야 한다") {
                verify(exactly = 0) { newGame.update(any()) }
                verify(exactly = 1) { existingGame1.update(gameToUpdate) }
                verify(exactly = 1) { existingGame2.update(gameWithoutChange) }
            }
        }
    }
})
