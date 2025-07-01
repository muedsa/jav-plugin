package com.muedsa.tvbox.jav.service

import com.muedsa.tvbox.api.data.MediaCard
import com.muedsa.tvbox.api.data.MediaCatalogConfig
import com.muedsa.tvbox.api.data.MediaCatalogOption
import com.muedsa.tvbox.api.data.MediaCatalogOptionItem
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
    private val mediaSearchService: MediaSearchService,
) : IMediaCatalogService {

    override suspend fun getConfig(): MediaCatalogConfig {
        return MediaCatalogConfig(
            initKey = "1",
            pageSize = 12,
            cardWidth = JavConsts.CARD_WIDTH,
            cardHeight = JavConsts.CARD_HEIGHT,
            catalogOptions = listOf(
                MediaCatalogOption(
                    name = "排序",
                    value = "sort",
                    items = listOf(
                        MediaCatalogOptionItem(
                            name = "默认",
                            value = "",
                        ),
                        MediaCatalogOptionItem(
                            name = "发布日期",
                            value = "release_date",
                        ),
                        MediaCatalogOptionItem(
                            name = "最近更新",
                            value = "recent_update",
                        ),
                        MediaCatalogOptionItem(
                            name = "热门",
                            value = "trending",
                        ),
                        MediaCatalogOptionItem(
                            name = "今天最多观看",
                            value = "most_viewed_today",
                            defaultChecked = true,
                        ),
                        MediaCatalogOptionItem(
                            name = "本周最多观看",
                            value = "most_viewed_week",
                        ),
                        MediaCatalogOptionItem(
                            name = "本月最多观看",
                            value = "most_viewed_month",
                        ),
                        MediaCatalogOptionItem(
                            name = "最多观看",
                            value = "most_viewed",
                        ),
                        MediaCatalogOptionItem(
                            name = "最受欢迎",
                            value = "most_favourited",
                        ),
                    ),
                    required = true,
                )
            )
        )
    }

    override suspend fun catalog(
        options: List<MediaCatalogOption>,
        loadKey: String,
        loadSize: Int
    ): PagingResult<MediaCard> {
        val sort = options.find { it.value === "sort" }?.items[0]?.value ?: ""
        val body = Request.Builder().url(
            "${JavConsts.SITE_BASE_URL}/search"
                .toHttpUrl()
                .newBuilder()
                .setQueryParameter("keyword", mediaSearchService.lastQuery)
                .setQueryParameter("page", loadKey)
                .setQueryParameter("sort", sort)
                .build()
        ).feignChrome()
            .get(okHttpClient = okHttpClient)
            .checkSuccess()
            .parseHtml()
            .body()

        val paginationEl = body.selectFirst("#page-nav .navigation .pagination")
        val prevUrl = paginationEl?.selectFirst(".page-item .page-link[rel=\"prev\"]")?.attr("href")
        val nextUrl = paginationEl?.selectFirst(".page-item .page-link[rel=\"next\"]")?.attr("href")
        return PagingResult(
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
            nextKey = nextUrl?.let { PAGE_NUM_REGEX.find(it)?.groups[1]?.value } ?: getNextNum(
                loadKey
            ),
            prevKey = prevUrl?.let { PAGE_NUM_REGEX.find(it)?.groups[1]?.value } ?: getPrevNum(
                loadKey
            ),
        )
    }

    companion object {
        val PAGE_NUM_REGEX = "page=(\\d+)".toRegex()

        fun getPrevNum(current: String): String? {
            return current.toIntOrNull()?.let {
                if (it > 1) "${it - 1}" else null
            }
        }

        fun getNextNum(current: String): String? {
            return current.toIntOrNull()?.let {
                "${it + 1}"
            }
        }
    }
}