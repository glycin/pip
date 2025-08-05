package com.glycin.pipscripts.slack

import com.fasterxml.jackson.annotation.JsonProperty

data class SlackMessage(
    @JsonProperty("client_msg_id") val clientMsgId: String? = null,
    val type: String,
    val user: String,
    val text: String? = "",
    val ts: String,
    val team: String? = null,
    @JsonProperty("replace_original")
    val replaceOriginal: Boolean = false,
    @JsonProperty("delete_original")
    val deleteOriginal: Boolean = false,
    val metadata: Metadata? = null,
    //val blocks: List<Blocks>? = emptyList(),
    @JsonProperty("user_team")
    val userTeam: String? = null,
    @JsonProperty("source_team")
    val sourceTeam: String? = null,
    @JsonProperty("user_profile")
    val userProfile: UserProfile
)

data class Metadata(
    @JsonProperty("event_type")
    val eventType: String,
    @JsonProperty("event_payload")
    val eventPayload: String? = null
)

data class Blocks(
    val type: String,
    @JsonProperty("block_id")
    val blockId: String,
    val elements: List<Elements>
)

data class Elements(
    val type: String,
    val text: String? = null,
    val url: String? = null,
    //val elements: List<Elements>? = null, // Let's not parse the recursive part for now
    val style: Map<String, Any>? = null,
    val name: String? = null,
    val unicode: String? = null
)

data class UserProfile(
    @JsonProperty("avatar_hash")
    val avatarHash: String,
    @JsonProperty("image_72")
    val image72: String,
    @JsonProperty("first_name")
    val firstName: String,
    @JsonProperty("real_name")
    val realName: String,
    @JsonProperty("display_name")
    val displayName: String,
    val team: String,
    val name: String,
    @JsonProperty("is_restricted")
    val isRestricted: Boolean,
    @JsonProperty("is_ultra_restricted")
    val isUltraRestricted: Boolean
)