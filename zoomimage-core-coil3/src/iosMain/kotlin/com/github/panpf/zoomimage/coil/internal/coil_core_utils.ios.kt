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

package com.github.panpf.zoomimage.coil.internal

import coil3.ImageLoader
import coil3.PlatformContext
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.toUri
import com.github.panpf.zoomimage.coil.CoilHttpImageSource
import com.github.panpf.zoomimage.subsampling.ImageSource
import com.github.panpf.zoomimage.subsampling.fromByteArray
import com.github.panpf.zoomimage.subsampling.fromFile
import com.github.panpf.zoomimage.subsampling.toFactory
import okio.Path
import okio.Path.Companion.toPath
import platform.Foundation.NSURL

/**
 * Convert coil model to [ImageSource.Factory] for iOS platform
 *
 * @see com.github.panpf.zoomimage.core.coil3.ios.test.internal.CoilCoreUtilsIosTest.testDataToImageSource
 */
actual suspend fun dataToImageSource(
    context: PlatformContext,
    imageLoader: ImageLoader,
    request: ImageRequest,
): ImageSource.Factory? {
    val data = request.data
    val uri = when (data) {
        is String -> data.toUri()
        is coil3.Uri -> data
        is NSURL -> data.toString().toUri()
        else -> null
    }
    return when {
        uri != null && (uri.scheme == "http" || uri.scheme == "https") -> {
            CoilHttpImageSource.Factory(context, imageLoader, request)
        }

        // /sdcard/xxx.jpg
        uri != null && uri.scheme?.takeIf { it.isNotEmpty() } == null
                && uri.authority?.takeIf { it.isNotEmpty() } == null
                && uri.path?.startsWith("/") == true -> {
            ImageSource.fromFile(uri.path!!.toPath()).toFactory()
        }

        // file:///sdcard/xxx.jpg
        uri != null && uri.scheme == "file"
                && uri.authority?.takeIf { it.isNotEmpty() } == null
                && uri.path?.startsWith("/") == true -> {
            ImageSource.fromFile(uri.path!!.toPath()).toFactory()
        }

        data is Path -> {
            ImageSource.fromFile(data).toFactory()
        }

        data is ByteArray -> {
            ImageSource.fromByteArray(data).toFactory()
        }

        else -> {
            null
        }
    }
}

/**
 * @see com.github.panpf.zoomimage.core.coil3.ios.test.internal.CoilCoreUtilsIosTest.testDataToImageSource
 */
@Deprecated("Please use dataToImageSource(context, imageLoader, request) instead")
actual suspend fun dataToImageSource(
    context: PlatformContext,
    imageLoader: ImageLoader,
    model: Any
): ImageSource.Factory? = dataToImageSource(
    context = context,
    imageLoader = imageLoader,
    request = ImageRequest.Builder(context)
        .data(model)
        .diskCachePolicy(CachePolicy.ENABLED)
        .networkCachePolicy(CachePolicy.ENABLED)
        .build()
)