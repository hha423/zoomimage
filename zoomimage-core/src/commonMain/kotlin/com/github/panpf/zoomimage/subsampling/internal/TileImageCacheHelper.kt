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

package com.github.panpf.zoomimage.subsampling.internal

import com.github.panpf.zoomimage.subsampling.ImageInfo
import com.github.panpf.zoomimage.subsampling.TileImage
import com.github.panpf.zoomimage.subsampling.TileImageCache

/**
 * Assist [TileManager] to obtain and store Bitmap from [TileImageCache]
 *
 * @see com.github.panpf.zoomimage.core.common.test.subsampling.internal.TileImageCacheHelperTest
 */
class TileImageCacheHelper {

    var disabled: Boolean = false
    var tileImageCache: TileImageCache? = null

    fun get(key: String): TileImage? {
        val disabled = disabled
        val tileMemoryCache = tileImageCache
        if (disabled || tileMemoryCache == null) {
            return null
        }
        return tileMemoryCache.get(key)
    }

    fun put(
        key: String,
        tileImage: TileImage,
        imageUrl: String,
        imageInfo: ImageInfo,
    ): TileImage? {
        val disabled = disabled
        val tileMemoryCache = tileImageCache
        return if (!disabled && tileMemoryCache != null) {
            tileMemoryCache.put(
                key = key,
                tileImage = tileImage,
                imageUrl = imageUrl,
                imageInfo = imageInfo
            )
        } else {
            null
        }
    }
}