package com.harshul.pdfkotlin.ui.view

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.harshul.pdfkotlin.utils.PaintView
import com.harshul.pdfkotlin.R
import com.harshul.pdfkotlin.utils.Constants.KEY_SIGN
import com.harshul.pdfkotlin.databinding.ActivityYourSignBinding
import java.io.ByteArrayOutputStream

class YourSignActivity : AppCompatActivity() {

    lateinit var binding: ActivityYourSignBinding
    private lateinit var mPaintView: PaintView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityYourSignBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mPaintView = PaintView(this, null)
        binding.llCanvas.addView(mPaintView, 0)
        mPaintView.requestFocus()


    }

    fun onClick(v: View) {
        when (v.id) {
            R.id.ivDelete -> {
                mPaintView = PaintView(this, null)
                binding.llCanvas.addView(mPaintView, 0)
                mPaintView.requestFocus()
            }
            R.id.btnSave -> saveBitmap()
        }
    }

    private fun saveBitmap() {
        if (mPaintView.arl.size == 0) {
            Toast.makeText(this, "Signature not valid", Toast.LENGTH_LONG).show()
            return
        }

        // View view = mLlCanvas.getRootView();
        val view = binding.llCanvas.getChildAt(0) as View
        view.isDrawingCacheEnabled = true
        val bitmap = Bitmap.createBitmap(view.drawingCache)
        view.isDrawingCacheEnabled = false
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        val byteArray = stream.toByteArray()
        val encoded = Base64.encodeToString(byteArray, Base64.DEFAULT)
        val intent = Intent()
        intent.putExtra(KEY_SIGN, encoded)
        setResult(RESULT_OK, intent)
        finish()
    }
}