package com.github.panpf.zoomimage.core.coil3.desktop.test

import coil3.ImageLoader.Builder
import coil3.PlatformContext
import com.github.panpf.zoomimage.coil.CoilHttpImageSource
import com.github.panpf.zoomimage.coil.CoilHttpImageSource.Factory
import com.github.panpf.zoomimage.util.IntSizeCompat
import kotlinx.coroutines.runBlocking
import okio.buffer
import org.jetbrains.skia.Image
import org.jetbrains.skia.impl.use
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class CoilHttpImageSourceTest {

    @Test
    fun testKey() {
        val imageUri = "https://www.example.com/image.jpg"
        CoilHttpImageSource(imageUri) {
            throw UnsupportedOperationException()
        }.apply {
            assertEquals(imageUri, key)
        }
    }

    @Test
    fun testEqualsAndHashCode() {
        val imageUri1 = "https://www.example.com/image1.jpg"
        val imageUri2 = "https://www.example.com/image2.jpg"

        val imageSource1 = CoilHttpImageSource(imageUri1) {
            throw UnsupportedOperationException()
        }
        val imageSource12 = CoilHttpImageSource(imageUri1) {
            throw UnsupportedOperationException()
        }
        val imageSource2 = CoilHttpImageSource(imageUri2) {
            throw UnsupportedOperationException()
        }
        val imageSource22 = CoilHttpImageSource(imageUri2) {
            throw UnsupportedOperationException()
        }

        assertEquals(expected = imageSource1, actual = imageSource1)
        assertEquals(expected = imageSource1, actual = imageSource12)
        assertEquals(expected = imageSource2, actual = imageSource22)
        assertNotEquals(illegal = imageSource1, actual = null as Any?)
        assertNotEquals(illegal = imageSource1, actual = Any())
        assertNotEquals(illegal = imageSource1, actual = imageSource2)
        assertNotEquals(illegal = imageSource12, actual = imageSource22)

        assertEquals(
            expected = imageSource1.hashCode(),
            actual = imageSource12.hashCode()
        )
        assertEquals(
            expected = imageSource2.hashCode(),
            actual = imageSource22.hashCode()
        )
        assertNotEquals(
            illegal = imageSource1.hashCode(),
            actual = imageSource2.hashCode()
        )
        assertNotEquals(
            illegal = imageSource12.hashCode(),
            actual = imageSource22.hashCode()
        )
    }

    @Test
    fun testToString() {
        val imageUri1 = "https://www.example.com/image1.jpg"
        val imageUri2 = "https://www.example.com/image2.jpg"

        assertEquals(
            "CoilHttpImageSource('$imageUri1')",
            CoilHttpImageSource(imageUri1) {
                throw UnsupportedOperationException()
            }.toString()
        )
        assertEquals(
            "CoilHttpImageSource('$imageUri2')",
            CoilHttpImageSource(imageUri2) {
                throw UnsupportedOperationException()
            }.toString()
        )
    }

    @Test
    fun testOpenSource() {
        val context = PlatformContext.INSTANCE
        val imageLoader = Builder(context).build()
        val imageUri =
            "https://images.unsplash.com/photo-1721340143289-94be4f77cda4?q=80&w=640&auto=jpeg&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D"
        val diskCache = imageLoader.diskCache!!

        diskCache.clear()
        assertEquals(null, diskCache.openSnapshot(imageUri))

        val imageSourceFactory = Factory(context, imageLoader, imageUri)
        val imageSource = runBlocking {
            imageSourceFactory.create()
        }
        val bytes = imageSource.openSource().buffer().use { it.readByteArray() }
        val imageSize = Image.makeFromEncoded(bytes).use { IntSizeCompat(it.width, it.height) }
        assertEquals(expected = IntSizeCompat(640, 958), actual = imageSize)
        assertNotEquals(
            illegal = null,
            actual = diskCache.openSnapshot(imageUri)?.apply { close() }
        )
    }

    @Test
    fun testFactoryEqualsAndHashCode() {
        val context = PlatformContext.INSTANCE
        val imageLoader = Builder(context).build()
        val imageUri1 = "https://www.example.com/image1.jpg"
        val imageUri2 = "https://www.example.com/image2.jpg"

        val imageSourceFactory1 = Factory(context, imageLoader, imageUri1)
        val imageSourceFactory12 = Factory(context, imageLoader, imageUri1)
        val imageSourceFactory2 = Factory(context, imageLoader, imageUri2)
        val imageSourceFactory22 = Factory(context, imageLoader, imageUri2)

        assertEquals(expected = imageSourceFactory1, actual = imageSourceFactory12)
        assertEquals(expected = imageSourceFactory2, actual = imageSourceFactory22)
        assertNotEquals(illegal = imageSourceFactory1, actual = imageSourceFactory2)
        assertNotEquals(illegal = imageSourceFactory12, actual = imageSourceFactory22)

        assertEquals(
            expected = imageSourceFactory1.hashCode(),
            actual = imageSourceFactory12.hashCode()
        )
        assertEquals(
            expected = imageSourceFactory2.hashCode(),
            actual = imageSourceFactory22.hashCode()
        )
        assertNotEquals(
            illegal = imageSourceFactory1.hashCode(),
            actual = imageSourceFactory2.hashCode()
        )
        assertNotEquals(
            illegal = imageSourceFactory12.hashCode(),
            actual = imageSourceFactory22.hashCode()
        )
    }

    @Test
    fun testFactoryToString() {
        val context = PlatformContext.INSTANCE
        val imageLoader = Builder(context).build()
        val imageUri1 = "https://www.example.com/image1.jpg"
        val imageUri2 = "https://www.example.com/image2.jpg"

        assertEquals(
            "CoilHttpImageSource.Factory('$imageUri1')",
            Factory(context, imageLoader, imageUri1).toString()
        )
        assertEquals(
            "CoilHttpImageSource.Factory('$imageUri2')",
            Factory(context, imageLoader, imageUri2).toString()
        )
    }
}