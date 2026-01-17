package com.colabear754.kbo_scraper.api.services.persistence

import com.colabear754.kbo_scraper.api.domain.GameInfo
import com.colabear754.kbo_scraper.api.dto.responses.CollectDataResponse
import com.colabear754.kbo_scraper.api.repositories.GameInfoRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GameInfoWriter(
    private val gameInfoRepository: GameInfoRepository
) {
    @Transactional
    fun saveOrUpdateGameInfo(gameInfoList: List<GameInfo>): CollectDataResponse {
        val existingGamesMap = gameInfoRepository.findByGameKeyIn(gameInfoList.map { it.gameKey })
            .associateBy { it.gameKey }

        val (newGameList, gameListToUpdate) = gameInfoList.partition { existingGamesMap[it.gameKey] == null }
        gameInfoRepository.saveAll(newGameList)

        var modifiedCount = 0
        for (gameInfo in gameListToUpdate) {
            existingGamesMap[gameInfo.gameKey]?.run {
                if (update(gameInfo)) modifiedCount++
            }
        }

        return CollectDataResponse(gameInfoList.size, newGameList.size, modifiedCount)
    }
}