package com.muedsa.tvbox.jav.service

import com.muedsa.tvbox.api.data.MediaCard
import com.muedsa.tvbox.api.data.MediaCardRow
import com.muedsa.tvbox.api.service.IMediaSearchService
import com.muedsa.tvbox.jav.JavConsts
import com.muedsa.tvbox.tool.checkSuccess
import com.muedsa.tvbox.tool.feignChrome
import com.muedsa.tvbox.tool.get
import com.muedsa.tvbox.tool.parseHtml
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request

class MediaSearchService(
    private val okHttpClient: OkHttpClient,
) : IMediaSearchService {

    var lastQuery: String = ""

    override suspend fun searchMedias(query: String): MediaCardRow {
        lastQuery = query
        val body = Request.Builder().url(
            "${JavConsts.SITE_BASE_URL}/search"
                .toHttpUrl()
                .newBuilder()
                .setQueryParameter("keyword", query)
                .build()
        ).feignChrome()
            .get(okHttpClient = okHttpClient)
            .checkSuccess()
            .parseHtml()
            .body()
        return MediaCardRow(
            title = "search list",
            cardWidth = JavConsts.CARD_WIDTH,
            cardHeight = JavConsts.CARD_HEIGHT,
            list = body.select("#body .box-item-list .box-item").map { boxEl ->
                val aEl = boxEl.selectFirst(".thumb a")!!
                val id = aEl.attr("href")
                MediaCard(
                    id = id,
                    title = aEl.attr("title").trim(),
                    detailUrl = id,
                    coverImageUrl = aEl.selectFirst("img")!!.attr("data-src"),
                    subTitle = boxEl.selectFirst(".detail a")?.text()?.trim()
                )
            }
        )
    }
}