package com.muedsa.tvbox.jav.service

import com.muedsa.tvbox.api.data.MediaCard
import com.muedsa.tvbox.api.data.MediaCatalogConfig
import com.muedsa.tvbox.api.data.MediaCatalogOption
import com.muedsa.tvbox.api.data.PagingResult
import com.muedsa.tvbox.api.service.IMediaCatalogService
import com.muedsa.tvbox.jav.JavConsts
import com.muedsa.tvbox.tool.checkSuccess
import com.muedsa.tvbox.tool.feignChrome
import com.muedsa.tvbox.tool.get
import com.muedsa.tvbox.tool.parseHtml
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request

class MediaCatalogService(
    private val okHttpClient: OkHttpClient,
) : IMediaCatalogService {

    override suspend fun getConfig(): MediaCatalogConfig {
        return MediaCatalogConfig(
            initKey = "1",
            pageSize = 12,
            cardWidth = JavConsts.CARD_WIDTH,
            cardHeight = JavConsts.CARD_HEIGHT,
            catalogOptions = emptyList()
        )
    }

    override suspend fun catalog(
        options: List<MediaCatalogOption>,
        loadKey: String,
        loadSize: Int
    ): PagingResult<MediaCard> {
        val body = Request.Builder().url(
            "${JavConsts.SITE_BASE_URL}/search"
                .toHttpUrl()
                .newBuilder()
                .setQueryParameter("keyword", "")
                .setQueryParameter("page", loadKey)
                .build()
        ).feignChrome()
            .get(okHttpClient = okHttpClient)
            .checkSuccess()
            .parseHtml()
            .body()

        val navEl = body.selectFirst("#body >.container >div >nav")
        val prevUrl = navEl?.child(0)?.selectFirst("a")?.attr("href")
        val nextUrl = navEl?.child(2)?.selectFirst("a")?.attr("href")
        return PagingResult<MediaCard>(
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
            },
            nextKey = nextUrl?.let { PAGE_NUM_REGEX.find(it)?.groups[1]?.value },
            prevKey = prevUrl?.let { PAGE_NUM_REGEX.find(it)?.groups[1]?.value },
        )
    }

    companion object {
        val PAGE_NUM_REGEX = "page=(\\d+)".toRegex()
    }
}