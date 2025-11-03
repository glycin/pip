package com.glycin.mcpip

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.neovisionaries.i18n.CountryCode
import org.slf4j.LoggerFactory
import org.springframework.ai.tool.annotation.Tool
import org.springframework.ai.tool.annotation.ToolParam
import org.springframework.stereotype.Service
import se.michaelthelin.spotify.SpotifyApi
import se.michaelthelin.spotify.SpotifyHttpManager
import se.michaelthelin.spotify.model_objects.specification.Track
import kotlin.random.Random

private const val PLAYLIST_ID = "spotify:playlist:4agDUCts2VBH5yBr20YUSB"
private const val DEVICE_NAME = "GLYCINWORK"

@Service
class SpotifyService(
    private val spotifyProperties: SpotifyProperties,
) {
    private val log = LoggerFactory.getLogger(SpotifyService::class.java)

    private lateinit var spotifyApi: SpotifyApi
    private var inititalized = false

    // Auth request to create a new refresh token
    /*private val authRequest = spotifyApi
        .authorizationCodeUri()
        .scope(AuthorizationScope.USER_MODIFY_PLAYBACK_STATE, AuthorizationScope.USER_READ_PLAYBACK_STATE, AuthorizationScope.USER_READ_CURRENTLY_PLAYING)
        .build()
        .execute()

    private val authCodeRequest = spotifyApi.authorizationCode("ADD TOKEN FROM MANUAL AUTH").build().execute()
    */

    /*init {

        println(authCodeRequest.refreshToken)

    }*/

    private fun initialize(){
        if(inititalized) return

        spotifyApi = SpotifyApi.builder()
            .setClientId(spotifyProperties.clientId)
            .setClientSecret(spotifyProperties.clientSecret)
            .setRedirectUri(SpotifyHttpManager.makeUri(spotifyProperties.redirectUrl))
            .setRefreshToken(spotifyProperties.refreshToken)
            .build()

        // Use refresh token for a new access token
        spotifyApi!!.authorizationCodeRefresh().build().execute().also {
            spotifyApi!!.accessToken = it.accessToken
        }

        inititalized = true
    }

    @Tool(description = "Play a song. Input is the name of the song")
    fun playSong(
        @ToolParam(description = "The name of the song to be played") songName: String,
    ): String {
        initialize()
        log.info("PLAY SONG TOOL INVOKED => Trying to find song: $songName")
        return searchSong(songName)?.let { track ->
            playTrack(track)
        } ?: "Could not find a song with name $songName to play"
    }

    @Tool(description = "Play music from an artist. Input is the name of the artist")
    fun playArtist(
        @ToolParam(description = "The name of the artist to play music from") artistName: String,
    ): String {
        initialize()
        log.info("PLAY ARTIST TOOL INVOKED => Trying to find song by artist: $artistName")
        return searchSong(songName = null, artistName = artistName)?.let { track ->
            playTrack(track)
        } ?: "Could not find a song from artist $artistName"
    }

    @Tool(description = "Play a song by a specific artist. Input is the name of the song and the name of the artist")
    fun playSongFromArtist(
        @ToolParam(description = "The name of the song to be played") songName: String,
        @ToolParam(description = "The name of the artist that performs the song") artistName: String,
    ): String {
        initialize()
        log.info("PLAY SONG BY ARTIST TOOL INVOKED => Trying to find song $songName by artist: $artistName")
        return searchSong(songName, artistName)?.let { track ->
            playTrack(track)
        } ?: "Could not find a song $songName from artist $artistName"
    }

    @Tool(description = "Start playing from the metal playlist")
    fun playFromMetal(): String {
        initialize()
        log.info("PLAY METAL PLAYLIST TOOL INVOKED")
        val randomOffset = Random.nextInt(0, 300)
        val devices = spotifyApi.usersAvailableDevices.build().execute()
        return devices.takeIf { !it.isNullOrEmpty() }
            ?.first()
            ?.let {
                val offsetObj = JsonObject()
                offsetObj.addProperty("position", randomOffset)
                spotifyApi.startResumeUsersPlayback()
                    .context_uri(PLAYLIST_ID)
                    .offset(offsetObj)
                    .build()
                    .execute()
                "Music is now playing from the metal playlist."
            } ?: "Could not play music. Something went wrong."
    }

    private fun searchSong(songName: String? = null, artistName: String? = null): Track? {
        if(songName == null && artistName == null) {
            log.info("Could not search because there was no song or artist given")
            return null
        }

        val trackQuery = songName?.let { song ->
            "track:$song"
        } ?: ""
        val artistQuery = artistName?.let { artist ->
            "artist:$artist"
        } ?: ""

        log.info("Searching for song using query: $trackQuery $artistQuery")
        return spotifyApi.searchTracks("$trackQuery $artistQuery")
            .market(CountryCode.EU)
            .limit(1)
            .offset(0)
            .build()
            .execute()
            .items
            .toList()
            .firstOrNull()?.also {
                log.info("Found a song to play ${it.name} by ${it.artists.joinToString { a -> a.name }}")
            }
    }

    private fun playTrack(track: Track): String {
        val devices = spotifyApi.usersAvailableDevices.build().execute()
        return devices.takeIf { !it.isNullOrEmpty() }
            ?.firstOrNull { it.name == DEVICE_NAME }
            ?.let { device ->
                spotifyApi.startResumeUsersPlayback()
                    .device_id(devices.first().id)
                    .uris(JsonParser.parseString("[\"${track.uri}\"]").asJsonArray)
                    .build()
                    .execute()
                log.info("Now playing ${track.name} on ${device.name}")
                "Now playing ${track.name}, performed by ${track.artists.joinToString { it.name }}, from the album ${track.album.name} released in ${track.album.releaseDate}"
            } ?: "Could not find an active device to play song ${track.name}"
    }
}