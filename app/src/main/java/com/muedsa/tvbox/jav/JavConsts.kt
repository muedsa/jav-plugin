package com.muedsa.tvbox.jav

import com.muedsa.tvbox.tool.decodeBase64ToStr

object JavConsts {
    val SITE_BASE_URL = "aHR0cHM6Ly8xMjNhdi5jb20=".decodeBase64ToStr()
    const val LANG_PATH = "/cn/"
    const val VIDEO_PATH_PREFIX = "${LANG_PATH}v/"
    const val CARD_WIDTH = 240
    const val CARD_HEIGHT = 180
}