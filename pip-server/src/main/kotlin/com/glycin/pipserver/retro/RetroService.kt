package com.glycin.pipserver.retro

import com.glycin.pipserver.util.NanoId
import com.glycin.pipserver.util.withoutThinkTags
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.memory.ChatMemory
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service

private val LOG = KotlinLogging.logger {}

@Service
class RetroService(
    @param:Qualifier("pip_toolless") private val pipToolless: ChatClient,
) {

    private val retroSystemPrompt = """
        You are the DOOM AI, assuming the role of Demon Slayer in a grid environment represented by ASCII characters. 
        Understand each character as follows: 
            E: Enemy, 
            P: Player, 
            B: Bullet, 
            W: Wall, 
            F: Floor, 
            A: Armor Bonus, 
            Z: Zombieman, 
            H: Health Bonus, 
            S: Stimpack. 
        Your task is to interpret the grid and choose an appropriate action from the following options: 
            ATTACK,
            MOVE_RIGHT,
            MOVE_LEFT,
            MOVE_BACKWARD,
            MOVE_FORWARD,
            TURN_RIGHT,
            TURN_LEFT,
            SELECT_NEXT_WEAPON,
            SELECT_PREV_WEAPON
        You are the Player.
        If you see an enemy move so that they are in the middle of the grid. If they are in the middle of the grid, attack them.
        If you don't see an enemy move around until you see one.
        If you are surrounded by Walls, turn around until you see the floor.
        IMPORTANT: Your responses must exclusively be your chosen action.
    """.trimIndent()

    fun play(requestBody: RetroRequestBody): RetroPlayResponse {
        //LOG.info { "Playing doom!" }
        //LOG.info { requestBody.grid }
        val action = pipToolless
                .prompt(Prompt(requestBody.grid))
                .system("$retroSystemPrompt /no_think")
                .advisors { it.param(ChatMemory.CONVERSATION_ID, NanoId.generate()) }
                .call()
                .content()

        val trimmed = action?.withoutThinkTags() ?: "ATTACK"
        LOG.info { "Playing doom, now $trimmed" }
        return RetroPlayResponse(trimmed)
    }
}