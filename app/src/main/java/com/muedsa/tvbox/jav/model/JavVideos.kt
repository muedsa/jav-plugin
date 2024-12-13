package com.muedsa.tvbox.jav.model

import kotlinx.serialization.Serializable

@Serializable
data class JavVideos(
    val watch: List<JavVideoWatch> = emptyList(),
    val download: List<JavVideoDownload> = emptyList(),
)
