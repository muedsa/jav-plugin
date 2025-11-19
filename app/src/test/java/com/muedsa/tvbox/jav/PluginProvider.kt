package com.muedsa.tvbox.jav

import com.muedsa.tvbox.api.plugin.TvBoxContext
import com.muedsa.tvbox.tool.IPv6Checker
import com.muedsa.tvbox.tool.PluginCookieJar
import com.muedsa.tvbox.tool.SharedCookieSaver
import com.muedsa.tvbox.tool.createOkHttpClient

val TestPluginPrefStore by lazy {
    FakePluginPrefStore()
}

val TestCookieSaver by lazy { SharedCookieSaver(store = TestPluginPrefStore) }

val TestOkHttpClient by lazy {
    createOkHttpClient(
        debug = true,
        cookieJar = PluginCookieJar(saver = TestCookieSaver),
        onlyIpv4 = true,
    ) {
        proxy(
            java.net.Proxy(
                java.net.Proxy.Type.SOCKS,
                java.net.InetSocketAddress("127.0.0.1", 23333)
            )
        )
    }
}

val TestPlugin by lazy {
    JavPlugin(
        tvBoxContext = TvBoxContext(
            screenWidth = 1920,
            screenHeight = 1080,
            debug = true,
            store = TestPluginPrefStore,
            iPv6Status = IPv6Checker.checkIPv6Support()
        )
    )
}