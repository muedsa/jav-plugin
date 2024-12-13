package com.muedsa.tvbox.jav.service

import com.muedsa.tvbox.jav.TestPlugin
import com.muedsa.tvbox.jav.checkMediaCardRow
import kotlinx.coroutines.test.runTest
import org.junit.Test

class MediaSearchServiceTest {

    private val service = TestPlugin.provideMediaSearchService()

    @Test
    fun searchMedias_test() = runTest {
        val row = service.searchMedias("")
        checkMediaCardRow(row = row)
    }
}