package com.muedsa.tvbox.jav.model

import kotlinx.serialization.Serializable

@Serializable
data class JavResp<T>(
    val status: Int = 0,
    val data: T? = null,
)