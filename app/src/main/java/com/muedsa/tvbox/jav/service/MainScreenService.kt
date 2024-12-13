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
        val body = "${JavConsts.SITE_BASE_URL}/dm1/".toRequestBuild()
            .feignChrome()
            .get(okHttpClient = okHttpClient)
            .checkSuccess()
            .parseHtml()
            .body()
        val rows = mutableListOf<MediaCardRow>()
        val topCards = body.select("#body #top-carousel .box-item-list .box-item").map { boxEl ->
            val aEl = boxEl.selectFirst("a[href]")!!
            val id = aEl.attr("href")
            MediaCard(
                id = id,
                title = aEl.selectFirst(".name")!!.text().trim(),
                detailUrl = id,
                coverImageUrl = aEl.selectFirst("img")!!.attr("src")
            )
        }
        if (topCards.isNotEmpty()) {
            rows.add(
                MediaCardRow(
                    title = "精选",
                    cardWidth = JavConsts.CARD_WIDTH,
                    cardHeight = JavConsts.CARD_HEIGHT,
                    list = topCards,
                )
            )
        }
        body.select("#body .container >section").forEach { sectionEl ->
            val rowTitle = sectionEl.selectFirst(".section-title .title h2")!!.text()
            rows.add(
                MediaCardRow(
                    title = rowTitle,
                    cardWidth = JavConsts.CARD_WIDTH,
                    cardHeight = JavConsts.CARD_HEIGHT,
                    list = sectionEl.select(".box-item-list .box-item").map { boxEl ->
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
                )
            )
        }
        return rows
    }
}