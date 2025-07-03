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
import app.cash.paging.PagingSourceLoadResultError
import app.cash.paging.PagingState
import app.cash.paging.createPagingSourceLoadResultPage
import com.github.panpf.zoomimage.sample.data.api.Response
import com.github.panpf.zoomimage.sample.data.api.pexels.PexelsApi
import com.github.panpf.zoomimage.sample.data.api.pexels.PexelsPhoto
import com.github.panpf.zoomimage.sample.ui.model.Photo

class PexelsPhotoListPagingSource(val pexelsApi: PexelsApi) : PagingSource<Int, Photo>() {

    private val keySet = HashSet<String>()  // Compose LazyVerticalGrid does not allow a key repeat

    override fun getRefreshKey(state: PagingState<Int, Photo>): Int = 0

    override suspend fun load(params: PagingSourceLoadParams<Int>): PagingSourceLoadResult<Int, Photo> {
        val pageNumber = (params.key ?: 0).coerceAtLeast(1)
        val response = try {
            pexelsApi.curated(pageNumber, params.loadSize)
        } catch (e: Exception) {
            e.printStackTrace()
            return PagingSourceLoadResultError(e)
        }

        return if (response is Response.Success) {
            val pexelsPhotos = response.body.photos
            val photos = pexelsPhotos.map { it.toPhoto() }
            val filteredPhotos = photos.filter { keySet.add(it.originalUrl) }
            val nextKey = if (pexelsPhotos.isNotEmpty()) pageNumber + 1 else null
            createPagingSourceLoadResultPage(filteredPhotos, null, nextKey)
        } else {
            response as Response.Error
            PagingSourceLoadResultError(Exception("Http error: ${response.throwable?.message}"))
        }
    }

    private fun PexelsPhoto.toPhoto(): Photo = Photo(
        originalUrl = src.original,
        mediumUrl = src.large,
        thumbnailUrl = src.medium,
        width = width,
        height = height,
    )
}