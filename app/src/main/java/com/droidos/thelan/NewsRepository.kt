package com.droidos.thelan

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class NewsViewModel(private val newsRepository: NewsRepository) : ViewModel() {

    private val _articles = MutableLiveData<List<Article>>()
    val articles: LiveData<List<Article>> get() = _articles

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    fun fetchArticles() {
        try {
            val result = newsRepository.getArticles()
            _articles.setValue(result)  // Use setValue for synchronous updates in test
        } catch (e: Exception) {
            _error.setValue(e.message ?: "Unknown error")
        }
    }

}

interface NewsRepository {
    fun getArticles(): List<Article>
}
