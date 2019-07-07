package com.example.textrecognition

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.text.FirebaseVisionText
import com.wonderkiln.camerakit.*
import kotlinx.android.synthetic.main.activity_text_recognizer.*

class TextRecognizerActivity : AppCompatActivity() {

    private val dialog: Dialog by lazy { createAlertDialog() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_text_recognizer)
        setData()
        setListeners()
    }

    override fun onPause() {
        super.onPause()
        cameraView.stop()
    }

    override fun onResume() {
        super.onResume()
        cameraView.start()
    }

    private fun setData() {
        createAlertDialog()
    }

    private fun setListeners() {
        btnRecognize.setOnClickListener {
            dialog.show()
            if (!cameraView.isStarted) {
                cameraView.start()
                dialog.dismiss()
            }
            cameraView.captureImage()
            graphicOverlay.clear()
        }

        cameraView.addCameraKitListener(object : CameraKitEventListener {
            override fun onVideo(p0: CameraKitVideo?) {}

            override fun onEvent(p0: CameraKitEvent?) {}

            override fun onImage(image: CameraKitImage?) {
                val bitmap = Bitmap.createScaledBitmap(image!!.bitmap, cameraView.width, cameraView.height, false)
                cameraView.stop()
                recognizeText(bitmap)
            }

            override fun onError(p0: CameraKitError?) {}

        })
    }

    private fun createAlertDialog(): AlertDialog {
        return AlertDialog.Builder(this).apply {
            title = resources.getString(R.string.app_name)
            setMessage("Espera un momento")
            setCancelable(false)
        }.create()
    }

    private fun recognizeText(bitmap: Bitmap) {
        val image = FirebaseVisionImage.fromBitmap(bitmap)
        val textRecognizer = FirebaseVision.getInstance().cloudTextRecognizer

        textRecognizer.processImage(image)
            .addOnSuccessListener { result -> processTextResult(result) }
            .addOnFailureListener { error ->
                Log.e("Error", error.message!!)
                dialog.dismiss()
            }
    }

    private fun processTextResult(result: FirebaseVisionText) {
        val blocks = result.textBlocks
        if (blocks.size == 0) {
            Toast.makeText(this, "No se encontró texto", Toast.LENGTH_SHORT).show()
            return
        }
        graphicOverlay.clear()

        blocks.forEach { block ->
            block.lines.forEach { line ->
                line.elements.forEach { element ->
                    val textGraphic = TextGraphic(graphicOverlay, element)
                    graphicOverlay.add(textGraphic)
                }
            }
        }

        dialog.dismiss()
    }

}