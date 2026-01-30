package com.colabear754.kbo_scraper

import com.colabear754.kbo_scraper.initializer.GameInfoInitializer
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.getBean
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.test.context.bean.override.mockito.MockitoBean

@SpringBootTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
class KboScraperApplicationTests {
    @Autowired
    private lateinit var applicationContext: ApplicationContext
    @MockitoBean
    private lateinit var gInfoInitializer: GameInfoInitializer

    @Test
    fun contextLoads() {
        val runner = applicationContext.getBean<GameInfoInitializer>()

        runner shouldNotBe null
    }
}
