package com.machinefi.w3bstream

import com.machinefi.w3bstream.repository.upload.UploadManager
import io.reactivex.plugins.RxJavaPlugins

class W3bStreamKit private constructor(
    uploadManager: UploadManager
) : UploadManager by uploadManager {

    init {
        RxJavaPlugins.setErrorHandler {
            it.printStackTrace()
        }
    }

    class Builder(config: W3bStreamKitConfig) {

        private val w3bStreamKitModule = W3bStreamKitModule(config)

        fun build() = W3bStreamKit(
            w3bStreamKitModule.uploadManager
        )
    }

}