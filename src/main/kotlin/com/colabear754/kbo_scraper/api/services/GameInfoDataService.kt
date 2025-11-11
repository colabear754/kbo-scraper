package com.colabear754.kbo_scraper.api.services

import com.colabear754.kbo_scraper.api.domain.GameInfo
import com.colabear754.kbo_scraper.api.domain.Team
import com.colabear754.kbo_scraper.api.dto.responses.CollectDataResponse
import com.colabear754.kbo_scraper.api.dto.responses.FindGameInfoResponse
import com.colabear754.kbo_scraper.api.repositories.GameInfoRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
class GameInfoDataService(
    private val gameInfoRepository: GameInfoRepository
) {
    @Transactional
    fun saveOrUpdateGameInfo(seasonGameInfo: List<GameInfo>): CollectDataResponse {
        var savedCount = 0
        var modifiedCount = 0

        val existingGamesMap = gameInfoRepository.findByGameKeyIn(seasonGameInfo.map { it.gameKey })
            .associateBy { it.gameKey }

        for (gameInfo in seasonGameInfo) {
            val existingGame = existingGamesMap[gameInfo.gameKey]
            if (existingGame == null) {
                gameInfoRepository.save(gameInfo)
                savedCount++
                continue
            }
            val isUpdated = existingGame.update(gameInfo)
            if (isUpdated) {
                modifiedCount++
            }
        }

        return CollectDataResponse(seasonGameInfo.size, savedCount, modifiedCount)
    }

    fun findGameInfoByTeamAndDate(date: LocalDate, team: Team): List<FindGameInfoResponse> {
        return gameInfoRepository.findByDateAndTeam(date, team)
            .map(FindGameInfoResponse::from)
    }
}