package com.colabear754.kbo_scraper.initializer

import com.colabear754.kbo_scraper.api.repositories.GameInfoRepository
import com.colabear754.kbo_scraper.api.services.CollectGameScheduleService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class GameInfoInitializer(
    private val collectGameScheduleService: CollectGameScheduleService,
    private val gameInfoRepository: GameInfoRepository
) : ApplicationRunner {
    private val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    override fun run(args: ApplicationArguments?) {
        val currentSeason = LocalDate.now().year
        if (gameInfoRepository.countByDateBetween(LocalDate.of(currentSeason, 1, 1), LocalDate.of(currentSeason, 12, 31)) > 0) {
            logger.info("[$currentSeason] 시즌 경기 정보가 이미 존재합니다. 초기화 작업을 생략합니다.")
            return
        }
        logger.info("[$currentSeason] 시즌 경기 정보가 존재하지 않습니다. 초기화 작업을 시작합니다.")
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = collectGameScheduleService.collectAndSaveSeasonGameInfo(currentSeason)
                logger.info("[$currentSeason] 시즌 경기 정보 초기화 작업이 완료되었습니다. 총 수집된 경기 수: ${response.collectedCount}, 신규 경기 수: ${response.savedCount}, 수정된 경기 수: ${response.modifiedCount}")
            } catch (e: Exception) {
                logger.error("[$currentSeason] 시즌 경기 정보 초기화 작업 중 오류가 발생했습니다.", e)
            }
        }
    }
}