package com.glycin.mcpip

import org.slf4j.LoggerFactory
import org.springframework.ai.tool.annotation.Tool
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class ConferenceService {
    private val log = LoggerFactory.getLogger(ConferenceService::class.java)

    private val conferences = listOf(
        Conference(LocalDate.of(2025, 9, 23), LocalDate.of(2025, 10, 1), "Test", "This be a test conference yarr!"),
        Conference(LocalDate.of(2025, 10, 6), LocalDate.of(2025, 10, 10), "Devoxx", "The one that started it all in Antwerp Belgium, founded by Stephan Janssen"),
        Conference(LocalDate.of(2025, 11, 5), LocalDate.of(2025, 11, 7), "JFall", "Organized by the NLJUG in Ede, the Netherlands. Say hi to your colleagues!"),
        Conference(LocalDate.of(2026, 2, 2), LocalDate.of(2026, 2, 4), "JFokus", "Amazing conference in Stockholm. This years theme is: Norse gods!")
    )

    @Tool(description = "Get the current conference where this agent is being presented")
    fun conferenceGetCurrent(): String {
        log.info("GET CURRENT CONFERENCE INVOKED")
        val currentDate = LocalDate.now()
        val conf = conferences.firstOrNull {
            currentDate in it.from..it.to
        }

        return if(conf == null) {
            "You are not being presented at a conference right now..."
        } else {
            "You are being presented at ${conf.name}. This conference is ${conf.description}"
        }
    }
}

data class Conference(
    val from: LocalDate,
    val to: LocalDate,
    val name: String,
    val description: String,
)