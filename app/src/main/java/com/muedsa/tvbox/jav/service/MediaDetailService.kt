package com.muedsa.tvbox.jav.service

import com.muedsa.tvbox.api.data.DanmakuData
import com.muedsa.tvbox.api.data.DanmakuDataFlow
import com.muedsa.tvbox.api.data.MediaCardRow
import com.muedsa.tvbox.api.data.MediaDetail
import com.muedsa.tvbox.api.data.MediaEpisode
import com.muedsa.tvbox.api.data.MediaHttpSource
import com.muedsa.tvbox.api.data.MediaPlaySource
import com.muedsa.tvbox.api.data.SavedMediaCard
import com.muedsa.tvbox.api.service.IMediaDetailService
import com.muedsa.tvbox.jav.JavConsts
import com.muedsa.tvbox.jav.model.JavPlayerVideoInfo
import com.muedsa.tvbox.jav.model.JavResp
import com.muedsa.tvbox.jav.model.JavVideos
import com.muedsa.tvbox.tool.ChromeUserAgent
import com.muedsa.tvbox.tool.LenientJson
import com.muedsa.tvbox.tool.checkSuccess
import com.muedsa.tvbox.tool.feignChrome
import com.muedsa.tvbox.tool.get
import com.muedsa.tvbox.tool.parseHtml
import com.muedsa.tvbox.tool.stringBody
import com.muedsa.tvbox.tool.toRequestBuild
import okhttp3.OkHttpClient

class MediaDetailService(
    private val okHttpClient: OkHttpClient,
) : IMediaDetailService {

    override suspend fun getDetailData(mediaId: String, detailUrl: String): MediaDetail {
        val pageUrl = "${JavConsts.SITE_BASE_URL}/$mediaId"
        val body = pageUrl.toRequestBuild()
            .feignChrome()
            .get(okHttpClient = okHttpClient)
            .checkSuccess()
            .parseHtml()
            .body()
        val videoEl = body.selectFirst("#app #body #page-video")!!
        val longTitle = videoEl.selectFirst(">.row >.col >.d-flex >.mr-3 >h1")!!.text().trim()
        var title = longTitle.split(" ")[0]
        val imgUrl = videoEl.selectFirst("#player")!!.attr("data-poster")
        val result = MOVIE_INFO_REGEX.find(videoEl.attr("v-scope"))
        val mediaPlaySources = mutableListOf<MediaPlaySource>()
        if (result != null && result.groups.size > 2) {
            val movieId = result.groups[1]?.value!!
            title = result.groups[2]?.value!!
            val respJson = "${JavConsts.SITE_BASE_URL}/ajax/v/$movieId/videos".toRequestBuild()
                .feignChrome(referer = pageUrl)
                .get(okHttpClient = okHttpClient)
                .checkSuccess()
                .stringBody()
            val javVideos = LenientJson.decodeFromString<JavResp<JavVideos>>(respJson)
            val watch = javVideos.result?.watch ?: javVideos.data?.watch
            if (javVideos.status == 200 && watch?.isNotEmpty() == true) {
                mediaPlaySources.add(
                    MediaPlaySource(
                        id = "javplayer",
                        name = "javplayer",
                        episodeList = watch.map {
                            MediaEpisode(
                                id = "$movieId:${it.name}",
                                name = "部分 ${it.name}",
                                flag5 = it.url,
                                flag6 = pageUrl,
                            )
                        }
                    )
                )
            }
        }
        val rows = mutableListOf<MediaCardRow>()
        return MediaDetail(
            id = mediaId,
            title = title,
            subTitle = longTitle,
            description = videoEl.select("#details .content .detail-item >div")
                .joinToString("\n") { divEl ->
                    val label = divEl.child(0).text().trim()
                    val descr = divEl.child(1).text()
                    "$label $descr"
                },
            detailUrl = detailUrl,
            backgroundImageUrl = imgUrl,
            playSourceList = mediaPlaySources,
            favoritedMediaCard = SavedMediaCard(
                id = mediaId,
                title = title,
                detailUrl = detailUrl,
                coverImageUrl = imgUrl,
                subTitle = longTitle,
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
        val url = episode.flag5 ?: throw RuntimeException("解析播放地址失败")
        val referer = episode.flag6 ?: throw RuntimeException("解析播放地址失败")
        val body = url.toRequestBuild()
            .feignChrome(referer = referer)
            .get(okHttpClient = okHttpClient)
            .checkSuccess()
            .parseHtml()
            .body()
        val videoInfoJson = VIDEO_INFO_REGEX.find(body.selectFirst("#player")!!.attr("v-scope"))?.groups[2]?.value
            ?: throw RuntimeException("解析播放地址失败")
        val videoInfo = LenientJson.decodeFromString<JavPlayerVideoInfo>(videoInfoJson)
        return MediaHttpSource(
            url = videoInfo.stream,
            httpHeaders = mapOf(
                "Referer" to url,
                "User-Agent" to ChromeUserAgent,
            ),
        )
    }

    override suspend fun getEpisodeDanmakuDataList(episode: MediaEpisode): List<DanmakuData>
        = emptyList()

    override suspend fun getEpisodeDanmakuDataFlow(episode: MediaEpisode): DanmakuDataFlow? = null

    companion object {
        val MOVIE_INFO_REGEX = "Movie\\(\\{id: (\\d+), code: '(.*?)'\\}\\)".toRegex()
        val VIDEO_INFO_REGEX = "Video\\((\\d+), (\\{.*?\\})\\)".toRegex()
//        fun unescapeHtml(input: String): String {
//            return input
//                .replace("&quot;", "\"")
//                .replace("&apos;", "'")
//                .replace("&amp;", "&")
//                .replace("&lt;", "<")
//                .replace("&gt;", ">")
//                .replace("&nbsp;", " ")
//                .replace("&#34;", "\"")
//                .replace("&#39;", "'")
//                .replace("&#38;", "&")
//                .replace("&#60;", "<")
//                .replace("&#62;", ">")
//                .replace("&#160;", " ")
//        }

    }
}