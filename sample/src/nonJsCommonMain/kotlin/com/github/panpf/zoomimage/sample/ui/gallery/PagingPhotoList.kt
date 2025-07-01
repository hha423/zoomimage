package com.github.panpf.zoomimage.sample.ui.gallery

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState.Loading
import app.cash.paging.PagingData
import app.cash.paging.compose.LazyPagingItems
import app.cash.paging.compose.collectAsLazyPagingItems
import com.github.panpf.sketch.ability.bindPauseLoadWhenScrolling
import com.github.panpf.zoomimage.sample.AppSettings
import com.github.panpf.zoomimage.sample.ui.common.PagingListAppendState
import com.github.panpf.zoomimage.sample.ui.common.PagingListRefreshState
import com.github.panpf.zoomimage.sample.ui.components.VerticalScrollbarCompat
import com.github.panpf.zoomimage.sample.ui.model.Photo
import kotlinx.coroutines.flow.Flow
import org.koin.compose.koinInject

@Composable
@OptIn(ExperimentalMaterialApi::class)
fun PagingPhotoList(
    photoPagingFlow: Flow<PagingData<Photo>>,
    modifier: Modifier = Modifier,
    gridCellsMinSize: Dp = 100.dp,
    onClick: (items: List<Photo>, photo: Photo, index: Int) -> Unit,
) {
    val pagingItems = photoPagingFlow.collectAsLazyPagingItems()
    val appSettings: AppSettings = koinInject()

    val pullRefreshState = rememberPullRefreshState(
        refreshing = pagingItems.loadState.refresh is Loading,
        onRefresh = { pagingItems.refresh() }
    )
    Box(
        modifier = modifier
            .fillMaxSize()
            .pullRefresh(pullRefreshState)
    ) {
        val staggeredGridMode by appSettings.staggeredGridMode.collectAsState()
        if (staggeredGridMode) {
            PhotoStaggeredGrid(
                pagingItems = pagingItems,
                gridCellsMinSize = gridCellsMinSize,
                onClick = onClick,
            )
        } else {
            PhotoSquareGrid(
                pagingItems = pagingItems,
                gridCellsMinSize = gridCellsMinSize,
                onClick = onClick,
            )
        }

        PullRefreshIndicator(
            refreshing = pagingItems.loadState.refresh is Loading,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter)
        )

        PagingListRefreshState(pagingItems)

        BottomToolbar(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
        )
    }
}

@Composable
private fun PhotoSquareGrid(
    pagingItems: LazyPagingItems<Photo>,
    gridCellsMinSize: Dp,
    onClick: (items: List<Photo>, photo: Photo, index: Int) -> Unit,
) {
    Box(Modifier.fillMaxSize()) {
        val gridState = rememberLazyGridState()
        bindPauseLoadWhenScrolling(gridState)

        LazyVerticalGrid(
            columns = GridCells.Adaptive(gridCellsMinSize),
            state = gridState,
            contentPadding = PaddingValues(start = 4.dp, top = 4.dp, end = 4.dp, bottom = 84.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            items(
                count = pagingItems.itemCount,
                key = { "${pagingItems.peek(it)?.originalUrl}:${it}" },
                contentType = { 1 }
            ) { index ->
                val photo = pagingItems[index]!!
                PhotoGridItem(
                    index = index,
                    photo = photo,
                    staggeredGridMode = false,
                    onClick = { photo1, index1 ->
                        onClick(pagingItems.itemSnapshotList.items, photo1, index1)
                    },
                )
            }

            if (pagingItems.itemCount > 0) {
                item(
                    key = "AppendState",
                    span = { GridItemSpan(this.maxLineSpan) },
                    contentType = 2
                ) {
                    PagingListAppendState(pagingItems)
                }
            }
        }

        VerticalScrollbarCompat(
            modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight().padding(10.dp),
            gridState = gridState
        )
    }
}

@Composable
private fun PhotoStaggeredGrid(
    pagingItems: LazyPagingItems<Photo>,
    gridCellsMinSize: Dp,
    onClick: (items: List<Photo>, photo: Photo, index: Int) -> Unit,
) {
    Box(Modifier.fillMaxSize()) {
        val gridState = rememberLazyStaggeredGridState()
        bindPauseLoadWhenScrolling(gridState)

        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Adaptive(minSize = gridCellsMinSize),
            state = gridState,
            contentPadding = PaddingValues(start = 4.dp, top = 4.dp, end = 4.dp, bottom = 84.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalItemSpacing = 4.dp,
        ) {
            items(
                count = pagingItems.itemCount,
                key = { "${pagingItems.peek(it)?.originalUrl}:${it}" },
                contentType = { 1 }
            ) { index ->
                val photo = pagingItems[index]!!
                PhotoGridItem(
                    index = index,
                    photo = photo,
                    staggeredGridMode = true,
                    onClick = { photo1, index1 ->
                        onClick(pagingItems.itemSnapshotList.items, photo1, index1)
                    },
                )
            }

            if (pagingItems.itemCount > 0) {
                item(
                    key = "AppendState",
                    span = StaggeredGridItemSpan.FullLine,
                    contentType = 2
                ) {
                    PagingListAppendState(pagingItems)
                }
            }
        }

        // VerticalScrollbar is not yet supported in StaggeredGrid
//        VerticalScrollbar(
//            modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
//            adapter = rememberScrollbarAdapter(
//                scrollState = gridState
//            )
//        )
    }
}