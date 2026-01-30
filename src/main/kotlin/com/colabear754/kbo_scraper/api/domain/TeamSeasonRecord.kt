package com.colabear754.kbo_scraper.api.domain

import jakarta.persistence.*
import jakarta.validation.constraints.Size

@Entity
@Table(uniqueConstraints = [UniqueConstraint(columnNames = ["season", "team"])])
class TeamSeasonRecord(
    @Column(nullable = false)
    val season: Int,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val team: Team,
    var teamRank: Int,
    var gamesPlayed: Int,
    var wins: Int,
    var losses: Int,
    var draws: Int,
    var winRate: Double,
    var gamesBehind: Double,
    @Size(max = 20)
    @Column(length = 20)
    var recent10Games: String,
    @Size(max = 20)
    @Column(length = 20)
    var streak: String
) : BaseEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val seq: Long? = null

    fun updateStats(other: TeamSeasonRecord) {
        this.teamRank = other.teamRank
        this.gamesPlayed = other.gamesPlayed
        this.wins = other.wins
        this.losses = other.losses
        this.draws = other.draws
        this.winRate = other.winRate
        this.gamesBehind = other.gamesBehind
        this.recent10Games = other.recent10Games
        this.streak = other.streak
    }
}