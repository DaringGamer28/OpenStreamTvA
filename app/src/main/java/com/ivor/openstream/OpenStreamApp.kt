package com.ivor.openstream

import android.app.Application
import android.content.Context
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.disk.DiskCache
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import com.ivor.openstream.data.cache.CacheBudget
import com.ivor.openstream.data.diagnostics.Diagnostics
import com.ivor.openstream.data.settings.AppDns
import com.ivor.openstream.data.settings.AppLocale
import com.ivor.openstream.data.settings.AppSettingsStore
import com.ivor.openstream.data.settings.CacheSize
import dagger.hilt.android.HiltAndroidApp
import okhttp3.OkHttpClient
import okio.Path.Companion.toOkioPath
import java.io.File
import javax.inject.Inject

@HiltAndroidApp
class OpenStreamApp : Application(), SingletonImageLoader.Factory {

    @Inject
    lateinit var appDns: AppDns

    @Inject
    lateinit var appSettingsStore: AppSettingsStore

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(AppLocale.wrap(base, AppLocale.savedTag(base)))
    }

    override fun onCreate() {
        super.onCreate()
        Diagnostics.installCrashRecorder(this)
        // Background tasks (EpisodeAlarm and smartDownloads) removed to save TV RAM
    }

    /**
     * Artwork goes through the app's DNS setting too, so posters load where TMDB's DNS is blocked.
     * The disk cache stays in Coil's default folder (so nothing cached earlier is lost) and is
     * sized by the Cache size setting.
     */
    override fun newImageLoader(context: PlatformContext): ImageLoader {
        val client = OkHttpClient.Builder().dns(appDns).build()
        val directory = File(cacheDir, CacheBudget.IMAGE_DIR)
        // Hilt injects in onCreate; nothing should ask for images before that, but never crash on it.
        val wanted = (if (::appSettingsStore.isInitialized) appSettingsStore.current.cacheSize else CacheSize.STANDARD).imageBytes
        return ImageLoader.Builder(context)
            .components { add(OkHttpNetworkFetcherFactory(client)) }
            .diskCache {
                DiskCache.Builder()
                    .directory(directory.toOkioPath())
                    .maxSizeBytes(CacheBudget.bytesFor(directory, wanted))
                    .build()
            }
            .build()
    }
}
