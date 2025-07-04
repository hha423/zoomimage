package com.github.panpf.zoomimage.core.coil3.desktop.test.internal

import coil3.ImageLoader
import coil3.PlatformContext
import coil3.request.CachePolicy
import coil3.toUri
import com.github.panpf.zoomimage.coil.CoilHttpImageSource
import com.github.panpf.zoomimage.coil.internal.dataToImageSource
import com.github.panpf.zoomimage.subsampling.ByteArrayImageSource
import com.github.panpf.zoomimage.subsampling.FileImageSource
import com.github.panpf.zoomimage.subsampling.toFactory
import kotlinx.coroutines.test.runTest
import okio.Buffer
import okio.Path.Companion.toPath
import okio.Source
import okio.Timeout
import okio.buffer
import java.io.File
import java.net.URL
import java.nio.ByteBuffer
import kotlin.test.Test
import kotlin.test.assertEquals

class CoilCoreUtilsDesktopTest {

    private fun buildRequest(data: Any): coil3.request.ImageRequest {
        val context = PlatformContext.INSTANCE
        return coil3.request.ImageRequest.Builder(context)
            .data(data)
            .diskCachePolicy(CachePolicy.ENABLED)
            .networkCachePolicy(CachePolicy.ENABLED)
            .build()
    }

    @Test
    fun testDataToImageSource() = runTest {
        val context = PlatformContext.INSTANCE
        val imageLoader = ImageLoader.Builder(context).build()
        try {
            val httpUri = "http://www.example.com/image.jpg"
            assertEquals(
                expected = CoilHttpImageSource.Factory(context, imageLoader, buildRequest(httpUri)),
                actual = dataToImageSource(context, imageLoader, buildRequest(httpUri))
            )
            assertEquals(
                expected = CoilHttpImageSource.Factory(
                    context,
                    imageLoader,
                    buildRequest(httpUri.toUri())
                ),
                actual = dataToImageSource(
                    context,
                    imageLoader,
                    buildRequest(httpUri.toUri())
                )
            )
            assertEquals(
                expected = null,
                actual = dataToImageSource(context, imageLoader, buildRequest(URL(httpUri)))
            )

            val httpsUri = "https://www.example.com/image.jpg"
            assertEquals(
                expected = CoilHttpImageSource.Factory(
                    context,
                    imageLoader,
                    buildRequest(httpsUri)
                ),
                actual = dataToImageSource(context, imageLoader, buildRequest(httpsUri))
            )
            assertEquals(
                expected = CoilHttpImageSource.Factory(
                    context,
                    imageLoader,
                    buildRequest(httpsUri.toUri())
                ),
                actual = dataToImageSource(
                    context,
                    imageLoader,
                    buildRequest(httpsUri.toUri())
                )
            )
            assertEquals(
                expected = null,
                actual = dataToImageSource(context, imageLoader, buildRequest(URL(httpsUri)))
            )

            val pathUri = "/sdcard/image.jpg"
            assertEquals(
                expected = FileImageSource(pathUri.toPath()).toFactory(),
                actual = dataToImageSource(context, imageLoader, buildRequest(pathUri))
            )
            assertEquals(
                expected = FileImageSource(pathUri.toPath()).toFactory(),
                actual = dataToImageSource(
                    context,
                    imageLoader,
                    buildRequest(pathUri.toUri())
                )
            )

            val fileUri = "file:///sdcard/image.jpg"
            assertEquals(
                expected = FileImageSource(fileUri.toUri().path!!.toPath()).toFactory(),
                actual = dataToImageSource(context, imageLoader, buildRequest(fileUri))
            )
            assertEquals(
                expected = FileImageSource(fileUri.toUri().path!!.toPath()).toFactory(),
                actual = dataToImageSource(
                    context,
                    imageLoader,
                    buildRequest(fileUri.toUri())
                )
            )

            val path = "/sdcard/image.jpg".toPath()
            assertEquals(
                expected = FileImageSource(path).toFactory(),
                actual = dataToImageSource(context, imageLoader, buildRequest(path))
            )

            val file = File("/sdcard/image.jpg")
            assertEquals(
                expected = FileImageSource(file).toFactory(),
                actual = dataToImageSource(context, imageLoader, buildRequest(file))
            )

            val byteArray = "Hello".toByteArray()
            assertEquals(
                expected = ByteArrayImageSource(byteArray).toFactory(),
                actual = dataToImageSource(context, imageLoader, buildRequest(byteArray))
            )

            val byteBuffer = ByteBuffer.wrap("Hello".toByteArray())
            assertEquals(
                expected = ByteArrayImageSource(
                    byteBuffer.asSource().buffer().use { it.readByteArray() }).toFactory(),
                actual = dataToImageSource(context, imageLoader, buildRequest(byteBuffer))
            )
        } finally {
            imageLoader.shutdown()
        }
    }

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
}