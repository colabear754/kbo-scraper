package com.colabear754.kbo_scraper.api.domain.game

enum class Team(
    val teamName: String,
    val teamFullName: String
) {
    DOOSAN("두산", "두산 베어스"),
    SAMSUNG("삼성", "삼성 라이온즈"),
    LOTTE("롯데", "롯데 자이언츠"),
    HANWHA("한화", "한화 이글스"),
    LG("LG", "LG 트윈스"),
    KIA("KIA", "KIA 타이거즈"),
    HEROES("키움", "키움 히어로즈"),
    NC("NC", "NC 다이노스"),
    KT("KT", "KT 위즈"),
    SSG("SSG", "SSG 랜더스"),
    UNKNOWN("알수없음", "알수없음");

    companion object {
        fun findByTeamName(teamName: String) = entries.find { it.teamName == teamName } ?: UNKNOWN
    }
}