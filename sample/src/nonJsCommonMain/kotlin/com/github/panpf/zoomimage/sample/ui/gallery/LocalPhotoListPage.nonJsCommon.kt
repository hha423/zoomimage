package com.github.panpf.zoomimage.sample.ui.gallery

import androidx.compose.runtime.Composable
import androidx.paging.cachedIn
import app.cash.paging.Pager
import app.cash.paging.PagingConfig
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import com.github.panpf.sketch.LocalPlatformContext
import com.github.panpf.sketch.PlatformContext
import com.github.panpf.sketch.Sketch
import com.github.panpf.zoomimage.sample.ui.gridCellsMinSize
import org.koin.compose.koinInject

@Composable
actual fun LocalPhotoListPage(screen: Screen) {
    val navigator = LocalNavigator.current!!
    val context = LocalPlatformContext.current
    val sketch: Sketch = koinInject()
    val screenModel = screen.rememberScreenModel {
        LocalPhotoListScreenModel(context, sketch)
    }
    PagingPhotoList(
        photoPagingFlow = screenModel.pagingFlow,
        gridCellsMinSize = gridCellsMinSize,
        onClick = { photos, _, index ->
            val params = buildPhotoPagerScreenParams(photos, index)
            navigator.push(PhotoPagerScreen(params))
        },
    )
}

class LocalPhotoListScreenModel(context: PlatformContext, sketch: Sketch) : ScreenModel {

    val pagingFlow = Pager(
        config = PagingConfig(
            pageSize = 40,
            enablePlaceholders = false,
        ),
        initialKey = 0,
        pagingSourceFactory = {
            LocalPhotoListPagingSource(context, sketch)
        }
    ).flow.cachedIn(screenModelScope)
}