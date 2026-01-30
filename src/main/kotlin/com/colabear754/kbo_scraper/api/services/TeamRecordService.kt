package com.colabear754.kbo_scraper.api.services

import com.colabear754.kbo_scraper.api.domain.Team
import com.colabear754.kbo_scraper.api.dto.responses.TeamRecordResponse
import com.colabear754.kbo_scraper.api.properties.TeamRecordProperties
import com.colabear754.kbo_scraper.api.repositories.TeamSeasonRecordRepository
import com.colabear754.kbo_scraper.api.repositories.cache.TeamSeasonRecordCacheRepository
import com.colabear754.kbo_scraper.api.scrapers.launchChromium
import com.colabear754.kbo_scraper.api.scrapers.navigateAndBlock
import com.colabear754.kbo_scraper.api.scrapers.parseTeamSeasonRecord
import com.colabear754.kbo_scraper.api.scrapers.selectOptionAndWaitForDomChange
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class TeamRecordService(
    private val teamSeasonRecordRepository: TeamSeasonRecordRepository,
    private val teamSeasonRecordCacheRepository: TeamSeasonRecordCacheRepository,
    private val teamRecordProperties: TeamRecordProperties
) {
    @Transactional
    fun collectTeamRecords(season: Int) {
        val seasonRecords = launchChromium {
            navigateAndBlock(teamRecordProperties.url) {
                val rankTableLocator = locator(teamRecordProperties.selectors.rankTable)
                // 파라미터로 받은 시즌 존재 여부 확인
                if (locator("${teamRecordProperties.selectors.year} option[value=\"$season\"]").count() == 0) {
                    throw NoSuchElementException("$season 팀 기록 정보가 존재하지 않습니다.")
                }
                // 시즌 선택
                rankTableLocator.selectOptionAndWaitForDomChange(teamRecordProperties.selectors.year, "$season")
                rankTableLocator.selectOptionAndWaitForDomChange(teamRecordProperties.selectors.series, "0")
                // 전체 row 선택 후 파싱
                val rankTableRows = rankTableLocator.locator("tr").all()
                parseTeamSeasonRecord(rankTableRows, season)
            }
        }

        val existingRecords = teamSeasonRecordRepository.findBySeason(season)
        val existingRecordMap = existingRecords.associateBy { it.team }
        for (record in seasonRecords) {
            existingRecordMap[record.team]?.run { updateStats(record) }
        }

        teamSeasonRecordRepository.saveAll(seasonRecords.filter { existingRecordMap[it.team] == null })
    }

    fun inquiryAllTeamRankings(season: Int): List<TeamRecordResponse> {
        return teamSeasonRecordCacheRepository.findBySeason(season)
            .sortedBy { it.teamRank }
            .map(TeamRecordResponse::from)
    }

    fun inquiryTeamRanking(season: Int, team: Team): TeamRecordResponse {
        return teamSeasonRecordCacheRepository.findBySeason(season)
            .firstOrNull { it.team == team }
            ?.let(TeamRecordResponse::from) ?: throw NoSuchElementException("$season ${team.teamFullName} 기록 정보가 존재하지 않습니다.")
    }
}