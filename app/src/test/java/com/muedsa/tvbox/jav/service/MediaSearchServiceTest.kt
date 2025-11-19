package com.muedsa.tvbox.jav.service

import com.muedsa.tvbox.jav.TestPlugin
import com.muedsa.tvbox.jav.checkMediaCardRow
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class MediaSearchServiceTest {

    private val service = TestPlugin.provideMediaSearchService()

    @Test
    fun searchMedias_test() = runTest {
        val row = service.searchMedias("")
        checkMediaCardRow(row = row)
    }
}