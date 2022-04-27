package br.com.babysitter.ui.elements

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Bundle
import android.os.PowerManager
import android.view.SurfaceHolder
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import br.com.babysitter.R
import br.com.babysitter.databinding.ActivityReceiverBinding

class ReceiverActivity : AppCompatActivity(), MediaPlayer.OnPreparedListener, SurfaceHolder.Callback {
    private lateinit var mBinding:ActivityReceiverBinding
    private lateinit var mWakeLock: PowerManager.WakeLock
    private lateinit var mMediaPlayer: MediaPlayer
    private lateinit var mWifiLok:WifiManager.WifiLock
    private var mUrl:String?=null

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)

        mUrl = savedInstanceState?.getString(URL_KEY)
        if(mUrl == null){
            mUrl = intent.getStringExtra(URL_KEY)
        }

        if(mUrl == null){
            Toast.makeText(this, getString(R.string.error_url_not_found), Toast.LENGTH_SHORT).show()
            finish()
        }

        mBinding = ActivityReceiverBinding.inflate(layoutInflater)
        mBinding.sfView.holder.addCallback(this)
        supportActionBar?.hide()

        val power = getSystemService(Context.POWER_SERVICE) as PowerManager
        mWakeLock = power.newWakeLock(PowerManager.FULL_WAKE_LOCK, "babysitter:appWakeLockTag")

        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        mWifiLok = wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL, "babysitter::appWifiLockTag")

        setContentView(mBinding.root)
    }

    override fun onStart() {
        super.onStart()

        mMediaPlayer = MediaPlayer()
        mMediaPlayer.setOnPreparedListener(this)
        mMediaPlayer.setDataSource(this, Uri.parse(mUrl))
        mMediaPlayer.prepareAsync()

        if(this::mWakeLock.isInitialized){
            mWakeLock.acquire()
        }
        if(this::mWifiLok.isInitialized){
            mWifiLok.acquire()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if(mUrl != null){
            outState.putString(URL_KEY, mUrl)
        }
    }

    override fun onPrepared(mp: MediaPlayer?) {
        mBinding.progressCircular.visibility = View.GONE
        mp?.start()
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        if(this::mMediaPlayer.isInitialized){
            mMediaPlayer.setDisplay(holder)
        }
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        if(this::mMediaPlayer.isInitialized){
            mMediaPlayer.setDisplay(holder)
        }
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        if(this::mMediaPlayer.isInitialized){
            mMediaPlayer.setDisplay(null)
        }
    }

    override fun onStop() {
        super.onStop()

        if(this::mWakeLock.isInitialized && mWakeLock.isHeld){
            mWakeLock.release()
        }

        if(this::mWifiLok.isInitialized){
            mWifiLok.release()
        }

        if(this::mMediaPlayer.isInitialized){
            mMediaPlayer.release()
        }
    }

    companion object {
        const val URL_KEY:String = "ReceiverActivity_URL_KEY"
    }
}