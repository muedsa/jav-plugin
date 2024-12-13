package com.muedsa.tvbox.jav.model

import kotlinx.serialization.Serializable

@Serializable
data class JavPlayerVideoInfo(
    val stream: String = "",
    val vtt: String? = null,
)