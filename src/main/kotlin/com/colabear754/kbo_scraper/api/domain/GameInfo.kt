package com.colabear754.kbo_scraper.api.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.validation.constraints.Size
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@Entity
@EntityListeners(AuditingEntityListener::class)
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
    val awayTeam: Team,
    @Enumerated(EnumType.STRING)
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
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val gameId: Long? = null
    @CreatedDate
    lateinit var createdAt: LocalDateTime
        protected set
    @LastModifiedDate
    lateinit var modifiedAt: LocalDateTime
        protected set

    fun update(gameInfo: GameInfo): Boolean {
        val isUpdated = this.time?.equals(gameInfo.time) == false
                || this.awayScore != gameInfo.awayScore
                || this.homeScore != gameInfo.homeScore
                || this.stadium != gameInfo.stadium
                || this.relay != gameInfo.relay
                || this.gameStatus != gameInfo.gameStatus
                || this.cancellationReason != gameInfo.cancellationReason

        this.time = gameInfo.time
        this.awayScore = gameInfo.awayScore
        this.homeScore = gameInfo.homeScore
        this.stadium = gameInfo.stadium
        this.relay = gameInfo.relay
        this.gameStatus = gameInfo.gameStatus
        this.cancellationReason = gameInfo.cancellationReason

        return isUpdated
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is GameInfo) return false
        return gameKey == other.gameKey
    }

    override fun hashCode(): Int {
        return gameKey.hashCode()
    }
}