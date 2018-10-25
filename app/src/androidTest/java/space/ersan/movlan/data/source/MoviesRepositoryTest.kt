package space.ersan.movlan.data.source

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.Observer
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.runner.AndroidJUnit4
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.equalTo
import org.junit.*
import org.junit.runner.RunWith
import space.ersan.movlan.FakeMoviesApi
import space.ersan.movlan.MoviesFactory
import space.ersan.movlan.TestCoroutineDispatchers
import space.ersan.movlan.app.MovlanApp
import space.ersan.movlan.data.model.Genre
import space.ersan.movlan.data.source.local.MoviesDb
import space.ersan.movlan.data.source.local.MoviesDbBoundaryCallback
import space.ersan.movlan.data.source.local.MoviesLocalDataSource
import space.ersan.movlan.data.source.remote.MoviesRemoteDataSource
import space.ersan.movlan.mock
import space.ersan.movlan.utils.LiveNetworkStatus

@RunWith(AndroidJUnit4::class)
class MoviesRepositoryTest {

  @get:Rule
  val instantExecutor = InstantTaskExecutorRule()

  private val moviesFactory = MoviesFactory()
  private val popularMovies = (1..50).map { moviesFactory.createMovie(title = "Name$it") }
  private val searchMovies = (1..30).map { moviesFactory.createMovie(title = "Name$it") }
  private val genreList = (0..3).map { Genre(it, "Genre:$it") }

  private lateinit var moviesRepository: MoviesRepository
  private lateinit var database: MoviesDb
  private lateinit var lifecycleOwner: LifecycleOwner

  private val fakeMoviesApi = FakeMoviesApi(
      popularMovies = popularMovies,
      genreList = genreList,
      movieDetails = popularMovies[0],
      searchResult = searchMovies
      )

  @Before
  fun setUp() {
    val context = ApplicationProvider.getApplicationContext<MovlanApp>()
    database = Room.inMemoryDatabaseBuilder(context, MoviesDb::class.java)
        .build()
    val moviesDao = database.moviesDao()
    val localDataSource = MoviesLocalDataSource(moviesDao)
    val remoteDataSource = MoviesRemoteDataSource(fakeMoviesApi)
    val networkStatus = LiveNetworkStatus()
    val boundaryCallback = MoviesDbBoundaryCallback(TestCoroutineDispatchers,
        remoteDataSource,
        localDataSource,
        networkStatus)
    moviesRepository = MoviesRepository(TestCoroutineDispatchers,
        localDataSource,
        remoteDataSource,
        boundaryCallback,
        networkStatus)

    lifecycleOwner = LifecycleOwner {
      LifecycleRegistry(mock()).apply {
        handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
      }
    }
  }

  @After
  fun tearDown() {
    database.close()
  }

  @Test
  fun getPopularMovies() {
    val liveData = moviesRepository.getPopularMovies()
    liveData.observe(lifecycleOwner, Observer {
      Assert.assertNotNull(it)
      Assert.assertThat(it.size, `is`(equalTo(50)))
      Assert.assertThat(it.last().title, `is`(equalTo(popularMovies.last().title)))
    })
  }

  @Test
  fun getMovieDetails() {
    val movieLiveData = moviesRepository.getMovieDetails(1)
    Assert.assertThat(movieLiveData.value, `is`(equalTo(popularMovies[0])))
  }

  @Test
  fun searchMovies() {
    val liveData = moviesRepository.searchMovies("Hello")
    liveData.observe(lifecycleOwner, Observer {
      Assert.assertNotNull(it)
      Assert.assertThat(it.size, `is`(equalTo(30)))
      Assert.assertThat(it.last().title, `is`(equalTo(searchMovies.last().title)))
    })
  }
}