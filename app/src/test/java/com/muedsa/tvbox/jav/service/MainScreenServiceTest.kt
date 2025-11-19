package com.muedsa.tvbox.jav.service

import com.muedsa.tvbox.jav.TestOkHttpClient
import com.muedsa.tvbox.jav.checkMediaCardRows
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class MainScreenServiceTest {

    private val service = MainScreenService(
        okHttpClient = TestOkHttpClient,
    )

    @Test
    fun getRowsDataTest() = runTest{
        val rows = service.getRowsData()
        check(rows.isNotEmpty())
        checkMediaCardRows(rows = rows)
    }

}