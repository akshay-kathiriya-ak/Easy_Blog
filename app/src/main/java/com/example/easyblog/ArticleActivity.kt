package com.example.easyblog

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.easyblog.adapter.ArticleAdapter
import com.example.easyblog.adapter.BlogItemModel
import com.example.easyblog.databinding.ActivityArticleBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ArticleActivity : AppCompatActivity() {
    private val binding: ActivityArticleBinding by lazy {
        ActivityArticleBinding.inflate(layoutInflater)
    }


    private lateinit var databaseReference: DatabaseReference
    private val auth = FirebaseAuth.getInstance()
    private lateinit var blogAdapter: ArticleAdapter
    private val EDIT_BLOG_REQUEST_CODE =123

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)


        val recyclerView = binding.ArticleRecyleView
        recyclerView.layoutManager = LinearLayoutManager(this)
        blogAdapter = ArticleAdapter(this, emptyList(), object : ArticleAdapter.OnItemClickListener {
            override fun onEditClick(blogItem: BlogItemModel) {
                // Handle edit click
                val intent = Intent(this@ArticleActivity,EditBlogActivity::class.java)
                intent.putExtra("blogItem",blogItem)
                startActivityForResult(intent,EDIT_BLOG_REQUEST_CODE)

            }

            override fun onReadMoreClick(blogItem: BlogItemModel) {
            // Handle read more click
                val intent =Intent(this@ArticleActivity,ReadMoreActivity::class.java)
                intent.putExtra("blogItem",blogItem)
                startActivity(intent)
            }

            override fun onDeleteClick(blogItem: BlogItemModel) {
                deleteBlogPost(blogItem)
            }
        })
        recyclerView.adapter = blogAdapter

        // Initialize Firebase reference
        databaseReference = FirebaseDatabase.getInstance("https://easy-blog-72460-default-rtdb.asia-southeast1.firebasedatabase.app")
            .getReference("blogs")

        fetchBlogData()
    }

    private fun fetchBlogData() {
        val currentUserId = auth.currentUser?.uid
        if (currentUserId == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val blogSavedList = mutableListOf<BlogItemModel>()
                for (postSnapshot in snapshot.children) {
                    val blogSaved = postSnapshot.getValue(BlogItemModel::class.java)
                    if (blogSaved != null && currentUserId == blogSaved.userId) {
                        blogSavedList.add(blogSaved)
                    }
                }
                blogAdapter.setData(blogSavedList)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ArticleActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun deleteBlogPost(blogItem: BlogItemModel) {
        val postId = blogItem.postId?: return
        val blogPostReference = databaseReference.child(postId)

        blogPostReference.removeValue()
            .addOnSuccessListener {
                Toast.makeText(this, "Blog Post Deleted", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Blog Post Not Deleted", Toast.LENGTH_SHORT).show()
            }
    }
}
