package com.dicoding.asclepius.view.adapter

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dicoding.asclepius.databinding.NewsItemBinding
import com.dicoding.asclepius.model.Article

class NewsAdapter(private val articles: List<Article>) :
    RecyclerView.Adapter<NewsAdapter.NewsViewHolder>() {

    inner class NewsViewHolder(private val binding: NewsItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(article: Article) {
            binding.textViewTitle.text = article.title
            binding.textViewDescription.text = article.description
            binding.textViewDate.text = article.publishedAt
            Glide.with(binding.root.context).load(article.urlToImage).into(binding.imageViewNews)

            binding.root.setOnClickListener {
                val context = binding.root.context
                val url = article.url

                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse(url)
                }
                context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val binding = NewsItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NewsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        holder.bind(articles[position])
    }

    override fun getItemCount(): Int = articles.size
}

