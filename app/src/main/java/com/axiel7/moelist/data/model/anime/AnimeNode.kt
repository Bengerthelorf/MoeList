package com.axiel7.moelist.data.model.anime

import com.axiel7.moelist.data.model.media.MainPicture
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AnimeNode(
    @SerialName("id")
    val id: Int,
    @SerialName("title")
    val title: String,
    @SerialName("main_picture")
    val mainPicture: MainPicture? = null,
    @SerialName("start_season")
    val startSeason: StartSeason? = null,
    @SerialName("num_episodes")
    val numEpisodes: Int? = null,
    @SerialName("num_list_users")
    val numListUsers: Int? = null,
    @SerialName("media_type")
    val mediaType: String? = null,
    @SerialName("status")
    val status: String? = null,
    @SerialName("mean")
    val mean: Float? = null,
)