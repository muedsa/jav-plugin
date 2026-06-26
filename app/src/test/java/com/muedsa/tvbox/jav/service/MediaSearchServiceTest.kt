package com.muedsa.tvbox.jav.service

import com.muedsa.tvbox.jav.TestOkHttpClient
import com.muedsa.tvbox.jav.checkMediaCardRow
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class MediaSearchServiceTest {

    private val service = MediaSearchService(
        okHttpClient = TestOkHttpClient,
    )

    @Test
    fun searchMedias_test() = runTest {
        val row = service.searchMedias("123")
        checkMediaCardRow(row = row)
    }
}