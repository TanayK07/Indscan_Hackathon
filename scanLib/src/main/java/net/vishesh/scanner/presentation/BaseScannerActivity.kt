package net.vishesh.scanner.presentation

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import net.vishesh.scanner.R
import net.vishesh.scanner.data.OpenCVLoader
import net.vishesh.scanner.databinding.ActivityScannerBinding
import net.vishesh.scanner.extensions.outputDirectory
import java.io.File

abstract class BaseScannerActivity : AppCompatActivity() {
    private lateinit var viewModel: ScannerViewModel
    private lateinit var binding: ActivityScannerBinding

    private val resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val bitmapUri =
                    result.data?.extras?.getString("croppedPath") ?: error("invalid path")

                val image = File(bitmapUri)
                val bmOptions = BitmapFactory.Options()
                val bitmap = BitmapFactory.decodeFile(image.absolutePath, bmOptions)
                onDocumentAccepted(bitmap)

                image.delete()
            } else {
                viewModel.onViewCreated(OpenCVLoader(this), this, binding.viewFinder)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        //triggerFullscreen()

        binding = ActivityScannerBinding.inflate(layoutInflater)

        setContentView(binding.root)

        val viewModel: ScannerViewModel by viewModels()

        viewModel.isBusy.observe(this, { isBusy ->
            binding.progress.visibility = if (isBusy) {
                View.VISIBLE
            } else {
                View.INVISIBLE
            }
        })

        viewModel.lastUri.observe(this) {
            val intent = Intent(this, CropperActivity::class.java)
            intent.putExtra("lastUri", it.toString())
            var bundle: Bundle

            intent.putExtra(
                "detectedCorners",
                if (viewModel.corners.value != null) ArrayList(viewModel.corners.value!!.pointsToPair()) else null
            )
            intent.putExtra(
                "oldSizeWidth",
                if (viewModel.corners.value != null) viewModel.corners.value!!.size.width else null
            )
            intent.putExtra(
                "oldSizeHeight",
                if (viewModel.corners.value != null) viewModel.corners.value!!.size.height else null
            )

            resultLauncher.launch(intent)
        }

        viewModel.errors.observe(this, {
            onError(it)
            Log.e(ScannerActivity::class.java.simpleName, it.message, it)
        })

        viewModel.corners.observe(this) {
            it?.let { corners ->
                binding.hud.onCornersDetected(corners)
            } ?: run {
                binding.hud.onCornersNotDetected()
            }
        }
        viewModel.mlCorners.observe(this) {
            it?.let { corners ->
                binding.hud.onMlCornersDetected(corners)
            } ?: run {
                binding.hud.onMlCornersNotDetected()
            }
        }

        viewModel.flashStatus.observe(this, { status ->
            binding.flashToggle.setImageResource(
                when (status) {
                    FlashStatus.ON -> R.drawable.ic_flash_on
                    FlashStatus.OFF -> R.drawable.ic_flash_off
                    else -> R.drawable.ic_flash_off
                }
            )
        })

        binding.flashToggle.setOnClickListener {
            viewModel.onFlashToggle()
        }

        binding.shutter.setOnClickListener {
            viewModel.onTakePicture(outputDirectory, this)
        }

        binding.closeScanner.setOnClickListener {
            closePreview()
        }
        this.viewModel = viewModel
    }

    override fun onResume() {
        super.onResume()
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        viewModel.onViewCreated(OpenCVLoader(this), this, binding.viewFinder)
    }

    private fun closePreview() {
        binding.rootView.visibility = View.GONE
        viewModel.onClosePreview()
        finish()
    }

    abstract fun onError(throwable: Throwable)
    abstract fun onDocumentAccepted(bitmap: Bitmap)
    abstract fun onClose()
}


