package com.example.easyblog.register

    import android.os.Bundle
    import android.widget.Toast
    import androidx.activity.enableEdgeToEdge
    import androidx.appcompat.app.AppCompatActivity
    import com.example.easyblog.adapter.BlogItemModel
    import com.example.easyblog.databinding.ActivityAddArticleBinding
    import com.example.easyblog.model.UserData
    import com.google.firebase.auth.FirebaseAuth
    import com.google.firebase.auth.FirebaseUser
    import com.google.firebase.database.DataSnapshot
    import com.google.firebase.database.DatabaseError
    import com.google.firebase.database.DatabaseReference
    import com.google.firebase.database.FirebaseDatabase
    import com.google.firebase.database.ValueEventListener
    import java.text.SimpleDateFormat
    import java.util.Date
    class AddArticleActivity : AppCompatActivity() {
        private val binding: ActivityAddArticleBinding by lazy {
            ActivityAddArticleBinding.inflate(layoutInflater)
        }

        private val databaseReference: DatabaseReference = FirebaseDatabase.getInstance("https://easy-blog-72460-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("blogs")
        private val userReference: DatabaseReference = FirebaseDatabase.getInstance("https://easy-blog-72460-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("users")
        private val auth = FirebaseAuth.getInstance()

        private val blogItems = mutableListOf<BlogItemModel>() // List to hold blog items

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            enableEdgeToEdge()
            setContentView(binding.root)
            binding.imageButton.setOnClickListener {
                finish()
            }

            binding.addBlogButton.setOnClickListener {
                val title = binding.blogTitle.editText?.text.toString().trim()
                val description = binding.blogCommet.editText?.text.toString().trim()

                if (title.isEmpty() || description.isEmpty()) {
                    Toast.makeText(this, "Please Fill all the Fields", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val user = auth.currentUser
                if (user != null) {
                    val userId = user.uid
                    val userName = user.displayName ?: "Anonymous"
                    val userImageUrl = user.photoUrl?.toString() ?: ""

                    // Fetch user data if needed
                    userReference.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val userData = snapshot.getValue(UserData::class.java)
                            val userNameFromDB = userData?.name ?: userName


                            val currentDate = SimpleDateFormat("yyyy-MM-dd").format(Date())

                            // Create BlogItemModel
                            val blogItem = BlogItemModel(
                                title,
                                userNameFromDB,
                                currentDate,
                                description,
                                userId,
                                0, // initial likes count

                            )

                            // Save blog item to Firebase
                            var key = databaseReference.push().key
                            if (key != null) {
                                blogItem.postId= key
                                val blogReference = databaseReference.child(key)
                                blogReference.setValue(blogItem).addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        Toast.makeText(this@AddArticleActivity, "Blog Added Successfully", Toast.LENGTH_SHORT).show()
                                        finish()
                                    } else {
                                        Toast.makeText(this@AddArticleActivity, "Failed to Add Blog", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } else {
                                Toast.makeText(this@AddArticleActivity, "Failed to generate Blog key", Toast.LENGTH_SHORT).show()
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Toast.makeText(this@AddArticleActivity, "Failed to fetch User data", Toast.LENGTH_SHORT).show()
                        }
                    })
                } else {
                    Toast.makeText(this@AddArticleActivity, "User not authenticated", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
