package com.example.easyblog

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.easyblog.adapter.BlogItemModel
import com.example.easyblog.databinding.ActivityReadMoreBinding

class ReadMoreActivity : AppCompatActivity() {
    private lateinit var binding: ActivityReadMoreBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReadMoreBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        binding.backbutton.setOnClickListener{
            finish()
        }

        val blogs =intent.getParcelableExtra<BlogItemModel>("blogItem")
        if (blogs != null){
            // retrive user releted data
            binding.titleText.text =blogs.heading
            binding.userName.text =blogs.userName
            binding.dateShow.text =blogs.date
            binding.blogDescView.text =blogs.post
            val userImageUrl =blogs.profileImage
        }else{
            Toast.makeText(this,"Failed to load blog",Toast.LENGTH_SHORT).show()
        }

    }
}