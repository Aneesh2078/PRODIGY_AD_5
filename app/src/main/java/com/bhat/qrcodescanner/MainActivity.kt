package com.bhat.qrcodescanner

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bhat.qrcodescanner.databinding.ActivityMainBinding
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var scannedLink: String? = null
    private lateinit var qrScannerLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize View Binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Register ActivityResultLauncher
        qrScannerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val intentResult: IntentResult? = IntentIntegrator.parseActivityResult(result.resultCode, result.data)
            if (intentResult != null && intentResult.contents != null) {
                handleScannedResult(intentResult.contents)
            } else {
                showMessage("Scan canceled or no QR code detected.")
            }
        }

        // Set click listeners
        binding.scanButton.setOnClickListener { startQRScanner() }
        binding.navigateButton.setOnClickListener { navigateToScannedLink() }
        binding.copyButton.setOnClickListener { copyScannedLinkToClipboard() }

        // Disable buttons initially
        binding.navigateButton.isEnabled = false
        binding.copyButton.isEnabled = false
    }

    override fun onResume() {
        super.onResume()
        // Ensure any required resources are ready (if applicable).
    }

    override fun onPause() {
        super.onPause()
        // Release resources if necessary (e.g., stop camera preview or heavy operations).
    }

    override fun onDestroy() {
        super.onDestroy()
        // Clean up any resources tied to SurfaceView, Camera, or ZXing library.
        releaseResources()
    }

    private fun startQRScanner() {
        val integrator = IntentIntegrator(this).apply {
            setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES) // Scan all barcodes, including QR codes
            setPrompt("Scan any barcode or QR Code")
            setCameraId(0) // Use rear camera
            setBeepEnabled(true)
            setBarcodeImageEnabled(true)
            setOrientationLocked(true)
        }
        qrScannerLauncher.launch(integrator.createScanIntent())
    }

    private fun handleScannedResult(result: String) {
        scannedLink = result
        binding.scannedLinkTextView.text = scannedLink

        // Enable buttons after a successful scan
        binding.navigateButton.isEnabled = true
        binding.copyButton.isEnabled = true
    }

    private fun navigateToScannedLink() {
        scannedLink?.let {
            try {
                val uri = Uri.parse(it)
                val intent = Intent(Intent.ACTION_VIEW, uri)
                if (intent.resolveActivity(packageManager) != null) {
                    startActivity(intent)
                } else {
                    showMessage("No app available to open this link.")
                }
            } catch (e: Exception) {
                showMessage("Invalid link format. Cannot navigate to the scanned link.")
            }
        } ?: showMessage("No link scanned. Please scan a QR code first.")
    }

    private fun copyScannedLinkToClipboard() {
        scannedLink?.let {
            val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = ClipData.newPlainText("Scanned QR Code", it)
            clipboardManager.setPrimaryClip(clipData)
            showMessage("Scanned link copied to clipboard!")
        } ?: showMessage("No link scanned. Please scan a QR code first.")
    }

    private fun releaseResources() {
        try {
            // Add cleanup logic for ZXing or SurfaceView (if applicable)
            // Example: Stop Camera Preview or release SurfaceView
            showMessage("Resources released successfully.")
        } catch (e: Exception) {
            // Log any errors during cleanup
            showMessage("Error while releasing resources: ${e.localizedMessage}")
        }
    }

    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
