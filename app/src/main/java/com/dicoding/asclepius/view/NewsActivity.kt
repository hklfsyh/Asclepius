package com.dicoding.asclepius.view

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.dicoding.asclepius.databinding.ActivityNewsBinding
import com.dicoding.asclepius.model.NewsResponse
import com.dicoding.asclepius.network.RetrofitClient
import com.dicoding.asclepius.view.adapter.NewsAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NewsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNewsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.recyclerViewNews.layoutManager = LinearLayoutManager(this)

        fetchNewsData()
    }

    private fun fetchNewsData() {
        val apiKey = "573f2de6e36a4020b42450e5df382e7c"
        val call = RetrofitClient.instance.getCancerNews(apiKey = apiKey)

        call.enqueue(object : Callback<NewsResponse> {
            override fun onResponse(call: Call<NewsResponse>, response: Response<NewsResponse>) {
                if (response.isSuccessful) {
                    val newsResponse = response.body()
                    if (newsResponse != null) {
                        val validArticles = newsResponse.articles.filter { article ->
                            article.title != "[Removed]" && article.description != "[Removed]" && article.url != null && article.urlToImage != null
                        }

                        binding.recyclerViewNews.adapter = NewsAdapter(validArticles)
                    }
                } else {
                    Toast.makeText(
                        this@NewsActivity, "Failed to fetch news data", Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<NewsResponse>, t: Throwable) {
                Toast.makeText(
                    this@NewsActivity, "Failed to connect to the API", Toast.LENGTH_SHORT
                ).show()
                Log.e("NewsActivity", "Error: ${t.message}")
            }
        })
    }

}