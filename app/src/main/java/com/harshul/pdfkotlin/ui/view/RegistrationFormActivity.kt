package com.harshul.pdfkotlin.ui.view

import android.app.ActionBar
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.TextUtils
import android.util.Base64
import android.view.Display
import android.view.View
import android.view.Window
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContract
import androidx.appcompat.app.AppCompatActivity
import com.harshul.pdfkotlin.databinding.ActivityRegistrationFormBinding
import com.harshul.pdfkotlin.databinding.DialogTermsConditionBinding
import com.harshul.pdfkotlin.utils.Constants
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPage
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream
import com.tom_roush.pdfbox.pdmodel.font.PDType1Font
import com.tom_roush.pdfbox.pdmodel.graphics.image.JPEGFactory
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.text.DateFormat
import java.util.*

class RegistrationFormActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegistrationFormBinding
    var imgBase64 = ""
    val TAG = "TAG"
    lateinit var bitmap: Bitmap
    lateinit var file: File

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegistrationFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val files = filesDir.listFiles().filter { it.name == "filled_form.pdf" }
        file = files[0]
        binding.pdfView.fromFile(file).load()

        val contract = registerForActivityResult(Contract()) {
            imgBase64 = it
            if (TextUtils.isEmpty(imgBase64)) {
                Toast.makeText(this, "Invalid Signature. Sign Again", Toast.LENGTH_SHORT).show()
            } else {
                try {
                    bitmap = getBitmap(imgBase64)!!
                    binding.labelSignature.visibility = View.GONE
                    binding.ivSignature.setImageBitmap(bitmap)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        binding.ivSignature.setOnClickListener { contract.launch(Unit) }

        binding.btnProceed.setOnClickListener {
            if (imgBase64.isNotBlank()) agreeTermsDialog()
            else Toast.makeText(
                this,
                "Add Signature",
                Toast.LENGTH_SHORT
            ).show()
        }

    }

    inner class Contract : ActivityResultContract<Unit, String>() {

        override fun createIntent(context: Context, input: Unit): Intent {
            return Intent(context, YourSignActivity::class.java)
        }

        override fun parseResult(resultCode: Int, intent: Intent?): String {
            return if (intent != null) {
                intent.getStringExtra(Constants.KEY_SIGN)!!
            } else ""

        }

    }

    private fun getBitmap(encodedString: String): Bitmap? {
        return try {
            val decodedString = Base64.decode(encodedString, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
        } catch (e: java.lang.Exception) {
            e.message
            null
        }
    }

    private fun addSignatureToPdf() {
        try {

            val getPdf: InputStream = FileInputStream(file)
            val doc = PDDocument.load(getPdf)
            var page: PDPage?
            var contentStream: PDPageContentStream
            val setImage = JPEGFactory.createFromImage(doc, bitmap)
            val currentDate =
                DateFormat.getDateInstance(DateFormat.MEDIUM).format(Calendar.getInstance().time)


            for (pageNo in 0 until doc.numberOfPages) {
                page = doc.getPage(pageNo)
                contentStream = PDPageContentStream(doc, page, true, true, true)
                contentStream.beginText()
                contentStream.setFont(PDType1Font.COURIER_BOLD, 14F)
                contentStream.newLineAtOffset(430F, 100F)
                contentStream.showText(currentDate)
                contentStream.endText()
                contentStream.drawImage(setImage, 150F, 150F, 100F, 100F)
                contentStream.close()
            }

            doc.save("${filesDir.path}/submitted_form.pdf")
            doc.close()
            startActivity(Intent(this, FormSubmitActivity::class.java))
            finish()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun agreeTermsDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)

        val dialogBinding = DialogTermsConditionBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)

        dialogBinding.buttonYes.setOnClickListener {
            addSignatureToPdf()
            dialog.dismiss()
        }

        dialogBinding.buttonNo.setOnClickListener { dialog.cancel() }

        dialog.show()
        val display: Display = this.windowManager.defaultDisplay
        val width: Int = display.width - 100
        val window: Window? = dialog.window
        window?.setLayout(width, ActionBar.LayoutParams.WRAP_CONTENT)
        window?.setBackgroundDrawableResource(android.R.color.transparent)
    }
}