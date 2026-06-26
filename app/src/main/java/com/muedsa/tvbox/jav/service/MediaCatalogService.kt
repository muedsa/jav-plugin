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
import java.util.Calendar

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
                    name = "类型",
                    value = "type",
                    items = listOf(
                        MediaCatalogOptionItem(
                            name = "全部",
                            value = "",
                        ),
                        MediaCatalogOptionItem(
                            name = "有码",
                            value = "censored",
                        ),
                        MediaCatalogOptionItem(
                            name = "无码",
                            value = "uncensored",
                        ),
                        MediaCatalogOptionItem(
                            name = "无码泄露",
                            value = "uncensored-leaked",
                        ),
                    )
                ),
                MediaCatalogOption(
                    name = "年份",
                    value = "year",
                    items = buildList {
                        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
                        for (year in currentYear .. 2000) {
                            add(
                                MediaCatalogOptionItem(
                                    name = "$year",
                                    value = "$year",
                                )
                            )
                        }
                    }
                ),
                MediaCatalogOption(
                    name = "女演员",
                    value = "actress",
                    items = listOf(
                        MediaCatalogOptionItem(
                            name = "全部",
                            value = "",
                        ),
                        MediaCatalogOptionItem(
                            name = "单人",
                            value = "single",
                        ),
                        MediaCatalogOptionItem(
                            name = "多人",
                            value = "multi",
                        ),
                    )
                ),
                MediaCatalogOption(
                    name = "排序",
                    value = "sort",
                    items = listOf(
                        MediaCatalogOptionItem(
                            name = "发布日期",
                            value = "release_date",
                        ),
                        MediaCatalogOptionItem(
                            name = "最近添加",
                            value = "recent",
                        ),
                        MediaCatalogOptionItem(
                            name = "热门",
                            value = "hot",
                        ),
                        MediaCatalogOptionItem(
                            name = "今日观看",
                            value = "today",
                        ),
                        MediaCatalogOptionItem(
                            name = "每周观看",
                            value = "week",
                            defaultChecked = true,
                        ),
                        MediaCatalogOptionItem(
                            name = "每月观看",
                            value = "month",
                        ),
                        MediaCatalogOptionItem(
                            name = "最受欢迎",
                            value = "views",
                        ),
                        MediaCatalogOptionItem(
                            name = "最多关注",
                            value = "follows",
                        ),
                        MediaCatalogOptionItem(
                            name = "最长",
                            value = "longest",
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
        val type = options.find { it.value == "type" }?.items[0]?.value ?: ""
        val year = options.find { it.value == "year" }?.items[0]?.value ?: ""
        val actress = options.find { it.value == "actress" }?.items[0]?.value ?: ""
        val sort = options.find { it.value == "sort" }?.items[0]?.value ?: ""
        val body = Request.Builder().url(
            "${JavConsts.SITE_BASE_URL}${JavConsts.LANG_PATH}all"
                .toHttpUrl()
                .newBuilder()
                .setQueryParameter("keyword", mediaSearchService.lastQuery)
                .setQueryParameter("page", loadKey)
                .setQueryParameter("type", type)
                .setQueryParameter("year", year)
                .setQueryParameter("actress", actress)
                .setQueryParameter("sort", sort)
                .build()
        ).feignChrome()
            .get(okHttpClient = okHttpClient)
            .checkSuccess()
            .parseHtml()
            .body()

        val paginationEl = body.selectFirst(".app main.feed .pager")
        val prevUrl = paginationEl?.selectFirst(".pager__nav[rel=\"prev\"]")?.attr("href")
        val nextUrl = paginationEl?.selectFirst(".pager__nav[rel=\"next\"]")?.attr("href")
        return PagingResult(
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