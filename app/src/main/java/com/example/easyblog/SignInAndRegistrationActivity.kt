package com.example.easyblog

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity


import com.example.easyblog.databinding.ActivitySignInAndRegistrationBinding
import com.example.easyblog.model.UserData
import com.example.easyblog.register.welcomeActivity

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class SignInAndRegistrationActivity : AppCompatActivity() {
    private val binding: ActivitySignInAndRegistrationBinding by lazy {
        ActivitySignInAndRegistrationBinding.inflate(layoutInflater)
    }
    private lateinit var auth: FirebaseAuth
    private lateinit var datbase: FirebaseDatabase
    private lateinit var storage: FirebaseStorage
    private val PICK_IMAGE_REQUEST = 1
    private var imageUri: Uri? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
// firebase initialize
        auth = FirebaseAuth.getInstance()
        datbase =
            FirebaseDatabase.getInstance("https://easy-blog-72460-default-rtdb.asia-southeast1.firebasedatabase.app")
        storage = FirebaseStorage.getInstance()

        // for visibility of btn

        val action = intent.getStringExtra("action")

        // adjust visibility for login
        if (action == "login") {
            binding.loginEmail.visibility = View.VISIBLE
            binding.loginPassword.visibility = View.VISIBLE
            binding.loginButton.visibility = View.VISIBLE
            binding.registerName.visibility = View.GONE
            binding.registerEmail.visibility = View.GONE
            binding.registerPassword.visibility = View.GONE
            binding.cardView.visibility = View.GONE
            binding.loginButton.setOnClickListener {
                val loginEmail = binding.loginEmail.text.toString()
                val loginPassword = binding.loginPassword.text.toString()
                if (loginPassword.isEmpty() && loginEmail.isEmpty()) {
                    Toast.makeText(this, "Please Fill All Detail", Toast.LENGTH_SHORT).show()
                } else {
                    auth.signInWithEmailAndPassword(loginEmail, loginPassword)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this, MainActivity::class.java))
                                finish()
                            } else {
                                Toast.makeText(
                                    this,
                                    "Login Failed, Please Enter correct Detail",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                }
            }

            binding.registerButton.setOnClickListener {
                startActivity(Intent(this, SignInAndRegistrationActivity::class.java))

            }

        } else if (action == "register") {
            binding.loginButton.setOnClickListener {
                startActivity(Intent(this, welcomeActivity::class.java))
            }
            binding.registerButton.setOnClickListener {
                val registerName = binding.registerName.text.toString()
                val registerEmail = binding.registerEmail.text.toString()
                val registerPassword = binding.registerPassword.text.toString()
                if (registerName.isEmpty() || registerEmail.isEmpty() || registerPassword.isEmpty()) {
                    Toast.makeText(this, "Please Fill All Detail", Toast.LENGTH_SHORT).show()
                } else {
                    auth.createUserWithEmailAndPassword(registerEmail, registerPassword)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val user = auth.currentUser
                                auth.signOut()
                                user?.let {
                                    // save user data

                                    val userId = user.uid
                                    val userData = UserData(
                                        registerName,
                                        registerEmail,
                                         )


                                    val userReference = datbase.getReference("users").child(userId)
                                    userReference.setValue(userData)


                                    // upload image to firebase

                                    if (imageUri != null){
                                        val storageReference =
                                            storage.reference.child("profile/$userId.jpg")
                                        storageReference.putFile(imageUri!!)
                                            .addOnCompleteListener { task ->
                                                if (task.isSuccessful){
                                                    storageReference.downloadUrl.addOnCompleteListener { urlTask ->
                                                        if (urlTask.isSuccessful){
                                                            val downloadUrl = urlTask.result.toString()
                                                            // save the imgeurl to real time database
                                                            userReference.child("profileImage")
                                                                .setValue(downloadUrl)
                                                        }
                                                    }
                                                }
                                            }
                                    }

                                    Toast.makeText(
                                        this,
                                        "User Register Successfully",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    startActivity(Intent(this, welcomeActivity::class.java))
                                    finish()
                                }
                            } else {
                                Toast.makeText(this, "User registration Failed", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                }


            }
        }

        // set on clicklistner for choose img
        binding.ImagepickerSignup.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(intent, "select Image"), PICK_IMAGE_REQUEST)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            imageUri = data.data
        }
    }
}
