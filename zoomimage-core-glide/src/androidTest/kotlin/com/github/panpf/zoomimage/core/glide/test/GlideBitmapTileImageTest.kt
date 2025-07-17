package com.github.panpf.zoomimage.core.glide.test

import android.graphics.Bitmap
import androidx.test.platform.app.InstrumentationRegistry
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.EngineResourceWrapper
import com.bumptech.glide.load.engine.GlideEngine
import com.bumptech.glide.load.engine.createGlideEngine
import com.bumptech.glide.load.engine.newEngineKey
import com.github.panpf.zoomimage.glide.GlideBitmapTileImage
import com.github.panpf.zoomimage.glide.internal.toLogString
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertSame

class GlideBitmapTileImageTest {

    @Test
    fun testConstructor() {
        val context = InstrumentationRegistry.getInstrumentation().context
        val glide = Glide.get(context)
        val glideEngine: GlideEngine by lazy {
            createGlideEngine(glide)!!
        }

        val bitmap1 = Bitmap.createBitmap(1101, 703, Bitmap.Config.ARGB_8888)
        val bitmap2 = Bitmap.createBitmap(507, 1305, Bitmap.Config.ARGB_8888)
        val resource1 =
            EngineResourceWrapper(glideEngine.newEngineResource(bitmap1, newEngineKey("key1")))
        val resource2 =
            EngineResourceWrapper(glideEngine.newEngineResource(bitmap2, newEngineKey("key1")))

        GlideBitmapTileImage(resource1).apply {
            assertSame(bitmap1, bitmap)
        }
        GlideBitmapTileImage(resource2).apply {
            assertSame(bitmap2, bitmap)
        }
    }

    @Test
    fun testWidthHeightByteCount() {
        val context = InstrumentationRegistry.getInstrumentation().context
        val glide = Glide.get(context)
        val glideEngine: GlideEngine by lazy {
            createGlideEngine(glide)!!
        }

        val bitmap1 = Bitmap.createBitmap(1101, 703, Bitmap.Config.ARGB_8888)
        val bitmap12 = Bitmap.createBitmap(1101, 703, Bitmap.Config.RGB_565)
        val bitmap2 = Bitmap.createBitmap(507, 1305, Bitmap.Config.ARGB_8888)
        val resource1 =
            EngineResourceWrapper(glideEngine.newEngineResource(bitmap1, newEngineKey("key1")))
        val resource12 =
            EngineResourceWrapper(glideEngine.newEngineResource(bitmap12, newEngineKey("key1")))
        val resource2 =
            EngineResourceWrapper(glideEngine.newEngineResource(bitmap2, newEngineKey("key1")))

        val tileImage1 = GlideBitmapTileImage(resource1).apply {
            assertEquals(resource1.bitmap.width, width)
            assertEquals(resource1.bitmap.height, height)
            assertEquals(resource1.bitmap.byteCount.toLong(), byteCount)
        }
        val tileImage12 = GlideBitmapTileImage(resource12).apply {
            assertEquals(resource12.bitmap.width, width)
            assertEquals(resource12.bitmap.height, height)
            assertEquals(resource12.bitmap.byteCount.toLong(), byteCount)
        }
        val tileImage2 = GlideBitmapTileImage(resource2).apply {
            assertEquals(resource2.bitmap.width, width)
            assertEquals(resource2.bitmap.height, height)
            assertEquals(resource2.bitmap.byteCount.toLong(), byteCount)
        }

        assertEquals(tileImage1.width, tileImage12.width)
        assertEquals(tileImage1.height, tileImage12.height)
        assertNotEquals(tileImage1.byteCount, tileImage12.byteCount)

        assertNotEquals(tileImage1.width, tileImage2.width)
        assertNotEquals(tileImage1.height, tileImage2.height)
        assertNotEquals(tileImage1.byteCount, tileImage2.byteCount)

        assertNotEquals(tileImage2.width, tileImage12.width)
        assertNotEquals(tileImage2.height, tileImage12.height)
        assertNotEquals(tileImage2.byteCount, tileImage12.byteCount)
    }

    @Test
    fun testRecycle() {
        val context = InstrumentationRegistry.getInstrumentation().context
        val glide = Glide.get(context)
        val glideEngine: GlideEngine by lazy {
            createGlideEngine(glide)!!
        }

        val bitmap1 = Bitmap.createBitmap(1101, 703, Bitmap.Config.ARGB_8888)
        val resource =
            EngineResourceWrapper(glideEngine.newEngineResource(bitmap1, newEngineKey("key1")))
        val tileImage = GlideBitmapTileImage(resource)
        assertEquals(false, tileImage.isRecycled)
        tileImage.recycle()
        assertEquals(true, tileImage.isRecycled)
    }

    @Test
    fun testEqualsAndHashCode() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().context
        val glide = Glide.get(context)
        val glideEngine: GlideEngine by lazy {
            createGlideEngine(glide)!!
        }

        val bitmap1 = Bitmap.createBitmap(1101, 703, Bitmap.Config.ARGB_8888)
        val bitmap2 = Bitmap.createBitmap(507, 1305, Bitmap.Config.ARGB_8888)
        val resource1 =
            EngineResourceWrapper(glideEngine.newEngineResource(bitmap1, newEngineKey("key1")))
        val resource2 =
            EngineResourceWrapper(glideEngine.newEngineResource(bitmap2, newEngineKey("key1")))

        val tileImage1 = GlideBitmapTileImage(resource1)
        val tileImage12 = GlideBitmapTileImage(resource1)
        val tileImage2 = GlideBitmapTileImage(resource2)

        assertEquals(expected = tileImage1, actual = tileImage1)
        assertEquals(expected = tileImage1, actual = tileImage12)
        assertNotEquals(illegal = tileImage1, actual = null as Any?)
        assertNotEquals(illegal = tileImage1, actual = Any())
        assertNotEquals(illegal = tileImage1, actual = tileImage2)

        assertEquals(expected = tileImage1.hashCode(), actual = tileImage12.hashCode())
        assertNotEquals(illegal = tileImage1.hashCode(), actual = tileImage2.hashCode())
    }

    @Test
    fun testToString() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().context
        val glide = Glide.get(context)
        val glideEngine: GlideEngine by lazy {
            createGlideEngine(glide)!!
        }

        val bitmap1 = Bitmap.createBitmap(1101, 703, Bitmap.Config.ARGB_8888)
        val bitmap2 = Bitmap.createBitmap(507, 1305, Bitmap.Config.ARGB_8888)
        val resource1 =
            EngineResourceWrapper(glideEngine.newEngineResource(bitmap1, newEngineKey("key1")))
        val resource2 =
            EngineResourceWrapper(glideEngine.newEngineResource(bitmap2, newEngineKey("key1")))

        val tileImage1 = GlideBitmapTileImage(resource1)
        val tileImage2 = GlideBitmapTileImage(resource2)

        assertEquals(
            expected = "GlideBitmapTileImage(bitmap=${resource1.bitmap.toLogString()})",
            actual = tileImage1.toString()
        )
        assertEquals(
            expected = "GlideBitmapTileImage(bitmap=${resource2.bitmap.toLogString()})",
            actual = tileImage2.toString()
        )
    }
}