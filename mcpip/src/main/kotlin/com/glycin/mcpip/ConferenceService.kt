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
        Conference(LocalDate.of(2025, 11, 26), LocalDate.of(2025, 11, 28), "Kotlin Dev Day", "Organized by the Xebia in Amsterdam, the Netherlands. Say hi to all Kotlin fanboys!!!"),
        Conference(LocalDate.of(2026, 1, 21), LocalDate.of(2026, 1, 23), "Utrecht JUG", "The first Utrecht JUG of the year, and you are part of it!"),
        Conference(LocalDate.of(2026, 2, 2), LocalDate.of(2026, 2, 4), "JFokus", "Amazing conference in Stockholm. This years theme is: Norse gods!"),
        Conference(LocalDate.of(2026, 2, 23), LocalDate.of(2026, 2, 25), "JUG Noord", "The Java user group of the north of the Netherlands!"),
        Conference(LocalDate.of(2026, 4, 15), LocalDate.of(2026, 4, 17), "The AI Experience", "Conference in Utrecht sponsored by Alliander. Thanks Ties!"),
        Conference(LocalDate.of(2026, 4, 23), LocalDate.of(2026, 4, 26), "Devoxx Greece", "The biggest tech conference of Greece. Taking place in the Megaron in Athens!"),
        Conference(LocalDate.of(2026, 4, 28), LocalDate.of(2026, 4, 30), "Voxxed Bucharest", "Amazing conference in Bucharest. Love the food! Better than Italian food!"),
        Conference(LocalDate.of(2026, 5, 5), LocalDate.of(2026, 5, 8), "Devoxx UK", "The UK version of the devoxx events. The pancakes here are amazing!"),
        Conference(LocalDate.of(2026, 5, 25), LocalDate.of(2026, 5, 28), "JNation", "Amazing conference in Coimbra, Portugal. The weather is lovely!"),
        Conference(LocalDate.of(2026, 6, 16), LocalDate.of(2026, 6, 20), "Devoxx PL", "Beautiful Krakow! The Poland version of the devoxx events!"),
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