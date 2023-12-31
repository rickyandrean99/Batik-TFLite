package com.rickyandrean.batiktflite

import android.content.Intent
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import com.rickyandrean.batiktflite.databinding.ActivityMainBinding
import com.rickyandrean.batiktflite.ml.Batik
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var bitmap: Bitmap
    private val labels = listOf("Cendrawasih", "Geblek Renteng", "Insang", "Kawung", "Mega Mendung", "Parang", "Pring Sdapur", "Simbut", "Sogan", "Truntum")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Preprocess Image Taken
        val imagePreprocessing = ImageProcessor.Builder()
            .add(NormalizeOp(0.0f, 1.0f))
            .add(ResizeOp(224, 224, ResizeOp.ResizeMethod.BILINEAR))
            .build()

        binding.selectBtn.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            startActivityForResult(intent, 100)
        }

        binding.predictBtn.setOnClickListener {
            // Load and preprocess the image
            var tensorImage = TensorImage(DataType.FLOAT32)
            tensorImage.load(bitmap)
            tensorImage = imagePreprocessing.process(tensorImage)

            // Prepare the model and input
            val model = Batik.newInstance(this)
            val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 224, 224, 3), DataType.FLOAT32)
            inputFeature0.loadBuffer(tensorImage.buffer)

            // Process model and get the result
            val outputs = model.process(inputFeature0)
            val outputFeature0 = outputs.outputFeature0AsTensorBuffer.floatArray

            // Show the result
            var maxIdx = 0
            outputFeature0.forEachIndexed { index, fl ->
                if (outputFeature0[maxIdx] < fl) {
                    maxIdx = index
                }
            }
            binding.resView.text = labels[maxIdx]


            model.close()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 100) {
            val uri = data?.data
            bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
            binding.imageView.setImageBitmap(bitmap)
        }
    }
}