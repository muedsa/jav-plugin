package com.muedsa.tvbox.jav.service

import com.muedsa.tvbox.api.data.MediaCatalogOption
import com.muedsa.tvbox.api.data.MediaCatalogOptionItem
import com.muedsa.tvbox.jav.TestPlugin
import com.muedsa.tvbox.jav.checkMediaCard
import kotlinx.coroutines.test.runTest
import org.junit.Test

class MediaCatalogServiceTest {

    private val service = TestPlugin.provideMediaCatalogService()

    @Test
    fun getConfig_test() = runTest {
        val config = service.getConfig()
        check(config.pageSize > 0)
//        check(config.catalogOptions.isNotEmpty())
//        check(config.catalogOptions.size == config.catalogOptions.distinctBy { it.value }.size)
//        for (option in config.catalogOptions) {
//            check(option.items.isNotEmpty())
//            check(option.items.size == option.items.distinctBy { it.value }.size)
//        }
        check(config.cardWidth > 0)
    }

    @Test
    fun catalog_test() = runTest {
        val config = service.getConfig()
        val page1 = service.catalog(
            options = MediaCatalogOption.getDefault(config.catalogOptions),
            loadKey = config.initKey,
            loadSize = config.pageSize
        )
        check(page1.list.isNotEmpty())
        page1.list.forEach {
            checkMediaCard(it, config.cardType)
        }
        check(page1.nextKey != null)
        val page2 = service.catalog(
            options = listOf(
                MediaCatalogOption(
                    name = "排序",
                    value = "sort",
                    items = listOf(
                        MediaCatalogOptionItem(
                            name = "发布日期",
                            value = "release_date",
                        ),
                    ),
                    required = true,
                )
            ),
            loadKey = page1.nextKey!!,
            loadSize = config.pageSize
        )
        check(page2.list.isNotEmpty())
        page2.list.forEach {
            checkMediaCard(it, config.cardType)
        }
        check(page2.nextKey != null)
        check(page2.prevKey === page1.nextKey)
    }

}