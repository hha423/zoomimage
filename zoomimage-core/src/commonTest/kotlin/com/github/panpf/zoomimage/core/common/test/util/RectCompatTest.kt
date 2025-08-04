package com.github.panpf.zoomimage.core.common.test.util

import com.github.panpf.zoomimage.util.IntRectCompat
import com.github.panpf.zoomimage.util.OffsetCompat
import com.github.panpf.zoomimage.util.RectCompat
import com.github.panpf.zoomimage.util.ScaleFactorCompat
import com.github.panpf.zoomimage.util.SizeCompat
import com.github.panpf.zoomimage.util.containsWithDelta
import com.github.panpf.zoomimage.util.div
import com.github.panpf.zoomimage.util.flip
import com.github.panpf.zoomimage.util.limitTo
import com.github.panpf.zoomimage.util.reverseRotateInSpace
import com.github.panpf.zoomimage.util.rotateInSpace
import com.github.panpf.zoomimage.util.round
import com.github.panpf.zoomimage.util.times
import com.github.panpf.zoomimage.util.toShortString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class RectCompatTest {

    @Test
    fun testToShortString() {
        assertEquals(
            "[10.34x9.77,600.32x500.91]",
            RectCompat(10.342f, 9.765f, 600.321f, 500.906f).toShortString()
        )
        assertEquals(
            "[9.77x10.34,500.91x600.32]",
            RectCompat(9.765f, 10.342f, 500.906f, 600.321f).toShortString()
        )
    }

    @Test
    fun testRound() {
        assertEquals(
            IntRectCompat(10, 9, 600, 501),
            RectCompat(10.342f, 8.765f, 600.321f, 500.906f).round()
        )
        assertEquals(
            IntRectCompat(9, 10, 501, 600),
            RectCompat(8.765f, 10.342f, 500.906f, 600.321f).round()
        )
    }

    @Test
    fun testTimes() {
        assertEquals(
            "[42.9x37.1,1989.9x2687.1]",
            (RectCompat(13f, 7f, 603f, 507f) * ScaleFactorCompat(3.3f, 5.3f)).toShortString()
        )
        assertEquals(
            "[68.9x23.1,3195.9x1673.1]",
            (RectCompat(13f, 7f, 603f, 507f) * ScaleFactorCompat(5.3f, 3.3f)).toShortString()
        )

        assertEquals(
            "[42.9x23.1,1989.9x1673.1]",
            (RectCompat(13f, 7f, 603f, 507f) * 3.3f).toShortString()
        )
        assertEquals(
            "[68.9x37.1,3195.9x2687.1]",
            (RectCompat(13f, 7f, 603f, 507f) * 5.3f).toShortString()
        )
    }

    @Test
    fun testDiv() {
        assertEquals(
            "[17.27x14.91,182.73x95.66]",
            (RectCompat(57f, 79f, 603f, 507f) / ScaleFactorCompat(3.3f, 5.3f)).toShortString()
        )
        assertEquals(
            "[10.75x23.94,113.77x153.64]",
            (RectCompat(57f, 79f, 603f, 507f) / ScaleFactorCompat(5.3f, 3.3f)).toShortString()
        )
        assertEquals(
            "[17.27x23.94,182.73x153.64]",
            (RectCompat(57f, 79f, 603f, 507f) / 3.3f).toShortString()
        )
        assertEquals(
            "[10.75x14.91,113.77x95.66]",
            (RectCompat(57f, 79f, 603f, 507f) / 5.3f).toShortString()
        )
    }

    @Test
    fun testLimitToRect() {
        assertEquals(
            RectCompat(600.5f, 200.2f, 1000.9f, 800.4f),
            RectCompat(600.5f, 200.2f, 1000.9f, 800.4f).limitTo(
                RectCompat(
                    300.6f,
                    100.3f,
                    1200.5f,
                    900.6f
                )
            )
        )

        assertEquals(
            RectCompat(300.6f, 200.2f, 1000.9f, 800.4f),
            RectCompat(200.2f, 200.2f, 1000.9f, 800.4f).limitTo(
                RectCompat(
                    300.6f,
                    100.3f,
                    1200.5f,
                    900.6f
                )
            )
        )
        assertEquals(
            RectCompat(1200.5f, 200.2f, 1000.9f, 800.4f),
            RectCompat(1300.1f, 200.2f, 1000.9f, 800.4f).limitTo(
                RectCompat(
                    300.6f,
                    100.3f,
                    1200.5f,
                    900.6f
                )
            )
        )

        assertEquals(
            RectCompat(600.5f, 100.3f, 1000.9f, 800.4f),
            RectCompat(600.5f, 50.4f, 1000.9f, 800.4f).limitTo(
                RectCompat(
                    300.6f,
                    100.3f,
                    1200.5f,
                    900.6f
                )
            )
        )
        assertEquals(
            RectCompat(600.5f, 900.6f, 1000.9f, 800.4f),
            RectCompat(600.5f, 1000.9f, 1000.9f, 800.4f).limitTo(
                RectCompat(
                    300.6f,
                    100.3f,
                    1200.5f,
                    900.6f
                )
            )
        )

        assertEquals(
            RectCompat(600.5f, 200.2f, 300.6f, 800.4f),
            RectCompat(600.5f, 200.2f, 200.2f, 800.4f).limitTo(
                RectCompat(
                    300.6f,
                    100.3f,
                    1200.5f,
                    900.6f
                )
            )
        )
        assertEquals(
            RectCompat(600.5f, 200.2f, 1200.5f, 800.4f),
            RectCompat(600.5f, 200.2f, 1300.1f, 800.4f).limitTo(
                RectCompat(
                    300.6f,
                    100.3f,
                    1200.5f,
                    900.6f
                )
            )
        )

        assertEquals(
            RectCompat(600.5f, 200.2f, 1000.9f, 100.3f),
            RectCompat(600.5f, 200.2f, 1000.9f, 50.4f).limitTo(
                RectCompat(
                    300.6f,
                    100.3f,
                    1200.5f,
                    900.6f
                )
            )
        )
        assertEquals(
            RectCompat(600.5f, 200.2f, 1000.9f, 900.6f),
            RectCompat(600.5f, 200.2f, 1000.9f, 1000.9f).limitTo(
                RectCompat(
                    300.6f,
                    100.3f,
                    1200.5f,
                    900.6f
                )
            )
        )
    }

    @Test
    fun testLimitToSize() {
        assertEquals(
            RectCompat(600.5f, 200.2f, 1000.9f, 800.4f),
            RectCompat(600.5f, 200.2f, 1000.9f, 800.4f).limitTo(SizeCompat(1200.5f, 900.6f))
        )

        assertEquals(
            RectCompat(0f, 200.2f, 1000.9f, 800.4f),
            RectCompat(-600.5f, 200.2f, 1000.9f, 800.4f).limitTo(SizeCompat(1200.5f, 900.6f))
        )
        assertEquals(
            RectCompat(1200.5f, 200.2f, 1000.9f, 800.4f),
            RectCompat(1300.1f, 200.2f, 1000.9f, 800.4f).limitTo(SizeCompat(1200.5f, 900.6f))
        )

        assertEquals(
            RectCompat(600.5f, 0f, 1000.9f, 800.4f),
            RectCompat(600.5f, -200.2f, 1000.9f, 800.4f).limitTo(SizeCompat(1200.5f, 900.6f))
        )
        assertEquals(
            RectCompat(600.5f, 900.6f, 1000.9f, 800.4f),
            RectCompat(600.5f, 1000.9f, 1000.9f, 800.4f).limitTo(SizeCompat(1200.5f, 900.6f))
        )

        assertEquals(
            RectCompat(600.5f, 200.2f, 0f, 800.4f),
            RectCompat(600.5f, 200.2f, -1000.9f, 800.4f).limitTo(SizeCompat(1200.5f, 900.6f))
        )
        assertEquals(
            RectCompat(600.5f, 200.2f, 1200.5f, 800.4f),
            RectCompat(600.5f, 200.2f, 1300.1f, 800.4f).limitTo(SizeCompat(1200.5f, 900.6f))
        )

        assertEquals(
            RectCompat(600.5f, 200.2f, 1000.9f, 0f),
            RectCompat(600.5f, 200.2f, 1000.9f, -800.4f).limitTo(SizeCompat(1200.5f, 900.6f))
        )
        assertEquals(
            RectCompat(600.5f, 200.2f, 1000.9f, 900.6f),
            RectCompat(600.5f, 200.2f, 1000.9f, 1000.9f).limitTo(SizeCompat(1200.5f, 900.6f))
        )
    }

    @Test
    fun testRotateInSpace() {
        val spaceSize = SizeCompat(1000f, 700f)

        listOf(0, 0 - 360, 0 + 360, 0 - 360 - 360).forEach { rotation ->
            assertEquals(
                expected = RectCompat(100.2f, 200.7f, 600.9f, 500.4f).toShortString(),
                actual = RectCompat(100.2f, 200.7f, 600.9f, 500.4f).rotateInSpace(
                    spaceSize,
                    rotation
                )
                    .toShortString(),
                message = "rotation: $rotation",
            )
        }

        listOf(90, 90 - 360, 90 + 360, 90 - 360 - 360).forEach { rotation ->
            assertEquals(
                expected = RectCompat(199.6f, 100.2f, 499.3f, 600.9f).toShortString(),
                actual = RectCompat(100.2f, 200.7f, 600.9f, 500.4f).rotateInSpace(
                    spaceSize,
                    rotation
                )
                    .toShortString(),
                message = "rotation: $rotation",
            )
        }

        listOf(180, 180 - 360, 180 + 360, 180 - 360 - 360).forEach { rotation ->
            assertEquals(
                expected = RectCompat(399.1f, 199.6f, 899.8f, 499.3f).toShortString(),
                actual = RectCompat(100.2f, 200.7f, 600.9f, 500.4f).rotateInSpace(
                    spaceSize,
                    rotation
                )
                    .toShortString(),
                message = "rotation: $rotation",
            )
        }

        listOf(270, 270 - 360, 270 + 360, 270 - 360 - 360).forEach { rotation ->
            assertEquals(
                expected = RectCompat(200.7f, 399.1f, 500.4f, 899.8f).toShortString(),
                actual = RectCompat(100.2f, 200.7f, 600.9f, 500.4f).rotateInSpace(
                    spaceSize,
                    rotation
                )
                    .toShortString(),
                message = "rotation: $rotation",
            )
        }

        listOf(360, 360 - 360, 360 + 360, 360 - 360 - 360).forEach { rotation ->
            assertEquals(
                expected = RectCompat(100.2f, 200.7f, 600.9f, 500.4f).toShortString(),
                actual = RectCompat(100.2f, 200.7f, 600.9f, 500.4f).rotateInSpace(
                    spaceSize,
                    rotation
                )
                    .toShortString(),
                message = "rotation: $rotation",
            )
        }

        assertFailsWith(IllegalArgumentException::class) {
            RectCompat(100.2f, 200.7f, 600.9f, 500.4f).rotateInSpace(spaceSize, -1)
        }
        assertFailsWith(IllegalArgumentException::class) {
            RectCompat(100.2f, 200.7f, 600.9f, 500.4f).rotateInSpace(spaceSize, 1)
        }
        assertFailsWith(IllegalArgumentException::class) {
            RectCompat(100.2f, 200.7f, 600.9f, 500.4f).rotateInSpace(spaceSize, 89)
        }
        assertFailsWith(IllegalArgumentException::class) {
            RectCompat(100.2f, 200.7f, 600.9f, 500.4f).rotateInSpace(spaceSize, 91)
        }
        assertFailsWith(IllegalArgumentException::class) {
            RectCompat(100.2f, 200.7f, 600.9f, 500.4f).rotateInSpace(spaceSize, 179)
        }
        assertFailsWith(IllegalArgumentException::class) {
            RectCompat(100.2f, 200.7f, 600.9f, 500.4f).rotateInSpace(spaceSize, 191)
        }
        assertFailsWith(IllegalArgumentException::class) {
            RectCompat(100.2f, 200.7f, 600.9f, 500.4f).rotateInSpace(spaceSize, 269)
        }
        assertFailsWith(IllegalArgumentException::class) {
            RectCompat(100.2f, 200.7f, 600.9f, 500.4f).rotateInSpace(spaceSize, 271)
        }
        assertFailsWith(IllegalArgumentException::class) {
            RectCompat(100.2f, 200.7f, 600.9f, 500.4f).rotateInSpace(spaceSize, 359)
        }
        assertFailsWith(IllegalArgumentException::class) {
            RectCompat(100.2f, 200.7f, 600.9f, 500.4f).rotateInSpace(spaceSize, 361)
        }
    }

    @Test
    fun testReverseRotateInSpace() {
        val spaceSize = SizeCompat(1000f, 700f)
        val rect = RectCompat(100.2f, 200.7f, 600.9f, 500.4f)

        listOf(0, 0 - 360, 0 + 360, 0 - 360 - 360)
            .forEach { rotation ->
                val rotatedSize = rect.rotateInSpace(spaceSize, rotation)
                assertEquals(
                    expected = rect,
                    actual = rotatedSize,
                    message = "rotation: $rotation",
                )
                assertEquals(
                    expected = rect,
                    actual = rotatedSize.reverseRotateInSpace(spaceSize, rotation),
                    message = "rotation: $rotation",
                )
            }

        listOf(90, 90 - 360, 90 + 360, 90 - 360 - 360)
            .forEach { rotation ->
                val rotatedSize = rect.rotateInSpace(spaceSize, rotation)
                assertNotEquals(
                    illegal = rect,
                    actual = rotatedSize,
                    message = "rotation: $rotation",
                )
                assertEquals(
                    expected = rect.toShortString(),
                    actual = rotatedSize.reverseRotateInSpace(spaceSize, rotation).toShortString(),
                    message = "rotation: $rotation",
                )
            }

        listOf(180, 180 - 360, 180 + 360, 180 - 360 - 360)
            .forEach { rotation ->
                val rotatedSize = rect.rotateInSpace(spaceSize, rotation)
                assertNotEquals(
                    illegal = rect,
                    actual = rotatedSize,
                    message = "rotation: $rotation",
                )
                assertEquals(
                    expected = rect.toShortString(),
                    actual = rotatedSize.reverseRotateInSpace(spaceSize, rotation).toShortString(),
                    message = "rotation: $rotation",
                )
            }

        listOf(270, 270 - 360, 270 + 360, 270 - 360 - 360)
            .forEach { rotation ->
                val rotatedSize = rect.rotateInSpace(spaceSize, rotation)
                assertNotEquals(
                    illegal = rect,
                    actual = rotatedSize,
                    message = "rotation: $rotation",
                )
                assertEquals(
                    expected = rect.toShortString(),
                    actual = rotatedSize.reverseRotateInSpace(spaceSize, rotation).toShortString(),
                    message = "rotation: $rotation",
                )
            }

        listOf(360, 360 - 360, 360 + 360, 360 - 360 - 360)
            .forEach { rotation ->
                val rotatedSize = rect.rotateInSpace(spaceSize, rotation)
                assertEquals(
                    expected = rect,
                    actual = rotatedSize,
                    message = "rotation: $rotation",
                )
                assertEquals(
                    expected = rect.toShortString(),
                    actual = rotatedSize.reverseRotateInSpace(spaceSize, rotation).toShortString(),
                    message = "rotation: $rotation",
                )
            }
    }

    @Test
    fun testFlip() {
        val spaceSize = SizeCompat(1000f, 700f)
        val rect = RectCompat(100.2f, 200.7f, 600.9f, 400.4f)

        assertEquals(
            RectCompat(399.1f, 200.7f, 899.8f, 400.4f).toShortString(),
            rect.flip(spaceSize, vertical = false).toShortString()
        )
        assertEquals(
            RectCompat(100.2f, 299.6f, 600.9f, 499.3f),
            rect.flip(spaceSize, vertical = true)
        )
    }

    @Test
    fun testContainsWithDelta() {
        val rect = RectCompat(100f, 200f, 300f, 400f)
        rect.containsWithDelta(OffsetCompat.Zero, 0f)
        rect.containsWithDelta(OffsetCompat.Zero, 1f)
        assertFailsWith(IllegalArgumentException::class) {
            rect.containsWithDelta(OffsetCompat.Zero, -1f)
        }

        assertTrue(rect.containsWithDelta(OffsetCompat(100f, 200f)))
        assertTrue(rect.containsWithDelta(OffsetCompat(100f, 399.999f)))
        assertTrue(rect.containsWithDelta(OffsetCompat(299.999f, 200f)))
        assertTrue(rect.containsWithDelta(OffsetCompat(299.999f, 399.999f)))

        assertFalse(rect.containsWithDelta(OffsetCompat(100f - 0.05f, 200f)))
        assertFalse(rect.containsWithDelta(OffsetCompat(100f, 200f - 0.05f)))
        assertFalse(rect.containsWithDelta(OffsetCompat(100f, 400f)))
        assertFalse(rect.containsWithDelta(OffsetCompat(300f, 200f)))
        assertFalse(rect.containsWithDelta(OffsetCompat(299.999f, 400f)))
        assertFalse(rect.containsWithDelta(OffsetCompat(300f, 399.999f)))

        assertTrue(rect.containsWithDelta(OffsetCompat(100f - 0.05f, 200f), delta = 0.1f))
        assertTrue(rect.containsWithDelta(OffsetCompat(100f, 200f - 0.05f), delta = 0.1f))
        assertTrue(rect.containsWithDelta(OffsetCompat(100f, 400f), delta = 0.1f))
        assertTrue(rect.containsWithDelta(OffsetCompat(300f, 200f), delta = 0.1f))
        assertTrue(rect.containsWithDelta(OffsetCompat(299.999f, 400f), delta = 0.1f))
        assertTrue(rect.containsWithDelta(OffsetCompat(300f, 399.999f), delta = 0.1f))
    }
}