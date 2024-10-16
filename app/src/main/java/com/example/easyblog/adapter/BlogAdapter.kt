package com.example.easyblog.adapter

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.easyblog.R
import com.example.easyblog.ReadMoreActivity
import com.example.easyblog.databinding.BlogItemBinding

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class BlogAdapter(private var items: List<BlogItemModel>) :
    RecyclerView.Adapter<BlogAdapter.BlogViewHolder>() {
    private val databaseReference: DatabaseReference =
        FirebaseDatabase.getInstance("https://easy-blog-72460-default-rtdb.asia-southeast1.firebasedatabase.app").reference
    private val currentUser = FirebaseAuth.getInstance().currentUser

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BlogViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = BlogItemBinding.inflate(inflater, parent, false)
        return BlogViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BlogViewHolder, position: Int) {
        val blogItem = items[position]
        holder.bind(blogItem)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class BlogViewHolder(private val binding: BlogItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(blogItemModel: BlogItemModel) {
            val postId = blogItemModel.postId
            val context = binding.root.context
            binding.heading.text = blogItemModel.heading
            binding.userName.text = blogItemModel.userName
            binding.date.text = blogItemModel.date
            binding.post.text = blogItemModel.post
            binding.likeCount.text = blogItemModel.likeCount.toString()


            binding.root.setOnClickListener {
                val context = binding.root.context
                val intent = Intent(context, ReadMoreActivity::class.java)
                intent.putExtra("blogItem", blogItemModel)
                context.startActivity(intent)
            }
            binding.readMoreButton.setOnClickListener{
                val context = binding.root.context
                val intent = Intent(context, ReadMoreActivity::class.java)
                intent.putExtra("blogItem", blogItemModel)
                context.startActivity(intent)

            }

            // Check if the current user has liked the post and update the like button
            val postLikeReference = databaseReference.child("blogs").child(postId).child("likes")
            currentUser?.uid?.let { uid ->
                postLikeReference.child(uid)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                binding.likeButton.setImageResource(R.drawable.like_btn_r)
                            } else {
                                binding.likeButton.setImageResource(R.drawable.like_btn_b)
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Log.e("BlogAdapter", "Error checking like status", error.toException())
                        }
                    })
            }


            // Handle like button click
            binding.likeButton.setOnClickListener {
                if (currentUser != null) {
                    handleLikeButtonClicked(postId, blogItemModel, binding)
                } else {
                    Toast.makeText(context, "Login first", Toast.LENGTH_SHORT).show()
                }
            }

            // Set the initial icon based on save status
            val userReference = databaseReference.child("users").child(currentUser?.uid ?: "")
            val postSaveReference = userReference.child("savePosts").child(postId)
            postSaveReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        binding.saveButton.setImageResource(R.drawable.save)
                    } else {
                        binding.saveButton.setImageResource(R.drawable.unsave_article_red)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("BlogAdapter", "Error checking save status", error.toException())
                }
            })




            // Handle save button click
            binding.saveButton.setOnClickListener {
                if (currentUser != null) {
                    handleSaveButtonClicked(postId, blogItemModel, binding)
                } else {
                    Toast.makeText(context, "Login first", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun handleLikeButtonClicked(postId: String, blogItemModel: BlogItemModel, binding: BlogItemBinding) {
        val userReference = databaseReference.child("users").child(currentUser!!.uid)
        val postLikeReference = databaseReference.child("blogs").child(postId).child("likes")

        postLikeReference.child(currentUser.uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        userReference.child("likes").child(postId).removeValue()
                            .addOnSuccessListener {
                                postLikeReference.child(currentUser.uid).removeValue()
                                blogItemModel.likeBy?.remove(currentUser.uid)
                                updateLikeButtonImage(binding, false)

                                val newLikeCount = blogItemModel.likeCount - 1
                                blogItemModel.likeCount = newLikeCount
                                databaseReference.child("blogs").child(postId).child("likeCount")
                                    .setValue(newLikeCount)
                                notifyDataSetChanged()
                            }.addOnFailureListener { e ->
                                Log.e("BlogAdapter", "Failed to unlike post", e)
                            }
                    } else {
                        userReference.child("likes").child(postId).setValue(true)
                            .addOnSuccessListener {
                                postLikeReference.child(currentUser.uid).setValue(true)
                                blogItemModel.likeBy?.add(currentUser.uid)
                                updateLikeButtonImage(binding, true)

                                val newLikeCount = blogItemModel.likeCount + 1
                                blogItemModel.likeCount = newLikeCount
                                databaseReference.child("blogs").child(postId).child("likeCount")
                                    .setValue(newLikeCount)
                                notifyDataSetChanged()
                            }.addOnFailureListener { e ->
                                Log.e("BlogAdapter", "Failed to like post", e)
                            }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("BlogAdapter", "Error handling like button click", error.toException())
                }
            })
    }

    private fun updateLikeButtonImage(binding: BlogItemBinding, liked: Boolean) {
        if (liked) {
            binding.likeButton.setImageResource(R.drawable.like_btn_r)
        } else {
            binding.likeButton.setImageResource(R.drawable.like_btn_b)
        }
    }

    private fun handleSaveButtonClicked(postId: String, blogItemModel: BlogItemModel, binding: BlogItemBinding) {
        val userReference = databaseReference.child("users").child(currentUser!!.uid)
        userReference.child("savePosts").child(postId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        userReference.child("savePosts").child(postId).removeValue()
                            .addOnCompleteListener {
                                blogItemModel.isSaved = false
                                notifyDataSetChanged()
                                binding.saveButton.setImageResource(R.drawable.unsave_article_red)
                                Toast.makeText(binding.root.context, "Blog unsaved!", Toast.LENGTH_SHORT).show()
                            }.addOnFailureListener {
                                Toast.makeText(binding.root.context, "Failed to unsave blog", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        userReference.child("savePosts").child(postId).setValue(true)
                            .addOnSuccessListener {
                                blogItemModel.isSaved = true
                                notifyDataSetChanged()
                                binding.saveButton.setImageResource(R.drawable.save)
                                Toast.makeText(binding.root.context, "Blog saved!", Toast.LENGTH_SHORT).show()
                            }.addOnFailureListener {
                                Toast.makeText(binding.root.context, "Failed to save blog", Toast.LENGTH_SHORT).show()
                            }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("BlogAdapter", "Error handling save button click", error.toException())
                }
            })
    }

    fun updateData(newBlogList: List<BlogItemModel>) {
        items = newBlogList
        notifyDataSetChanged()
    }

}
