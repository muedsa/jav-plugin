package com.muedsa.tvbox.jav.model

import kotlinx.serialization.Serializable

@Serializable
data class JavVideoWatch(
    val index: Int = 0,
    val name: String = "",
    val url: String = ""
)