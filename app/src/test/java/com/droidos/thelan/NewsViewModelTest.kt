package com.droidos.thelan

import android.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import app.cash.turbine.test
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config


@RunWith(RobolectricTestRunner::class)  // Use Robolectric's runner
@Config(sdk = [28])  // Specify the SDK version for the test
class NewsViewModelTest {

    // Rule to allow LiveData to be observed on a background thread
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    // Mock objects
    @Mock
    private lateinit var newsRepository: NewsRepository

    @Mock
    private lateinit var articlesObserver: Observer<List<Article>>

    @Mock
    private lateinit var errorObserver: Observer<String>

    private lateinit var newsViewModel: NewsViewModel

    @Before
    fun setup() {
        // Initialize mock objects
        MockitoAnnotations.openMocks(this)

        // Create the ViewModel instance
        newsViewModel = NewsViewModel(newsRepository)

        // Set up Observer for the LiveData
        newsViewModel.articles.observeForever(articlesObserver)
        newsViewModel.error.observeForever(errorObserver)

    }

    @Test
    fun testFetchArticlesSuccess() {
        // Mock data
        val mockArticles = listOf(
            Article("Title 1", "Content 1"),
            Article("Title 2", "Content 2")
        )
        `when`(newsRepository.getArticles()).thenReturn(mockArticles)

        // Call method to fetch articles
        newsViewModel.fetchArticles()

        // Verify that the repository method is called
        verify(newsRepository).getArticles()

        // Verify that LiveData emits the correct data
        verify(articlesObserver).onChanged(mockArticles)
    }

    @Test
    fun testFetchArticlesError() {
        // Mock error message
        val errorMessage = "Error fetching articles"
        `when`(newsRepository.getArticles()).thenThrow(RuntimeException(errorMessage))

        // Call method to fetch articles
        newsViewModel.fetchArticles()

        // Verify that the repository method is called
        verify(newsRepository).getArticles()

        // Verify that LiveData emits the error message
        verify(errorObserver).onChanged(errorMessage)
    }

    @Test
    fun `test flow`() = runTest {
        val res = flowOf(1, 2, 3, 4)
        res.test {
            assert(awaitItem() == 1)
            assert(awaitItem() == 2)
            assert(awaitItem() == 3)
            assert(awaitItem() == 4)
            awaitComplete()
        }
    }

    @Test
    fun `test flow with exception`() = runTest {
        val res = flow {
            emit(1)
            emit(2)
            emit(3)
            throw IllegalArgumentException("Test Exception")
        }
        res.test {
            assertEquals(1, awaitItem())
            assertEquals(2, awaitItem())
            assertEquals(3, awaitItem())
            awaitError().message?.contains("Test Exception")
        }
    }

    @Test
    fun `convert to hot flow `() = runTest {
        val flow = flowOf(1, 2, 3).map { it * 10 }.stateIn(this)

        flow.test {
            assertEquals(10, awaitItem())
        }

    }
}