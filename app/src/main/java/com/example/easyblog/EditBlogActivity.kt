package com.example.easyblog

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.easyblog.adapter.BlogItemModel
import com.example.easyblog.databinding.ActivityEditBlogBinding
import com.google.firebase.database.FirebaseDatabase

class EditBlogActivity : AppCompatActivity() {
    private val binding: ActivityEditBlogBinding by lazy {
        ActivityEditBlogBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        val blogItemModel = intent.getParcelableExtra<BlogItemModel>("blogItem")
        if (blogItemModel != null) {
            binding.blogTitle.editText?.setText(blogItemModel.heading)
            binding.blogCommet.editText?.setText(blogItemModel.post)
        } else {
            Toast.makeText(this, "Error loading blog details", Toast.LENGTH_SHORT).show()
            finish()
        }

        binding.saveBlogButton.setOnClickListener {
            val updatedTitle = binding.blogTitle.editText?.text.toString().trim()
            val updatedDesc = binding.blogCommet.editText?.text.toString().trim()
            if (updatedTitle.isEmpty() || updatedDesc.isEmpty()) {
                Toast.makeText(this, "Please fill all details", Toast.LENGTH_SHORT).show()
            } else {
                blogItemModel?.heading = updatedTitle
                blogItemModel?.post = updatedDesc
                if (blogItemModel != null) {
                    updateDetailInFirebase(blogItemModel)
                }
            }
        }
    }

    private fun updateDetailInFirebase(blogItemModel: BlogItemModel) {
        val databaseReference = FirebaseDatabase.getInstance("https://easy-blog-72460-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("blogs")
        val postId = blogItemModel.postId ?: return

        databaseReference.child(postId).setValue(blogItemModel)
            .addOnSuccessListener {
                Toast.makeText(this, "Blog update successful", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Blog update unsuccessful", Toast.LENGTH_SHORT).show()
            }
    }
}
