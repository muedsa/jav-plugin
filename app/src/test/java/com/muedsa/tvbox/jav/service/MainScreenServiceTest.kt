package com.muedsa.tvbox.jav.service

import com.muedsa.tvbox.jav.TestOkHttpClient
import com.muedsa.tvbox.jav.checkMediaCardRows
import kotlinx.coroutines.test.runTest
import org.junit.Test

class MainScreenServiceTest {

    private val service = MainScreenService(
        okHttpClient = TestOkHttpClient,
    )

    @Test
    fun getRowsDataTest() = runTest{
        val rows = service.getRowsData()
        checkMediaCardRows(rows = rows)
    }

}