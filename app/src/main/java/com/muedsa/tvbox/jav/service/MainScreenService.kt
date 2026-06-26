package com.muedsa.tvbox.jav.service

import com.muedsa.tvbox.api.data.MediaCard
import com.muedsa.tvbox.api.data.MediaCardRow
import com.muedsa.tvbox.api.service.IMainScreenService
import com.muedsa.tvbox.jav.JavConsts
import com.muedsa.tvbox.tool.checkSuccess
import com.muedsa.tvbox.tool.feignChrome
import com.muedsa.tvbox.tool.get
import com.muedsa.tvbox.tool.parseHtml
import com.muedsa.tvbox.tool.toRequestBuild
import okhttp3.OkHttpClient

class MainScreenService(
    private val okHttpClient: OkHttpClient,
) : IMainScreenService {

    override suspend fun getRowsData(): List<MediaCardRow> {
        val body = "${JavConsts.SITE_BASE_URL}${JavConsts.LANG_PATH}".toRequestBuild()
            .feignChrome()
            .get(okHttpClient = okHttpClient)
            .checkSuccess()
            .parseHtml()
            .body()
        val rows = mutableListOf<MediaCardRow>()
        body.select("main.feed .wrap section").forEach { sectionEl ->
            if (!sectionEl.hasClass("rec")) {
                val swiperEl = sectionEl.selectFirst(".swiper")
                if (swiperEl != null) {
                    val topCards = swiperEl.select(".swiper-wrapper .swiper-slide").map { slideEl ->
                        val aEl = slideEl.selectFirst(".featured .card__link")!!
                        val id = aEl.attr("href").removePrefix(JavConsts.VIDEO_PATH_PREFIX)
                        MediaCard(
                            id = id,
                            title = aEl.selectFirst(".featured__title")!!.text().trim(),
                            detailUrl = id,
                            coverImageUrl = slideEl.selectFirst(".featured .featured__poster img.featured__img")!!.attr("src")
                        )
                    }
                    rows.add(
                        MediaCardRow(
                            title = "精选",
                            cardWidth = JavConsts.CARD_WIDTH,
                            cardHeight = JavConsts.CARD_HEIGHT,
                            list = topCards,
                        )
                    )
                } else {
                    val rowTitleEl = sectionEl.selectFirst(".section__head .section__title")
                    if (rowTitleEl != null) {
                        val rowTitle = rowTitleEl.text().trim()
                        rows.add(
                            MediaCardRow(
                                title = rowTitle,
                                cardWidth = JavConsts.CARD_WIDTH,
                                cardHeight = JavConsts.CARD_HEIGHT,
                                list = sectionEl.select(".grid .card").map { cardEl ->
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
                            )
                        )
                    }
                }
            }
        }
        return rows
    }
}