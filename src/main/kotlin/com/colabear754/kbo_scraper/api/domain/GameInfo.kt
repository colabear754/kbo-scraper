package com.colabear754.kbo_scraper.api.domain

import jakarta.persistence.*
import jakarta.validation.constraints.Size
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes
import kotlin.time.toKotlinDuration

@Entity
class GameInfo(
    @Size(max = 25)
    @Column(unique = true, nullable = false, updatable = false, length = 25)
    val gameKey: String,
    @Column(nullable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    val seriesType: SeriesType,
    @Column(nullable = false, updatable = false)
    val date: LocalDate,
    var time: LocalTime?,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val awayTeam: Team,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val homeTeam: Team,
    var awayScore: Int?,
    var homeScore: Int?,
    @Size(max = 100)
    @Column(length = 100)
    var relay: String?,
    @Size(max = 20)
    @Column(length = 20)
    var stadium: String,
    @Enumerated(EnumType.STRING)
    var gameStatus: GameStatus,
    @Enumerated(EnumType.STRING)
    var cancellationReason: CancellationReason?
) : BaseEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val gameId: Long? = null

    fun update(newGameInfo: GameInfo): Boolean {
        val isUpdated = this.time != newGameInfo.time
                || this.awayScore != newGameInfo.awayScore
                || this.homeScore != newGameInfo.homeScore
                || this.stadium != newGameInfo.stadium
                || this.relay != newGameInfo.relay
                || this.gameStatus != newGameInfo.gameStatus
                || this.cancellationReason != newGameInfo.cancellationReason

        if (!isUpdated) {
            return false
        }

        this.time = newGameInfo.time
        this.awayScore = newGameInfo.awayScore
        this.homeScore = newGameInfo.homeScore
        this.stadium = newGameInfo.stadium
        this.relay = newGameInfo.relay
        this.gameStatus = newGameInfo.gameStatus
        this.cancellationReason = newGameInfo.cancellationReason

        return true
    }

    fun isMyTeam(team: Team): Boolean {
        return awayTeam == team || homeTeam == team
    }

    fun isExpired(): Boolean {
        if (gameStatus == GameStatus.FINISHED || gameStatus == GameStatus.CANCELLED) {
            return false
        }
        val now = LocalDateTime.now()
        val timeoutMinutes = when {
            now >= LocalDateTime.of(date, time) -> 10L
            else -> 60L
        }
        return modifiedAt < now.minusMinutes(timeoutMinutes)
    }

    fun getCacheExpiration(): Long {
        val now = LocalDateTime.now()
        return when (gameStatus) {
            GameStatus.FINISHED, GameStatus.CANCELLED -> 1.days
            GameStatus.PLAYING -> 5.minutes
            GameStatus.SCHEDULED -> {
                val startTime = LocalDateTime.of(date, time)
                if (now >= startTime) 5.minutes
                else minOf(Duration.between(now, startTime).toKotlinDuration(), 60.minutes)
            }
        }.inWholeNanoseconds
    }

    override fun equals(other: Any?) =
        this === other || (other is GameInfo && gameKey == other.gameKey)

    override fun hashCode(): Int {
        return gameKey.hashCode()
    }
}