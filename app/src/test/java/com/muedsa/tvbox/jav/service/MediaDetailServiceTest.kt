package com.muedsa.tvbox.jav.service

import com.muedsa.tvbox.api.data.MediaCardType
import com.muedsa.tvbox.jav.TestOkHttpClient
import com.muedsa.tvbox.jav.checkMediaCard
import com.muedsa.tvbox.jav.checkMediaCardRow
import kotlinx.coroutines.test.runTest
import org.junit.Test

class MediaDetailServiceTest {

    private val service = MediaDetailService(
        okHttpClient = TestOkHttpClient,
    )

    @Test
    fun getDetailData_test() = runTest{
        val detail = service.getDetailData("dm1/v/vec-674", "dm1/v/vec-674")
        check(detail.id.isNotEmpty())
        check(detail.title.isNotEmpty())
        check(detail.detailUrl.isNotEmpty())
        check(detail.backgroundImageUrl.isNotEmpty())
        detail.favoritedMediaCard?.let { favoritedMediaCard ->
            checkMediaCard(favoritedMediaCard, cardType = MediaCardType.STANDARD)
            check(favoritedMediaCard.cardWidth > 0)
            check(favoritedMediaCard.cardHeight > 0)
        }
        check(detail.playSourceList.isNotEmpty())
        detail.playSourceList.forEach { mediaPlaySource ->
            check(mediaPlaySource.id.isNotEmpty())
            check(mediaPlaySource.name.isNotEmpty())
            check(mediaPlaySource.episodeList.isNotEmpty())
            mediaPlaySource.episodeList.forEach {
                check(it.id.isNotEmpty())
                check(it.name.isNotEmpty())
            }
        }
        detail.rows.forEach {
            checkMediaCardRow(it)
        }
    }

    @Test
    fun getEpisodePlayInfo_test() = runTest{
        val detail = service.getDetailData("dm1/v/vec-674", "dm1/v/vec-674")
        check(detail.playSourceList.isNotEmpty())
        check(detail.playSourceList.flatMap { it.episodeList }.isNotEmpty())
        val mediaPlaySource = detail.playSourceList[0]
        val mediaEpisode = mediaPlaySource.episodeList[0]
        val playInfo = service.getEpisodePlayInfo(mediaPlaySource, mediaEpisode)
        check(playInfo.url.isNotEmpty())
    }

}