package com.example.easyblog.register

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.easyblog.MainActivity
import com.example.easyblog.R
import com.example.easyblog.SignInAndRegistrationActivity
import com.example.easyblog.databinding.ActivityWelcomeBinding
import com.google.firebase.auth.FirebaseAuth

class welcomeActivity : AppCompatActivity() {
    private lateinit var auth : FirebaseAuth
    private val binding : ActivityWelcomeBinding by lazy {
        ActivityWelcomeBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()
        binding.registerButton.setOnClickListener {
            val intent =Intent(this,SignInAndRegistrationActivity::class.java)
            intent.putExtra("action","register")
            startActivity(intent)
            finish()
        }
        binding.loginButton.setOnClickListener {
            val intent =Intent(this,SignInAndRegistrationActivity::class.java)
            intent.putExtra("action","login")
            startActivity(intent)
            finish()
        }
    }
    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser != null){
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}