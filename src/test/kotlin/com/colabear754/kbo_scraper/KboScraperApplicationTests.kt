package com.colabear754.kbo_scraper

import org.junit.jupiter.api.Test
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
class KboScraperApplicationTests {

    @Test
    fun contextLoads() {
    }

}
