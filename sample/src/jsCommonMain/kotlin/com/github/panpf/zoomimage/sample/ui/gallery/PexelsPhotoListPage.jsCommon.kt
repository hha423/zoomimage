package com.github.panpf.zoomimage.sample.ui.gallery

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import com.github.panpf.zoomimage.sample.data.api.Response
import com.github.panpf.zoomimage.sample.data.api.pexels.PexelsApi
import com.github.panpf.zoomimage.sample.data.api.pexels.PexelsPhoto
import com.github.panpf.zoomimage.sample.ui.model.Photo
import org.koin.compose.koinInject

@Composable
actual fun PexelsPhotoListPage(screen: Screen) {
    val navigator = LocalNavigator.current!!
    val pexelsApi: PexelsApi = koinInject()
    PhotoList(
        initialPageStart = 1,
        pageSize = 80,
        load = { pageStart: Int, pageSize: Int ->
            pexelsApi.curated(pageStart, pageSize).let { response ->
                if (response is Response.Success) {
                    response.body.photos.map { it.toPhoto() }
                } else {
                    emptyList()
                }
            }
        },
        calculateNextPageStart = { currentPageStart: Int, _: Int ->
            currentPageStart + 1
        },
        gridCellsMinSize = 150.dp,
        onClick = { photos1, _, index ->
            val params = buildPhotoPagerScreenParams(photos1, index)
            navigator.push(PhotoPagerScreen(params))
        }
    )
}

private fun PexelsPhoto.toPhoto(): Photo = Photo(
    originalUrl = src.original,
    mediumUrl = src.large,
    thumbnailUrl = src.medium,
    width = width,
    height = height,
)