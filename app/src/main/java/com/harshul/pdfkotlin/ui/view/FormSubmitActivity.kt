package com.harshul.pdfkotlin.ui.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.harshul.pdfkotlin.databinding.ActivityFormSubmitBinding

class FormSubmitActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFormSubmitBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFormSubmitBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val files = filesDir.listFiles().filter { it.name == "submitted_form.pdf" }
        binding.pdfView.fromFile(files[0]).load()
    }
}