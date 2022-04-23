package br.com.babysitter.ui.elements

import android.content.Context
import android.graphics.Bitmap
import android.net.wifi.WifiManager
import android.os.Bundle
import android.os.PowerManager
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import br.com.babysitter.databinding.ActivityTransmitterBinding
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import veg.mediacapture.sdk.MediaCapture
import veg.mediacapture.sdk.MediaCapture.CaptureNotifyCodes
import veg.mediacapture.sdk.MediaCaptureCallback
import veg.mediacapture.sdk.MediaCaptureConfig
import java.nio.ByteBuffer


class TransmitterActivity: AppCompatActivity(), MediaCaptureCallback {

    lateinit var mBinding: ActivityTransmitterBinding
    lateinit var mCapture: MediaCapture
    lateinit var mWakeLock:PowerManager.WakeLock
    var multicastLock:WifiManager.MulticastLock?=null
    var url:String?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mBinding = ActivityTransmitterBinding.inflate(layoutInflater)
        mCapture = mBinding.captureView

        savedInstanceState?.getString(URL_KEY, null)?.apply {
            onChangeUrl(this)
        }

        val power = getSystemService(Context.POWER_SERVICE) as PowerManager
        mWakeLock = power.newWakeLock(PowerManager.FULL_WAKE_LOCK, "babysitter:appWakeLockTag")

        val wifi = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
        multicastLock = wifi.createMulticastLock("babysitter:appMulticastLockTag")
        multicastLock?.setReferenceCounted(true)

        setContentView(mBinding.root)
    }

    override fun onStart() {
        super.onStart()

        val mConfig = mCapture.config
        mConfig.captureMode = MediaCaptureConfig.CaptureModes.PP_MODE_ALL.`val`()
        mConfig.streamType = MediaCaptureConfig.StreamerTypes.STREAM_TYPE_RTSP_SERVER.`val`()
        mConfig.transFormat = MediaCaptureConfig.TYPE_VIDEO_H263
        mConfig.audioFormat = MediaCaptureConfig.TYPE_AUDIO_AAC
        mConfig.audioChannels = 2
        mCapture.RequestPermission(this)
        mCapture.Open(null, this)

        if(this::mWakeLock.isInitialized){
            mWakeLock.acquire()
        }

        if(multicastLock != null){
            multicastLock!!.acquire()
        }

        if(this::mCapture.isInitialized){
            mCapture.onStart()
            mCapture.Start()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        url?.apply {
            outState.putString(URL_KEY, this)
        }
    }
    override fun OnCaptureStatus(status: Int): Int {
        val mStatus = CaptureNotifyCodes.forValue(status)
        if(mStatus.equals(CaptureNotifyCodes.CAP_STARTED)){
            runOnUiThread { mBinding.progressCircular.visibility = View.GONE }
            onChangeUrl(mCapture.config.url)
        }
        return 0
    }

    override fun OnCaptureReceiveData(buffer: ByteBuffer?, type: Int, size: Int, pts: Long): Int {
        return 0
    }

    private fun onChangeUrl(newUrl:String){
        url = newUrl
        val bitmap:Bitmap? = createQRCode(url!!)
        runOnUiThread {
            mBinding.imageQrCode.setImageBitmap(bitmap)
        }
    }

    override fun onPause() {
        super.onPause()
        if(this::mCapture.isInitialized){
            mCapture.onPause()
        }
    }

    override fun onResume() {
        super.onResume()
        if(this::mCapture.isInitialized){
            mCapture.onResume()
        }
    }

    override fun onStop() {
        super.onStop()

        if(this::mCapture.isInitialized){
            mCapture.onStop()
        }

        if(this::mWakeLock.isInitialized && mWakeLock.isHeld){
            mWakeLock.release()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        if(this::mCapture.isInitialized){
            mCapture.onDestroy()
        }

        if(multicastLock != null){
            multicastLock!!.release()
            multicastLock = null
        }
    }

    private fun createQRCode(str:String):Bitmap? {
        val result = try {
            MultiFormatWriter().encode(str, BarcodeFormat.QR_CODE, WIDTH, HEIGHT, null)
        } catch (iae: IllegalArgumentException) {null}

        if(result == null) return result;

        val width = result.width
        val height = result.height
        val pixels = IntArray(width * height)
        for (y in 0 until height) {
            val offset = y * width
            for (x in 0 until width) {
                pixels[offset + x] = if (result[x, y]) BLACK else WHITE
            }
        }

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        return bitmap
    }

    companion object {
        const val URL_KEY = "TransmitterActivity_URL_KEY"
        const val WHITE = -0x1
        const val BLACK = -0x1000000
        const val WIDTH = 400
        const val HEIGHT = 400
    }
}