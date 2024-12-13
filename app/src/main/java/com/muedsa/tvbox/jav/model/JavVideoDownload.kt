package com.muedsa.tvbox.jav.model

import kotlinx.serialization.Serializable

@Serializable
data class JavVideoDownload(
    val host: String = "",
    val index: Int = 0,
    val name: String = "",
    val url: String = "",
)
