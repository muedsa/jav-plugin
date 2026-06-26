package com.muedsa.tvbox.jav.service

import com.muedsa.tvbox.api.data.DanmakuData
import com.muedsa.tvbox.api.data.DanmakuDataFlow
import com.muedsa.tvbox.api.data.MediaCard
import com.muedsa.tvbox.api.data.MediaCardRow
import com.muedsa.tvbox.api.data.MediaDetail
import com.muedsa.tvbox.api.data.MediaEpisode
import com.muedsa.tvbox.api.data.MediaHttpSource
import com.muedsa.tvbox.api.data.MediaPlaySource
import com.muedsa.tvbox.api.data.MediaSniffingSource
import com.muedsa.tvbox.api.data.SavedMediaCard
import com.muedsa.tvbox.api.service.IMediaDetailService
import com.muedsa.tvbox.jav.JavConsts
import com.muedsa.tvbox.jav.model.ImgData
import com.muedsa.tvbox.tool.LenientJson
import com.muedsa.tvbox.tool.checkSuccess
import com.muedsa.tvbox.tool.feignChrome
import com.muedsa.tvbox.tool.get
import com.muedsa.tvbox.tool.parseHtml
import com.muedsa.tvbox.tool.toRequestBuild
import okhttp3.OkHttpClient

class MediaDetailService(
    private val okHttpClient: OkHttpClient,
) : IMediaDetailService {

    override suspend fun getDetailData(mediaId: String, detailUrl: String): MediaDetail {
        val pageUrl = "${JavConsts.SITE_BASE_URL}${JavConsts.VIDEO_PATH_PREFIX}$detailUrl"
        val body = pageUrl.toRequestBuild()
            .feignChrome()
            .get(okHttpClient = okHttpClient)
            .checkSuccess()
            .parseHtml()
            .body()
        val videoEl = body.selectFirst(".app main.feed .watch .watch-layout .watch-main .watch__main")!!
        val data = videoEl.attr("x-data")
        val dataResult = VIDEO_DATA_REGEX.find(data)!!
        val imgData = LenientJson.decodeFromString<List<ImgData>>(dataResult.groups[1]!!.value.replace("\\u0022", "\""))[0]
        val imgUrl = imgData.url.replace("\\/", "/")
        val titleArr = videoEl.selectFirst(".watch__head .watch__headinfo .watch__title")!!
            .text()
            .trim()
            .split(" — ")
        val title = titleArr[0]
        val subTitle = if (titleArr.size > 1) titleArr[1] else titleArr[0]
        val rows = mutableListOf<MediaCardRow>()
        val sideEl = body.selectFirst(".app main.feed .watch .watch-layout .watch-side")
        if (sideEl != null) {
            val rowTitle = sideEl.selectFirst(".watch-side__title")!!.text().trim()
            val cards = sideEl.select(".watch-side__list li a").map { aEl ->
                val cardId = aEl.attr("href").removePrefix(JavConsts.VIDEO_PATH_PREFIX)
                val cardTitleArr = aEl.selectFirst(".vside__body .vside__title")!!
                    .text()
                    .trim()
                    .split(" — ")
                val cardTitle = cardTitleArr[0]
                val cardSubTitle = if (cardTitleArr.size > 1) cardTitleArr[1] else cardTitleArr[0]
                MediaCard(
                    id = cardId,
                    title = cardTitle,
                    detailUrl = cardId,
                    subTitle = cardSubTitle,
                    coverImageUrl = aEl.selectFirst(".vside__thumb")!!.attr("style")
                        .removePrefix("background-image:url('")
                        .removeSuffix("')")
                )
            }
            rows.add(MediaCardRow(
                title = rowTitle,
                cardWidth = JavConsts.CARD_WIDTH,
                cardHeight = JavConsts.CARD_HEIGHT,
                list = cards,
            ))
        }
        return MediaDetail(
            id = mediaId,
            title = title,
            subTitle = subTitle,
            description = videoEl.select(".watch__block .watch__info .watch__info-row")
                .joinToString("\n") { rowEl ->
                    val label = rowEl.child(0).text().trim()
                    val descr = rowEl.child(1).text().trim()
                    "$label $descr"
                },
            detailUrl = detailUrl,
            backgroundImageUrl = imgUrl,
            playSourceList = listOf(
                MediaPlaySource(
                    id = "javplayer",
                    name = "javplayer",
                    episodeList = listOf(
                        MediaEpisode(
                            id = mediaId,
                            name = mediaId,
                            flag6 = pageUrl,
                        )
                    )
                )
            ),
            favoritedMediaCard = SavedMediaCard(
                id = mediaId,
                title = title,
                detailUrl = detailUrl,
                coverImageUrl = imgUrl,
                subTitle = subTitle,
                cardWidth = JavConsts.CARD_WIDTH,
                cardHeight = JavConsts.CARD_HEIGHT,
            ),
            rows = rows,
        )
    }

    override suspend fun getEpisodePlayInfo(
        playSource: MediaPlaySource,
        episode: MediaEpisode
    ): MediaHttpSource {
        return MediaSniffingSource(
            url = episode.flag6 ?: throw RuntimeException("解析播放地址失败"),
        )
    }

    override suspend fun getEpisodeDanmakuDataList(episode: MediaEpisode): List<DanmakuData>
        = emptyList()

    override suspend fun getEpisodeDanmakuDataFlow(episode: MediaEpisode): DanmakuDataFlow? = null

    companion object {
        val VIDEO_DATA_REGEX = "player\\(JSON.parse\\('(.*?)'\\), (\\d+), '(.*?)', '(.*?)', '(.*?)'\\)".toRegex()
    }
}