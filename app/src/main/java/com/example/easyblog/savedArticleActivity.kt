package com.example.easyblog

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.easyblog.adapter.BlogAdapter
import com.example.easyblog.adapter.BlogItemModel
import com.example.easyblog.databinding.ActivitySavedArticleBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class savedArticleActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySavedArticleBinding

    private val savedBlogArticle = mutableListOf<BlogItemModel>()
    private lateinit var blogAdapter: BlogAdapter
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySavedArticleBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()

        blogAdapter = BlogAdapter(savedBlogArticle)
        val recyclerView = binding.savedArticleRecyleView
        recyclerView.adapter = blogAdapter
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val userId = auth.currentUser?.uid
        if (userId != null) {
            val userReference = FirebaseDatabase.getInstance("https://easy-blog-72460-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("users").child(userId).child("savePosts")
            userReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    savedBlogArticle.clear()
                    for (postSnapshot in snapshot.children) {
                        val postId = postSnapshot.key
                        val isSaved = postSnapshot.value as Boolean
                        if (postId != null && isSaved) {
                            CoroutineScope(Dispatchers.IO).launch {
                                val blogItem = fetchBlogItem(postId)
                                if (blogItem != null) {
                                    savedBlogArticle.add(blogItem)
                                    launch(Dispatchers.Main) {
                                        blogAdapter.updateData(savedBlogArticle)
                                    }
                                }
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("SavedArticleActivity", "Database error: ${error.message}")
                }
            })
        }

        binding.backButton.setOnClickListener {
            finish()
        }
    }

    private suspend fun fetchBlogItem(postId: String): BlogItemModel? {
        val blogReference = FirebaseDatabase.getInstance("https://easy-blog-72460-default-rtdb.asia-southeast1.firebasedatabase.app")
            .getReference("blogs")
        return try {
            val dataSnapshot = blogReference.child(postId).get().await()
            dataSnapshot.getValue(BlogItemModel::class.java)
        } catch (e: Exception) {
            Log.e("SavedArticleActivity", "Error fetching blog item: ${e.message}")
            null
        }
    }
}
