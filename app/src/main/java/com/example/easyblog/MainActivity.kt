package com.example.easyblog

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager

import com.example.easyblog.adapter.BlogAdapter
import com.example.easyblog.adapter.BlogItemModel
import com.example.easyblog.databinding.ActivityMainBinding
import com.example.easyblog.register.AddArticleActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {
    private val binding : ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    private lateinit var databaseReference: DatabaseReference
    private val blogItems = mutableListOf<BlogItemModel>()
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // to go save article page
        binding.saveArticleButton.setOnClickListener {
            startActivity(Intent(this,savedArticleActivity::class.java))
        }

        enableEdgeToEdge()
        setContentView(binding.root)
        auth =FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance("https://easy-blog-72460-default-rtdb.asia-southeast1.firebasedatabase.app").reference.child("blogs")

        val  userId = auth.currentUser?.uid
        // to go profile page
            binding.profileImg.setOnClickListener {
                startActivity(Intent(this,ProfileActivity::class.java))
            }

        //
        if (userId != null){
            loadUserProfileImage(userId)
        }
        // initialize the recycler view and set adapter
        val recyclerView =binding.BlogViewHolder
        val blogAdapter =BlogAdapter(blogItems)
        recyclerView.adapter =blogAdapter
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager =LinearLayoutManager(this)

        // fetch data from database
        databaseReference.addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                blogItems.clear()
                for (snapshot in snapshot.children){
                    val  blogItem =snapshot.getValue(BlogItemModel::class.java)
                    if (blogItem != null){
                        blogItems.add(blogItem)
                    }
                }
                // reverse data
                blogItems.reverse()

                // notify the adapter that data change
                blogAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
              Toast.makeText(this@MainActivity,"Blog Loading is failed",Toast.LENGTH_SHORT).show()
            }
        })



        binding.floatingAddArticleButton.setOnClickListener {
            startActivity(Intent(this,AddArticleActivity::class.java))
        }
    }

    private fun loadUserProfileImage(userId: String) {
        val useReference =FirebaseDatabase.getInstance("https://easy-blog-72460-default-rtdb.asia-southeast1.firebasedatabase.app").reference.child("users").child(userId)
        useReference.child("profileImage").addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val profileImageUrl = snapshot.getValue(String::class.java)
                if (profileImageUrl != null){
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity,"Error Loading ProfileImage",Toast.LENGTH_SHORT).show()
            }
        })
    }
}