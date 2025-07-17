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

package com.github.panpf.zoomimage.sketch

import com.github.panpf.sketch.BitmapImage
import com.github.panpf.sketch.Sketch
import com.github.panpf.sketch.asImage
import com.github.panpf.sketch.cache.ImageCacheValue
import com.github.panpf.zoomimage.subsampling.BitmapTileImage
import com.github.panpf.zoomimage.subsampling.ImageInfo
import com.github.panpf.zoomimage.subsampling.TileImage
import com.github.panpf.zoomimage.subsampling.TileImageCache

/**
 * Implement [TileImageCache] based on Sketch on Android platforms
 *
 * @see com.github.panpf.zoomimage.core.sketch4.android.test.SketchTileImageCacheTest
 */
actual class SketchTileImageCache actual constructor(
    private val sketch: Sketch,
) : TileImageCache {

    actual override fun get(key: String): TileImage? {
        val cacheValue = sketch.memoryCache[key] ?: return null
        cacheValue as ImageCacheValue
        val bitmapImage = cacheValue.image as BitmapImage
        val bitmap = bitmapImage.bitmap
        return BitmapTileImage(bitmap)
    }

    actual override fun put(
        key: String,
        tileImage: TileImage,
        imageUrl: String,
        imageInfo: ImageInfo,
    ): TileImage? {
        tileImage as BitmapTileImage
        val bitmap = tileImage.bitmap
        val cacheValue = ImageCacheValue(bitmap.asImage())
        sketch.memoryCache.put(key, cacheValue)
        return null
    }
}