package com.glycin.mcpip

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.neovisionaries.i18n.CountryCode
import org.springframework.ai.tool.annotation.Tool
import org.springframework.ai.tool.annotation.ToolParam
import org.springframework.stereotype.Service
import se.michaelthelin.spotify.SpotifyApi
import se.michaelthelin.spotify.SpotifyHttpManager
import kotlin.random.Random

private const val PLAYLIST_ID = "spotify:playlist:4agDUCts2VBH5yBr20YUSB"

@Service
class SpotifyService(
    spotifyProperties: SpotifyProperties,
) {

    private val spotifyApi = SpotifyApi.builder()
        .setClientId(spotifyProperties.clientId)
        .setClientSecret(spotifyProperties.clientSecret)
        .setRedirectUri(SpotifyHttpManager.makeUri(spotifyProperties.redirectUrl))
        .setRefreshToken(spotifyProperties.refreshToken)
        .build()

    // Auth request to create a new refresh token
    /*private val authRequest = spotifyApi
        .authorizationCodeUri()
        .scope(AuthorizationScope.USER_MODIFY_PLAYBACK_STATE, AuthorizationScope.USER_READ_PLAYBACK_STATE, AuthorizationScope.USER_READ_CURRENTLY_PLAYING)
        .build()
        .execute()

    private val authCodeRequest = spotifyApi.authorizationCode("ADD TOKEN FROM MANUAL AUTH").build().execute()
    */

    init {
        // Use refresh token for a new access token
        spotifyApi.authorizationCodeRefresh().build().execute().also {
            spotifyApi.accessToken = it.accessToken
        }

        /*
        println(authCodeRequest.refreshToken)
         */
    }
    
    @Tool(description = "Play a song. Input is the name of the song")
    fun playSong(
        @ToolParam(description = "The name of the song to be played") songName: String,
    ): String {
        val devices = spotifyApi.usersAvailableDevices.build().execute()
        return devices.takeIf { !it.isNullOrEmpty() }
            ?.first()
            ?.let {
                val tracks = spotifyApi.searchTracks(songName)
                    .market(CountryCode.EU)
                    .limit(1)
                    .offset(0)
                    .build()
                    .execute()
                    .items
                    .toList()

                tracks.takeIf { it.isNotEmpty() }
                    ?.first()
                    ?.let { track ->
                        spotifyApi.startResumeUsersPlayback()
                            .device_id(devices.first().id)
                            .uris(JsonParser.parseString("[\"${track.uri}\"]").asJsonArray)
                            .build()
                            .execute()
                        "Now playing ${track.name}, performed by ${track.artists.joinToString { it.name }}, from the album ${track.album.name} released in ${track.album.releaseDate}"
                    } ?: "Could not find a song with name $songName to play"
            } ?: "Could not find an active device to play song $songName"
    }

    @Tool(description = "Start playing from the metal playlist")
    fun playFromMetal(): String {
        val randomOffset = Random.nextInt(0, 300)
        val devices = spotifyApi.usersAvailableDevices.build().execute()
        devices.takeIf { !it.isNullOrEmpty() }
            ?.first()
            ?.let {
                val offsetObj = JsonObject()
                offsetObj.addProperty("position", randomOffset)
                spotifyApi.startResumeUsersPlayback()
                    .context_uri(PLAYLIST_ID)
                    .offset(offsetObj)
                    .build()
                    .execute()
            } ?: return "Could not play music. Something went wrong."
        return "Music is now playing from the metal playlist."
    }
}