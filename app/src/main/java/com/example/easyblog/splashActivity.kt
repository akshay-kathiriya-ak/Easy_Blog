package com.example.easyblog

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

import com.example.easyblog.register.welcomeActivity


class splashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash)

       Handler(Looper.getMainLooper()).postDelayed({
           startActivity(Intent(this,welcomeActivity::class.java))
           finish()
       },3000)
    }


}