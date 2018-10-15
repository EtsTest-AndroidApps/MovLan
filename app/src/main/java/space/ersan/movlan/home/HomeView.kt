package space.ersan.movlan.home

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.widget.FrameLayout
import androidx.core.view.postDelayed
import androidx.paging.PagedList
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import kotlinx.android.synthetic.main.view_main.view.*
import space.ersan.movlan.R
import space.ersan.movlan.common.view.EndlessRecyclerViewScrollListener
import space.ersan.movlan.data.model.Movie
import space.ersan.movlan.home.list.MoviesListAdapter
import space.ersan.movlan.image.ImageLoader

@SuppressLint("ViewConstructor")
class HomeView(context: Context, private val thumbnailLoader: ImageLoader.Thumbnail) : FrameLayout(
    context) {

  private val adapter: MoviesListAdapter = MoviesListAdapter(thumbnailLoader)
  private val layoutManger = StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL)

  init {
    View.inflate(context, R.layout.view_main, this)
    recyclerView.adapter = adapter
    recyclerView.layoutManager = layoutManger
  }

  fun setMovies(result: PagedList<Movie>) {
    println("setting movies list $result")

    adapter.submitList(result)
    recyclerView.addOnScrollListener(object : EndlessRecyclerViewScrollListener(layoutManger) {
      override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView) {
        val key = result.lastKey
        println("Mosh lastKey:$key")
        val lastItem = adapter.getItem(adapter.itemCount - 1)
        println("Mosh lastItem: ${lastItem?.page}, total Items: ${adapter.itemCount}")
      }
    })
  }

  fun observeMovieListClicks(clb: (Movie) -> Unit) {
  }

  fun showError(error: Exception) {
    error.printStackTrace()
  }

  fun observeSwipeToRefresh(clb: () -> Unit) = swipeToRefresh.setOnRefreshListener(clb)

  fun setRefreshInficator(refresh: Boolean) {
    swipeToRefresh.isRefreshing = refresh
  }

}