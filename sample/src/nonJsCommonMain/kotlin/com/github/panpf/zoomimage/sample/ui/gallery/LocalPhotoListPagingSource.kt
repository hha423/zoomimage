/*
 * Copyright (C) 2024 panpf <panpfpanpf@outlook.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.panpf.zoomimage.sample.ui.gallery

import app.cash.paging.PagingSource
import app.cash.paging.PagingSourceLoadParams
import app.cash.paging.PagingSourceLoadResult
import app.cash.paging.PagingState
import app.cash.paging.createPagingSourceLoadResultPage
import com.github.panpf.sketch.Sketch
import com.github.panpf.zoomimage.sample.data.builtinImages
import com.github.panpf.zoomimage.sample.data.localImages
import com.github.panpf.zoomimage.sample.data.readImageInfoOrNull
import com.github.panpf.zoomimage.sample.ui.model.Photo

class LocalPhotoListPagingSource(
    val sketch: Sketch
) : PagingSource<Int, Photo>() {

    private val keySet = HashSet<String>()  // Compose LazyVerticalGrid does not allow a key repeat
    private var _builtInPhotos: List<String>? = null

    override fun getRefreshKey(state: PagingState<Int, Photo>): Int = 0

    private suspend fun getBuiltInPhotos(): List<String> {
        return _builtInPhotos ?: builtinImages(sketch.context).map { it.uri }.also {
            _builtInPhotos = it
        }
    }

    override suspend fun load(params: PagingSourceLoadParams<Int>): PagingSourceLoadResult<Int, Photo> {
        val startPosition = params.key ?: 0
        val pageSize = params.loadSize

        val builtInPhotos = getBuiltInPhotos()
        val photos = if (startPosition < builtInPhotos.size) {
            val fromBuiltInPhotos = builtInPhotos.subList(
                fromIndex = startPosition,
                toIndex = (startPosition + pageSize).coerceAtMost(builtInPhotos.size)
            )
            val fromPhotoAlbumPhotos = if (fromBuiltInPhotos.size < pageSize) {
                val photoAlbumStartPosition = 0
                val photoAlbumPageSize = pageSize - fromBuiltInPhotos.size
                localImages(sketch.context, photoAlbumStartPosition, photoAlbumPageSize)
            } else {
                emptyList()
            }
            fromBuiltInPhotos.toMutableList().apply {
                addAll(fromPhotoAlbumPhotos)
            }
        } else {
            val photoAlbumStartPosition = startPosition - builtInPhotos.size
            localImages(sketch.context, photoAlbumStartPosition, pageSize)
        }.map { uri -> uriToPhoto(uri) }
        val nextKey = if (photos.isNotEmpty()) startPosition + pageSize else null
        val filteredPhotos = photos.filter { keySet.add(it.originalUrl) }
        return createPagingSourceLoadResultPage(
            filteredPhotos,
            null,
            nextKey
        )
    }

    private suspend fun uriToPhoto(uri: String): Photo {
        val imageInfo = readImageInfoOrNull(
            context = sketch.context,
            sketch = sketch,
            uri = uri,
        )
        return Photo(
            originalUrl = uri,
            mediumUrl = null,
            thumbnailUrl = null,
            width = imageInfo?.width,
            height = imageInfo?.height,
        )
    }
}