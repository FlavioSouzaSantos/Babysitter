package br.com.babysitter.ui.elements

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import br.com.babysitter.databinding.ActivityReadQrcodeBinding
import br.com.babysitter.ui.holders.QRCodeAnalyzer
import br.com.babysitter.ui.holders.QRCodeReadListener
import com.google.common.util.concurrent.ListenableFuture
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ReadQRCodeActivity : AppCompatActivity(), QRCodeReadListener {

    private lateinit var mBinding: ActivityReadQrcodeBinding
    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var cameraProviderFuture:ListenableFuture<ProcessCameraProvider>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityReadQrcodeBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
    }

    override fun onStart() {
        super.onStart()
        cameraExecutor = Executors.newSingleThreadExecutor()
        startCamera()
    }

    private fun startCamera() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(mBinding.previewView.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder().build()

            val imageAnalyzer = ImageAnalysis.Builder()
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, QRCodeAnalyzer(this))
                }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalyzer)
            } catch(exc: Exception) {
                Log.e(TAG, exc.message, exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    override fun onReadQRCode(content: String) {
        if(this::cameraProviderFuture.isInitialized){
            cameraProviderFuture.cancel(true)
        }

        if(validateUrl(content)){
            runOnUiThread {
                val intent = Intent(this, ReceiverActivity::class.java)
                intent.putExtra(ReceiverActivity.URL_KEY, content)
                startActivity(intent)
            }
        }else{
            runOnUiThread {
                Toast.makeText(applicationContext, "QRCode inválido!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun validateUrl(url:String):Boolean {
        return url.startsWith("rtsp://") && url.endsWith("/ch0")
    }

    companion object {
        const val TAG:String = "ReadQRCodeActivity";
    }

}