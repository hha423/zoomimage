package com.github.panpf.zoomimage.sample.ui.gallery

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.github.panpf.sketch.ability.bindPauseLoadWhenScrolling
import com.github.panpf.zoomimage.sample.AppSettings
import com.github.panpf.zoomimage.sample.ui.components.AppendState
import com.github.panpf.zoomimage.sample.ui.components.VerticalScrollbarCompat
import com.github.panpf.zoomimage.sample.ui.model.Photo
import kotlinx.coroutines.flow.collectLatest
import org.koin.compose.koinInject

@Composable
@OptIn(ExperimentalMaterialApi::class)
fun PhotoList(
    modifier: Modifier = Modifier,
    gridCellsMinSize: Dp = 100.dp,
    initialPageStart: Int = 0,
    pageSize: Int = 80,
    load: suspend (pageStart: Int, pageSize: Int) -> List<Photo>,
    calculateNextPageStart: (currentPageStart: Int, loadedPhotoSize: Int) -> Int,
    onClick: (items: List<Photo>, photo: Photo, index: Int) -> Unit,
) {
    var photos: List<Photo> by remember { mutableStateOf(emptyList()) }
    val appSettings: AppSettings = koinInject()
    var pageStart by remember { mutableStateOf(initialPageStart) }
    var nextPageStart: Int? by remember { mutableStateOf(null) }
    var refreshing: Boolean by remember { mutableStateOf(false) }
    var appendState: AppendState? by remember { mutableStateOf(null) }
    LaunchedEffect(Unit) {
        snapshotFlow { pageStart }.collectLatest {
            val finalPageStart = it.takeIf { it >= 0 } ?: initialPageStart  // refresh
            refreshing = finalPageStart == initialPageStart
            appendState = if (finalPageStart > initialPageStart) AppendState.Loading else null
            val loadedPhotos = load(finalPageStart, pageSize)
            photos = if (it == initialPageStart) {
                loadedPhotos
            } else {
                photos + loadedPhotos
            }
            nextPageStart = calculateNextPageStart(it, loadedPhotos.size)
            appendState = if (finalPageStart > initialPageStart && loadedPhotos.isEmpty())
                AppendState.End else AppendState.Loading
            refreshing = false
        }
    }

    val pullRefreshState = rememberPullRefreshState(
        refreshing = refreshing,
        onRefresh = { pageStart = -1 }
    )
    Box(
        modifier = modifier
            .fillMaxSize()
            .pullRefresh(pullRefreshState)
    ) {
        val staggeredGridMode by appSettings.staggeredGridMode.collectAsState()
        if (staggeredGridMode) {
            val staggeredGridState = rememberLazyStaggeredGridState()
            LaunchedEffect(staggeredGridState.layoutInfo, photos) {
                val nextPageStart1 = nextPageStart
                if (nextPageStart1 != null && staggeredGridState.layoutInfo.visibleItemsInfo.last().index == photos.size - 1) {
                    pageStart = nextPageStart1
                }
            }
            PhotoStaggeredGrid(
                staggeredGridState = staggeredGridState,
                photos = photos,
                gridCellsMinSize = gridCellsMinSize,
                appendState = appendState,
                onClick = onClick,
            )
        } else {
            val gridState = rememberLazyGridState()
            LaunchedEffect(gridState.layoutInfo, photos) {
                val nextPageStart1 = nextPageStart
                if (nextPageStart1 != null && gridState.layoutInfo.visibleItemsInfo.last().index == photos.size - 1) {
                    pageStart = nextPageStart1
                }
            }
            PhotoSquareGrid(
                gridState = gridState,
                photos = photos,
                gridCellsMinSize = gridCellsMinSize,
                appendState = appendState,
                onClick = onClick,
            )
        }

        PullRefreshIndicator(
            refreshing = refreshing,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter)
        )

        BottomToolbar(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
        )
    }
}

@Composable
private fun PhotoSquareGrid(
    gridState: LazyGridState,
    photos: List<Photo>,
    gridCellsMinSize: Dp,
    appendState: AppendState?,
    onClick: (items: List<Photo>, photo: Photo, index: Int) -> Unit,
) {
    Box(Modifier.fillMaxSize()) {
        bindPauseLoadWhenScrolling(gridState)

        LazyVerticalGrid(
            columns = GridCells.Adaptive(gridCellsMinSize),
            state = gridState,
            contentPadding = PaddingValues(start = 4.dp, top = 4.dp, end = 4.dp, bottom = 84.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            items(
                count = photos.size,
                key = { "${photos[it].originalUrl}:${it}" },
                contentType = { 1 }
            ) { index ->
                val item = photos[index]

                PhotoGridItem(
                    index = index,
                    photo = item,
                    staggeredGridMode = false,
                    onClick = { photo, index1 ->
                        onClick(photos, photo, index1)
                    },
                )
            }

            if (appendState != null) {
                item(
                    key = "AppendState",
                    span = { GridItemSpan(this.maxLineSpan) },
                    contentType = 2
                ) {
                    AppendState(appendState)
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
    staggeredGridState: LazyStaggeredGridState,
    photos: List<Photo>,
    gridCellsMinSize: Dp,
    appendState: AppendState?,
    onClick: (items: List<Photo>, photo: Photo, index: Int) -> Unit,
) {
    Box(Modifier.fillMaxSize()) {
        bindPauseLoadWhenScrolling(staggeredGridState)

        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Adaptive(minSize = gridCellsMinSize),
            state = staggeredGridState,
            contentPadding = PaddingValues(start = 4.dp, top = 4.dp, end = 4.dp, bottom = 84.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalItemSpacing = 4.dp,
        ) {
            items(
                count = photos.size,
                key = { "${photos[it].originalUrl}:${it}" },
                contentType = { 1 }
            ) { index ->
                val photo = photos[index]
                PhotoGridItem(
                    index = index,
                    photo = photo,
                    staggeredGridMode = true,
                    onClick = { photo1, index1 ->
                        onClick(photos, photo1, index1)
                    },
                )
            }

            if (appendState != null) {
                item(
                    key = "AppendState",
                    span = StaggeredGridItemSpan.FullLine,
                    contentType = 2
                ) {
                    AppendState(appendState)
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