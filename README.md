# webstream-android-framework

## Integration
Import `w3bstream` into your project as a module, and sync you project.


## Usage
### Init

```
    private val config by lazy {
        W3bStreamKitConfig(
            AUTH_HOST,
            HTTPS_UPLOAD_API,
            WEB_SOCKET_UPLOAD_API
        )
    }

    private val w3bStreamKit by lazy {
        W3bStreamKit.Builder(config).build()
    }
```


### Create device
```
    private fun create() {
        lifecycleScope.launch {
            val device = w3bStreamKit.createDevice()
            mTvImei.text = "IMEI:${device.imei}"
            mTvSn.text = "SN:${device.sn}"
        }
    }

```

### Upload data
```
	w3bStreamKit.startUpload {
	    return@startUpload "{"imei":"100374242236884","latitude":34.09589161,"location":106.42410187}"
	}
```
TIPS: The type of data must be json string


### Other
Sign the device
```
w3bStreamKit.sign(imei, sn, pubKey)
```

Set the server for uploading data
```
w3bStreamKit.setHttpsServerApi(api)
w3bStreamKit.setWebSocketServerApi(api)
```

Set the interval for uploading data
```
w3bStreamKit.setUploadInterval(seconds)
```
