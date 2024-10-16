package com.example.easyblog

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.easyblog.databinding.ActivityProfileBinding
import com.example.easyblog.register.AddArticleActivity
import com.example.easyblog.register.welcomeActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ProfileActivity : AppCompatActivity() {
    private val binding: ActivityProfileBinding by lazy {
        ActivityProfileBinding.inflate(layoutInflater)
    }
    private lateinit var databaseReference: DatabaseReference
    private  lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        // open add new article page
        binding.addNewArticleButton.setOnClickListener {
            startActivity(Intent(this,AddArticleActivity::class.java))
        }
        // to go your Article activity
        binding.yourBlog.setOnClickListener {
            startActivity(Intent(this,ArticleActivity::class.java))
        }

        // log out
        binding.logOutButton.setOnClickListener {
            auth.signOut()
            // nevigation
            startActivity(Intent(this,welcomeActivity::class.java))
            finish()
        }
        // initialize Firebase
        auth =FirebaseAuth.getInstance()

        databaseReference =FirebaseDatabase.getInstance("https://easy-blog-72460-default-rtdb.asia-southeast1.firebasedatabase.app").reference.child("users")
        val userId = auth.currentUser?.uid
        if (userId != null){
            loadUserProfileData(userId)
        }
    }

    private fun loadUserProfileData(userID: String) {
        val userReference = databaseReference.child(userID)
        // load user profile
        //  baki che
        // load user name
        userReference.child("name").addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
               val userName = snapshot.getValue(String::class.java )
                if (userName != null){
                    binding.userProfileName.text =userName
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }
}