package com.harshul.pdfkotlin.ui.view

import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.DatePicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.harshul.pdfkotlin.R
import com.harshul.pdfkotlin.databinding.ActivityMainBinding
import com.harshul.pdfkotlin.utils.Constants
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream
import com.tom_roush.pdfbox.pdmodel.font.PDType1Font
import com.tom_roush.pdfbox.pdmodel.graphics.image.JPEGFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable.isCancelled
import kotlinx.coroutines.launch
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var myCalendar: Calendar
    private lateinit var date: DatePickerDialog.OnDateSetListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        myCalendar = Calendar.getInstance()
        CoroutineScope(Dispatchers.IO).launch {
            downloadPdf(filesDir.path + "/registration_form.pdf")
        }

        date =
            OnDateSetListener { _: DatePicker?, year: Int, monthOfYear: Int, dayOfMonth: Int ->
                myCalendar[Calendar.YEAR] = year
                myCalendar[Calendar.MONTH] = monthOfYear
                myCalendar[Calendar.DAY_OF_MONTH] = dayOfMonth
                updateDOB()
            }

        binding.btnProceed.setOnClickListener { fillPdf() }

        binding.etDob.setOnClickListener {
            val datePickerDialog = DatePickerDialog(
                this, date, myCalendar
                    .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
            datePickerDialog.show()
        }

    }

    private fun updateDOB() {
        val myFormat = "dd MM yyyy" //In which you need put here
        val sdf = SimpleDateFormat(myFormat, Locale.US)
        binding.etDob.setTextColor(Color.BLACK)
        binding.etDob.setText(sdf.format(myCalendar.time))
    }

    private fun downloadPdf(filePath: String): String? {
        var input: InputStream? = null
        var output: OutputStream? = null
        var connection: HttpURLConnection? = null
        try {
            val url = URL(Constants.urlString)
            connection = url.openConnection() as HttpURLConnection
            connection.connect()

            // expect HTTP 200 OK, so we don't mistakenly save error report
            // instead of the file
            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                return ("Server returned HTTP " + connection.responseCode
                        + " " + connection.responseMessage)
            }

            // this will be useful to display download percentage
            // might be -1: server did not report the length
            val fileLength = connection.contentLength
            // download the file
            input = connection.inputStream
            output = FileOutputStream(filePath)
            val data = ByteArray(4096)
            var total: Long = 0
            var count: Int
            while (input.read(data).also { count = it } != -1) {
                // allow canceling with back button
                if (isCancelled) {
                    input.close()
                    return null
                }
                total += count.toLong()
                // publishing the progress....
                if (fileLength > 0) // only if total length is known
                //  publishProgress((total * 100 / fileLength).toInt())
                    output.write(data, 0, count)
            }
        } catch (e: java.lang.Exception) {
            Log.d("TAG", "doInBackground: $e")
            return e.toString()
        } finally {
            try {
                output?.close()
                input?.close()
            } catch (ignored: IOException) {
            }
            connection?.disconnect()
        }
        return null
    }

    private fun fillPdf() {
        try {
            val firstName = binding.etFirstName.text.toString().trim()
            if (firstName.isEmpty()) {
                Toast.makeText(this, "Enter Firstname", Toast.LENGTH_SHORT).show()
                binding.scrollView.scrollTo(0, binding.labelFirstName.top)
                binding.ivFirstName.visibility = View.VISIBLE
                return
            }

            val surName = binding.etSurname.text.toString().trim()
            if (surName.isEmpty()) {
                Toast.makeText(this, "Enter Surname", Toast.LENGTH_SHORT).show()
                binding.scrollView.scrollTo(0, binding.labelSurname.top)
                binding.ivSurname.visibility = View.VISIBLE
                return
            }

            val gender = when (binding.genderRadio.checkedRadioButtonId) {
                R.id.radioButton1 -> "Male"
                R.id.radioButton2 -> "Female"
                R.id.radioButton3 -> "Other"
                else -> "NA"
            }
            if (gender.isEmpty() || gender == "NA") {
                Toast.makeText(this, "Select Gender", Toast.LENGTH_SHORT).show()
                binding.scrollView.scrollTo(0, binding.genderRadio.bottom)
                return
            }

            val dob = binding.etDob.text.toString().trim()
            if (dob.isEmpty()) {
                Toast.makeText(this, "Enter DOB", Toast.LENGTH_SHORT).show()
                binding.scrollView.scrollTo(0, binding.etDob.top)
                return
            }

            val mobileNo = binding.etMobileNo.text.toString().trim()
            if (mobileNo.isEmpty()) {
                Toast.makeText(this, "Enter Mobile No", Toast.LENGTH_SHORT).show()
                binding.scrollView.scrollTo(0, binding.labelMobileNo.top)
                binding.ivMobileNo.visibility = View.VISIBLE
                return
            }

            val email = binding.etEmail.text.toString().trim()
            if (email.isEmpty()) {
                Toast.makeText(this, "Enter Email", Toast.LENGTH_SHORT).show()
                binding.scrollView.scrollTo(0, binding.labelEmail.top)
                binding.ivEmail.visibility = View.VISIBLE
                return
            }

            val birthPlace = binding.etBirth.text.toString().trim()
            if (birthPlace.isEmpty()) {
                Toast.makeText(this, "Enter Birth City/Town", Toast.LENGTH_SHORT).show()
                binding.scrollView.scrollTo(0, binding.etBirth.top)
                binding.ivBirth.visibility = View.VISIBLE
                return
            }

            val address = binding.etAddress.text.toString().trim()
            if (address.isEmpty()) {
                Toast.makeText(this, "Enter Address", Toast.LENGTH_SHORT).show()
                binding.scrollView.scrollTo(0, binding.labelAddress.top)
                binding.ivAddress.visibility = View.VISIBLE
                return
            }

            val pincode = binding.etPincode.text.toString().trim()
            if (pincode.isEmpty()) {
                Toast.makeText(this, "Enter Pincode", Toast.LENGTH_SHORT).show()
                binding.scrollView.scrollTo(0, binding.etPincode.bottom)
                binding.ivPincode.visibility = View.VISIBLE
                return
            }

            PDFBoxResourceLoader.init(applicationContext)
            val files = filesDir.listFiles().filter { it.name.equals("registration_form.pdf") }
            val getPdf: InputStream = FileInputStream(files[0])
            val doc = PDDocument.load(getPdf)

            //Retrieving the pages of the document
            val docPage = doc.getPage(0)
            val contentStream = PDPageContentStream(doc, docPage, true, true, true)
            //PDPageContentStream contentStream = new PDPageContentStream(doc, docPage);

            //Begin the Content stream
            val bitmap = BitmapFactory.decodeResource(resources, R.drawable.ic_tick)
            val setImage = JPEGFactory.createFromImage(doc, bitmap)
            contentStream.drawImage(setImage, 115F, 590F, 24F, 24F)
            if (gender == "Male") {
                contentStream.drawImage(setImage, 480F, 510F, 24F, 24F)
            } else if (gender == "Female") {
                contentStream.drawImage(setImage, 550F, 510F, 24F, 24F)
            }

            contentStream.beginText()
            contentStream.setFont(PDType1Font.COURIER_BOLD, 10f)
            contentStream.newLineAtOffset(110F, 578F)
            contentStream.showText(firstName)
            contentStream.newLineAtOffset(300F, 0F)
            contentStream.showText(surName)
            contentStream.newLineAtOffset(-250F, -30F)
            contentStream.showText(birthPlace)
            contentStream.newLineAtOffset(0F, -30F)
            contentStream.showText(dob)
            contentStream.newLineAtOffset(0F, -40F)
            contentStream.showText(address)
            contentStream.newLineAtOffset(70F, -30F)
            contentStream.showText(pincode)
            contentStream.newLineAtOffset(170F, 0F)
            contentStream.showText("I N D I A")
            contentStream.newLineAtOffset(-240F, -75F)
            contentStream.showText(mobileNo)
            contentStream.newLineAtOffset(-20F, -40F)
            contentStream.showText(email)
            contentStream.endText()
            contentStream.close()
            doc.save("${filesDir.path}/filled_form.pdf")
            doc.close()

            startActivity(Intent(this, RegistrationFormActivity::class.java))

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}