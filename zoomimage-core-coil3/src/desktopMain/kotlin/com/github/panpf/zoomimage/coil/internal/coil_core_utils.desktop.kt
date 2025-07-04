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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.Buffer
import okio.Path
import okio.Path.Companion.toPath
import okio.Source
import okio.Timeout
import okio.buffer
import java.io.File
import java.nio.ByteBuffer

/**
 * Convert coil model to [ImageSource.Factory] for desktop platform
 *
 * @see com.github.panpf.zoomimage.core.coil3.desktop.test.internal.CoilCoreUtilsDesktopTest.testDataToImageSource
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

        data is File -> {
            ImageSource.fromFile(data).toFactory()
        }

        data is ByteArray -> {
            ImageSource.fromByteArray(data).toFactory()
        }

        data is ByteBuffer -> {
            val byteArray: ByteArray = withContext(Dispatchers.IO) {
                data.asSource().buffer().use { it.readByteArray() }
            }
            ImageSource.fromByteArray(byteArray).toFactory()
        }

        else -> {
            null
        }
    }
}

/**
 * @see com.github.panpf.zoomimage.core.coil3.desktop.test.internal.CoilCoreUtilsDesktopTest.testDataToImageSource
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

internal fun ByteBuffer.asSource() = object : Source {
    private val buffer = this@asSource.slice()
    private val len = buffer.capacity()

    override fun close() = Unit

    override fun read(sink: Buffer, byteCount: Long): Long {
        if (buffer.position() == len) return -1
        val pos = buffer.position()
        val newLimit = (pos + byteCount).toInt().coerceAtMost(len)
        buffer.limit(newLimit)
        return sink.write(buffer).toLong()
    }

    override fun timeout() = Timeout.NONE
}