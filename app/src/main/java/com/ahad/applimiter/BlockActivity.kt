package com.ahad.applimiter

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ahad.applimiter.databinding.ActivityBlockBinding

class BlockActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBlockBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBlockBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnOk.setOnClickListener {
            finish()
        }
    }

    override fun onBackPressed() {
        finish()
    }
}
