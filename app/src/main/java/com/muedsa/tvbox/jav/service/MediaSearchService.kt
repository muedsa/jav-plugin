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
            "${JavConsts.SITE_BASE_URL}${JavConsts.LANG_PATH}search"
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
            list = body.select(".app main.feed .grid .card").map { cardEl ->
                val aEl = cardEl.selectFirst(".card__poster a[href]")!!
                val id = aEl.attr("href").removePrefix(JavConsts.VIDEO_PATH_PREFIX)
                val titleArr = cardEl.selectFirst(".card__body .card__title")!!.text().split(" — ")
                val title = titleArr[0]
                val subTitle = if (titleArr.size > 1) titleArr[1] else ""
                MediaCard(
                    id = id,
                    title = title,
                    detailUrl = id,
                    coverImageUrl = aEl.selectFirst("img.card__img")!!.attr("src"),
                    subTitle = subTitle
                )
            }
        )
    }
}