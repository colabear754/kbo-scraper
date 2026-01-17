package com.colabear754.kbo_scraper.api.services

import com.colabear754.kbo_scraper.api.domain.GameInfo
import com.colabear754.kbo_scraper.api.domain.Team
import com.colabear754.kbo_scraper.api.dto.responses.CollectDataResponse
import com.colabear754.kbo_scraper.api.dto.responses.FindGameInfoResponse
import com.colabear754.kbo_scraper.api.repositories.GameInfoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

@Service
class GameInfoDataService(
    private val collectGameScheduleService: CollectGameScheduleService,
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

    suspend fun findGameInfoByTeamAndDate(date: LocalDate, team: Team): List<FindGameInfoResponse> {
        val gameInfos = withContext(Dispatchers.IO) { gameInfoRepository.findByDateAndTeam(date, team) }

        if (gameInfos.isEmpty() || gameInfos.any { it.isExpired() }) {
            collectGameScheduleService.collectAndSaveMonthGameInfo(date.year, date.monthValue)
            return withContext(Dispatchers.IO) { gameInfoRepository.findByDateAndTeam(date, team) }.map(FindGameInfoResponse::from)
        }

        return gameInfos.map(FindGameInfoResponse::from)
    }

    suspend fun findGameInfoByGameKey(gameKey: String): FindGameInfoResponse? {
        val gameInfo = withContext(Dispatchers.IO) { gameInfoRepository.findByGameKey(gameKey) }

        if (gameInfo == null || gameInfo.isExpired()) {
            val datePart = gameKey.split("-").first()
            val date = try {
                LocalDate.parse(datePart, DateTimeFormatter.BASIC_ISO_DATE)
            } catch (_: DateTimeParseException) {
                throw IllegalArgumentException("게임키 형식이 올바르지 않습니다.")
            }
            collectGameScheduleService.collectAndSaveMonthGameInfo(date.year, date.monthValue)
            return withContext(Dispatchers.IO) { gameInfoRepository.findByGameKey(gameKey) }?.let(FindGameInfoResponse::from)
        }

        return FindGameInfoResponse.from(gameInfo)
    }
}