package com.colabear754.kbo_scraper

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

@EnableJpaAuditing
@EnableCaching
@ConfigurationPropertiesScan
@SpringBootApplication
class KboScraperApplication

fun main(args: Array<String>) {
    runApplication<KboScraperApplication>(*args)
}
