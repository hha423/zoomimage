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

package com.github.panpf.zoomimage.compose.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.ScaleFactor
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import com.github.panpf.zoomimage.zoom.valueOf
import kotlin.math.abs
import kotlin.math.roundToInt


/**
 * Convert Dp to px
 *
 * @see com.github.panpf.zoomimage.compose.common.test.util.ComposePlatformUtilsTest.testDpToPx
 */
@Composable
internal fun Dp.toPx(): Float {
    return with(LocalDensity.current) { this@toPx.toPx() }
}

/**
 * Convert px to Dp
 *
 * @see com.github.panpf.zoomimage.compose.common.test.util.ComposePlatformUtilsTest.testFloatToDp
 */
@Composable
internal fun Float.toDp(): Dp {
    return with(LocalDensity.current) { this@toDp.toDp() }
}

/**
 * If [t] is not null, create a new Modifier via [block] and combine with the current Modifier, otherwise return the current Modifier
 *
 * @see com.github.panpf.zoomimage.compose.common.test.util.ComposePlatformUtilsTest.thenIfNotNull
 */
internal fun <T> Modifier.thenIfNotNull(t: T?, block: (T) -> Modifier): Modifier {
    return if (t != null) {
        this.then(block(t))
    } else {
        this
    }
}


/* ****************************************** Size ********************************************** */

/**
 * Return short string descriptions, for example: '100.56x900.45'
 *
 * @see com.github.panpf.zoomimage.compose.common.test.util.ComposePlatformUtilsSizeTest.testToShortString
 */
@Stable
internal fun Size.toShortString(): String =
    if (isSpecified) "${width.format(2)}x${height.format(2)}" else "Unspecified"

/**
 * Return true if the size is not empty
 *
 * @see com.github.panpf.zoomimage.compose.common.test.util.ComposePlatformUtilsSizeTest.testIsNotEmpty
 */
@Stable
internal fun Size.isNotEmpty(): Boolean = width > 0f && height > 0f

/**
 * Round a [Size] down to the nearest [Int] coordinates.
 *
 * @see com.github.panpf.zoomimage.compose.common.test.util.ComposePlatformUtilsSizeTest.testRound
 */
@Stable
internal fun Size.round(): IntSize =
    if (isSpecified) IntSize(width.roundToInt(), height.roundToInt()) else IntSize.Zero

/**
 * The size after rotating [rotation] degrees
 *
 * @see com.github.panpf.zoomimage.compose.common.test.util.ComposePlatformUtilsSizeTest.testRotate
 */
@Stable
internal fun Size.rotate(rotation: Int): Size =
    if (rotation % 180 == 0) this else Size(height, width)

/**
 * The size after reverse rotating [rotation] degrees
 *
 * @see com.github.panpf.zoomimage.compose.common.test.util.ComposePlatformUtilsSizeTest.testReverseRotate
 */
@Stable
internal fun Size.reverseRotate(rotation: Int): Size {
    val reverseRotation = (360 - rotation) % 360
    return rotate(reverseRotation)
}

/**
 * Returns true if the aspect ratio of itself and other is the same
 *
 * @see com.github.panpf.zoomimage.compose.common.test.util.ComposePlatformUtilsSizeTest.testIsSameAspectRatio
 */
@Stable
internal fun Size.isSameAspectRatio(other: Size, delta: Float = 0f): Boolean {
    val selfScale = this.width / this.height
    val otherScale = other.width / other.height
    if (selfScale.compareTo(otherScale) == 0) {
        return true
    }
    if (delta != 0f && abs(selfScale - otherScale) <= delta) {
        return true
    }
    return false
}

/**
 * Return the results of two Size addition operations
 *
 * @see com.github.panpf.zoomimage.compose.common.test.util.ComposePlatformUtilsSizeTest.testPlus
 */
@Stable
operator fun Size.plus(other: Size): Size = Size(width + other.width, height + other.height)

/**
 * Return the results of two Size subtraction operations
 *
 * @see com.github.panpf.zoomimage.compose.common.test.util.ComposePlatformUtilsSizeTest.testMinus
 */
@Stable
operator fun Size.minus(other: Size): Size = Size(width - other.width, height - other.height)


/* **************************************** IntSize ********************************************* */

/**
 * Return short string descriptions, for example: '100x200'
 *
 * @see com.github.panpf.zoomimage.compose.common.test.util.ComposePlatformUtilsIntSizeTest.testToShortString
 */
@Stable
internal fun IntSize.toShortString(): String = "${width}x${height}"

/**
 * Return true if the size is empty
 *
 * @see com.github.panpf.zoomimage.compose.common.test.util.ComposePlatformUtilsIntSizeTest.testIsEmpty
 */
@Stable
internal fun IntSize.isEmpty(): Boolean = width <= 0 || height <= 0

/**
 * Return true if the size is not empty
 *
 * @see com.github.panpf.zoomimage.compose.common.test.util.ComposePlatformUtilsIntSizeTest.testIsNotEmpty
 */
@Stable
internal fun IntSize.isNotEmpty(): Boolean = width > 0 && height > 0

/**
 * Returns an IntSize scaled by multiplying [this] by [scaleFactor]
 *
 * @see com.github.panpf.zoomimage.compose.common.test.util.ComposePlatformUtilsIntSizeTest.testTimes
 */
@Stable
internal operator fun IntSize.times(scaleFactor: ScaleFactor): IntSize =
    IntSize(
        width = (this.width * scaleFactor.scaleX).roundToInt(),
        height = (this.height * scaleFactor.scaleY).roundToInt()
    )

/**
 * Returns an IntSize scaled by dividing [this] by [scaleFactor]
 *
 * @see com.github.panpf.zoomimage.compose.common.test.util.ComposePlatformUtilsIntSizeTest.testDiv
 */
@Stable
internal operator fun IntSize.div(scaleFactor: ScaleFactor): IntSize =
    IntSize(
        width = (this.width / scaleFactor.scaleX).roundToInt(),
        height = (this.height / scaleFactor.scaleY).roundToInt()
    )

/**
 * Returns an IntSize scaled by multiplying [this] by [scale]
 *
 * @see com.github.panpf.zoomimage.compose.common.test.util.ComposePlatformUtilsIntSizeTest.testTimes
 */
@Stable
internal operator fun IntSize.times(scale: Float): IntSize =
    IntSize(
        width = (this.width * scale).roundToInt(),
        height = (this.height * scale).roundToInt()
    )

/**
 * Returns an IntSize scaled by dividing [this] by [scale]
 *
 * @see com.github.panpf.zoomimage.compose.common.test.util.ComposePlatformUtilsIntSizeTest.testDiv
 */
@Stable
internal operator fun IntSize.div(scale: Float): IntSize =
    IntSize(
        width = (this.width / scale).roundToInt(),
        height = (this.height / scale).roundToInt()
    )

/**
 * The size after rotating [rotation] degrees
 *
 * @see com.github.panpf.zoomimage.compose.common.test.util.ComposePlatformUtilsIntSizeTest.testRotate
 */
@Stable
internal fun IntSize.rotate(rotation: Int): IntSize {
    return if (rotation % 180 == 0) this else IntSize(height, width)
}

/**
 * The size after reverse rotating [rotation] degrees
 *
 * @see com.github.panpf.zoomimage.compose.common.test.util.ComposePlatformUtilsIntSizeTest.testReverseRotate
 */
@Stable
internal fun IntSize.reverseRotate(rotation: Int): IntSize {
    val reverseRotation = (360 - rotation) % 360
    return rotate(reverseRotation)
}

/**
 * Returns true if the aspect ratio of itself and other is the same
 *
 * @see com.github.panpf.zoomimage.compose.common.test.util.ComposePlatformUtilsIntSizeTest.testIsSameAspectRatio
 */
@Stable
internal fun IntSize.isSameAspectRatio(other: IntSize, delta: Float = 0f): Boolean {
    val selfScale = this.width / this.height.toFloat()
    val otherScale = other.width / other.height.toFloat()
    if (selfScale.compareTo(otherScale) == 0) {
        return true
    }
    if (delta != 0f && abs(selfScale - otherScale) <= delta) {
        return true
    }
    return false
}

/**
 * Linearly interpolate between two [IntSize]s.
 *
 * The [fraction] argument represents position on the timeline, with 0.0 meaning
 * that the interpolation has not started, returning [start] (or something
 * equivalent to [start]), 1.0 meaning that the interpolation has finished,
 * returning [stop] (or something equivalent to [stop]), and values in between
 * meaning that the interpolation is at the relevant point on the timeline
 * between [start] and [stop]. The interpolation can be extrapolated beyond 0.0 and
 * 1.0, so negative values and values greater than 1.0 are valid.
 *
 * @see com.github.panpf.zoomimage.compose.common.test.util.ComposePlatformUtilsIntSizeTest.testLerp
 */
@Stable
internal fun lerp(start: IntSize, stop: IntSize, fraction: Float): IntSize =
    IntSize(
        androidx.compose.ui.util.lerp(start.width, stop.width, fraction),
        androidx.compose.ui.util.lerp(start.height, stop.height, fraction)
    )

/**
 * Returns a copy of this IntOffset instance optionally overriding the
 * x or y parameter
 *
 * @see com.github.panpf.zoomimage.compose.common.test.util.ComposePlatformUtilsIntSizeTest.testCopy
 */
@Stable
internal fun IntSize.copy(width: Int = this.width, height: Int = this.height) =
    IntSize(width = width, height = height)

/**
 * Return the results of two IntSize addition operations
 *
 * @see com.github.panpf.zoomimage.compose.common.test.util.ComposePlatformUtilsIntSizeTest.testPlus
 */
@Stable
operator fun IntSize.plus(other: IntSize): IntSize =
    IntSize(this.width + other.width, this.height + other.height)

/**
 * Return the results of two IntSize subtraction operations
 *
 * @see com.github.panpf.zoomimage.compose.common.test.util.ComposePlatformUtilsIntSizeTest.testMinus
 */
@Stable
operator fun IntSize.minus(other: IntSize): IntSize =
    IntSize(this.width - other.width, this.height - other.height)


/* ***************************************** Offset ********************************************* */

/**
 * Return short string descriptions, for example: '10.01x9.03'
 *
 * @see com.github.panpf.zoomimage.compose.common.test.util.ComposePlatformUtilsOffsetTest.testToShortString
 */
@Stable
internal fun Offset.toShortString(): String =
    if (isSpecified) "${x.format(2)}x${y.format(2)}" else "Unspecified"

/**
 * Multiplication operator.
 *
 * Returns an offset whose coordinates are the coordinates of the
 * left-hand-side operand (an Offset) multiplied by the scalar
 * right-hand-side operand (a Float).
 *
 * @see com.github.panpf.zoomimage.compose.common.test.util.ComposePlatformUtilsOffsetTest.testTimes
 */
@Stable
internal operator fun Offset.times(scaleFactor: ScaleFactor): Offset =
    Offset(x * scaleFactor.scaleX, y * scaleFactor.scaleY)

/**
 * Division operator.
 *
 * Returns an offset whose coordinates are the coordinates of the
 * left-hand-side operand (an Offset) divided by the scalar right-hand-side
 * operand (a Float).
 *
 * @see com.github.panpf.zoomimage.compose.common.test.util.ComposePlatformUtilsOffsetTest.testDiv
 */
@Stable
internal operator fun Offset.div(scaleFactor: ScaleFactor): Offset =
    Offset(x = x / scaleFactor.scaleX, y = y / scaleFactor.scaleY)

/**
 * Rotate the space by [rotation] degrees, and then return the rotated coordinates
 *
 * @see com.github.panpf.zoomimage.compose.common.test.util.ComposePlatformUtilsOffsetTest.testRotateInSpace
 */
@Stable
internal fun Offset.rotateInSpace(spaceSize: Size, rotation: Int): Offset {
    require(rotation % 90 == 0) { "rotation must be a multiple of 90, rotation: $rotation" }
    return when ((rotation % 360).let { if (it < 0) 360 + it else it }) {
        90 -> Offset(x = spaceSize.height - y, y = x)
        180 -> Offset(x = spaceSize.width - x, y = spaceSize.height - y)
        270 -> Offset(x = y, y = spaceSize.width - x)
        else -> this
    }
}

/**
 * Reverse rotate the space by [rotation] degrees, and then returns the reverse rotated coordinates
 *
 * @see com.github.panpf.zoomimage.compose.common.test.util.ComposePlatformUtilsOffsetTest.testReverseRotateInSpace
 */
@Stable
internal fun Offset.reverseRotateInSpace(spaceSize: Size, rotation: Int): Offset {
    val rotatedSpaceSize = spaceSize.rotate(rotation)
    val reverseRotation = (360 - rotation) % 360
    return rotateInSpace(rotatedSpaceSize, reverseRotation)
}

/**
 * Limit the offset to the rectangular extent
 *
 * @see com.github.panpf.zoomimage.compose.common.test.util.ComposePlatformUtilsOffsetTest.testLimitToRect
 */
@Stable
internal fun Offset.limitTo(rect: Rect): Offset {
    return if (x < rect.left || x > rect.right || y < rect.top || y > rect.bottom) {
        Offset(
            x = x.coerceIn(rect.left, rect.right),
            y = y.coerceIn(rect.top, rect.bottom),
        )
    } else {
        this
    }
}

/**
 * Limit offset to 0 to the range of size
 *
 * @see com.github.panpf.zoomimage.compose.common.test.util.ComposePlatformUtilsOffsetTest.testLimitToSize
 */
@Stable
internal fun Offset.limitTo(size: Size): Offset =
    limitTo(Rect(0f, 0f, size.width, size.height))

/**
 * If true is returned, it means Offset is empty
 *
 * @see com.github.panpf.zoomimage.compose.common.test.util.ComposePlatformUtilsOffsetTest.testIsEmpty
 */
@Stable
internal fun Offset.isEmpty(): Boolean = abs(x).format(2) == 0f && abs(y).format(2) == 0f

/**
 * Convert to [Offset]
 *
 * @see com.github.panpf.zoomimage.compose.common.test.util.ComposePlatformUtilsOffsetTest.testToOffset
 */
@Stable
internal fun Size.toOffset(): Offset = Offset(x = width, y = height)


/* ************************************** IntOffset ********************************************* */

/**
 * Return short string descriptions, for example: '10x9'
 *
 * @see com.github.panpf.zoomimage.compose.common.test.util.ComposePlatformUtilsIntOffsetTest.testToShortString
 */
@Stable
internal fun IntOffset.toShortString(): String = "${x}x${y}"

/**
 * Multiplication operator.
 *
 * Returns an IntOffset whose coordinates are the coordinates of the
 * left-hand-side operand (an IntOffset) multiplied by the scalar
 * right-hand-side operand (a Float). The result is rounded to the nearest integer.
 *
 * @see com.github.panpf.zoomimage.compose.common.test.util.ComposePlatformUtilsIntOffsetTest.testTimes
 */
@Stable
internal operator fun IntOffset.times(scaleFactor: ScaleFactor): IntOffset =
    IntOffset(
        x = (x * scaleFactor.scaleX).roundToInt(),
        y = (y * scaleFactor.scaleY).roundToInt()
    )

/**
 * Division operator.
 *
 * Returns an IntOffset whose coordinates are the coordinates of the
 * left-hand-side operand (an IntOffset) divided by the scalar right-hand-side
 * operand (a Float). The result is rounded to the nearest integer.
 *
 * @see com.github.panpf.zoomimage.compose.common.test.util.ComposePlatformUtilsIntOffsetTest.testDiv
 */
@Stable
internal operator fun IntOffset.div(scaleFactor: ScaleFactor): IntOffset =
    IntOffset(
        x = (x / scaleFactor.scaleX).roundToInt(),
        y = (y / scaleFactor.scaleY).roundToInt()
    )

/**
 * Rotate the space by [rotation] degrees, and then return the rotated coordinates
 *
 * @see com.github.panpf.zoomimage.compose.common.test.util.ComposePlatformUtilsIntOffsetTest.testRotateInSpace
 */
@Stable
internal fun IntOffset.rotateInSpace(spaceSize: IntSize, rotation: Int): IntOffset {
    require(rotation % 90 == 0) { "rotation must be a multiple of 90, rotation: $rotation" }
    return when ((rotation % 360).let { if (it < 0) 360 + it else it }) {
        90 -> IntOffset(x = spaceSize.height - y, y = x)
        180 -> IntOffset(x = spaceSize.width - x, y = spaceSize.height - y)
        270 -> IntOffset(x = y, y = spaceSize.width - x)
        else -> this
    }
}

/**
 * Reverse rotate the space by [rotation] degrees, and then returns the reverse rotated coordinates
 *
 * @see com.github.panpf.zoomimage.compose.common.test.util.ComposePlatformUtilsIntOffsetTest.testReverseRotateInSpace
 */
@Stable
internal fun IntOffset.reverseRotateInSpace(spaceSize: IntSize, rotation: Int): IntOffset {
    val rotatedSpaceSize = spaceSize.rotate(rotation)
    val reverseRotation = (360 - rotation) % 360
    return rotateInSpace(rotatedSpaceSize, reverseRotation)
}

/**
 * Limit the offset to the rectangular extent
 *
 * @see com.github.panpf.zoomimage.compose.common.test.util.ComposePlatformUtilsIntOffsetTest.testLimitToRect
 */
@Stable
internal fun IntOffset.limitTo(rect: IntRect): IntOffset {
    return if (x < rect.left || x > rect.right || y < rect.top || y > rect.bottom) {
        IntOffset(
            x = x.coerceIn(rect.left, rect.right),
            y = y.coerceIn(rect.top, rect.bottom),
        )
    } else {
        this
    }
}

/**
 * Limit offset to 0 to the range of size
 *
 * @see com.github.panpf.zoomimage.compose.common.test.util.ComposePlatformUtilsIntOffsetTest.testLimitToSize
 */
@Stable
internal fun IntOffset.limitTo(size: IntSize): IntOffset =
    limitTo(IntRect(0, 0, size.width, size.height))

/**
 * Convert to [IntOffset]
 *
 * @see com.github.panpf.zoomimage.compose.common.test.util.ComposePlatformUtilsIntOffsetTest.testToIntOffset
 */
@Stable
internal fun IntSize.toIntOffset(): IntOffset = IntOffset(x = width, y = height)


/* ******************************************* Rect ********************************************* */

/**
 * Return short string descriptions, for example: '[0.01x0.34,100.67x200.02]'
 *
 * @see com.github.panpf.zoomimage.compose.common.test.util.ComposePlatformUtilsRectTest.testToShortString
 */
@Stable
internal fun Rect.toShortString(): String =
    "[${left.format(2)}x${top.format(2)},${right.format(2)}x${bottom.format(2)}]"

/**
 * Rounds a [Rect] to an [IntRect]
 *
 * @see com.github.panpf.zoomimage.compose.common.test.util.ComposePlatformUtilsRectTest.testRound
 */
@Stable
internal fun Rect.round(): IntRect = IntRect(
    left = left.roundToInt(),
    top = top.roundToInt(),
    right = right.roundToInt(),
    bottom = bottom.roundToInt()
)

/**
 * Returns an Rect scaled by multiplying [scale]
 *
 * @see com.github.panpf.zoomimage.compose.common.test.util.ComposePlatformUtilsRectTest.testTimes
 */
@Stable
internal operator fun Rect.times(scale: Float): Rect =
    Rect(
        left = (left * scale),
        top = (top * scale),
        right = (right * scale),
        bottom = (bottom * scale),
    )

/**
 * Returns an Rect scaled by multiplying [scaleFactor]
 *
 * @see com.github.panpf.zoomimage.compose.common.test.util.ComposePlatformUtilsRectTest.testTimes
 */
@Stable
internal operator fun Rect.times(scaleFactor: ScaleFactor): Rect =
    Rect(
        left = (left * scaleFactor.scaleX),
        top = (top * scaleFactor.scaleY),
        right = (right * scaleFactor.scaleX),
        bottom = (bottom * scaleFactor.scaleY),
    )

/**
 * Returns an Rect scaled by dividing [scale]
 *
 * @see com.github.panpf.zoomimage.compose.common.test.util.ComposePlatformUtilsRectTest.testDiv
 */
@Stable
internal operator fun Rect.div(scale: Float): Rect =
    Rect(
        left = (left / scale),
        top = (top / scale),
        right = (right / scale),
        bottom = (bottom / scale),
    )

/**
 * Returns an Rect scaled by dividing [scaleFactor]
 *
 * @see com.github.panpf.zoomimage.compose.common.test.util.ComposePlatformUtilsRectTest.testDiv
 */
@Stable
internal operator fun Rect.div(scaleFactor: ScaleFactor): Rect =
    Rect(
        left = (left / scaleFactor.scaleX),
        top = (top / scaleFactor.scaleY),
        right = (right / scaleFactor.scaleX),
        bottom = (bottom / scaleFactor.scaleY),
    )

/**
 * Limit the offset to the rectangular extent
 *
 * @see com.github.panpf.zoomimage.compose.common.test.util.ComposePlatformUtilsRectTest.testLimitToRect
 */
@Stable
internal fun Rect.limitTo(rect: Rect): Rect =
    if (this.left < rect.left || this.left > rect.right
        || this.top < rect.top || this.top > rect.bottom
        || this.right < rect.left || this.right > rect.right
        || this.bottom < rect.top || this.bottom > rect.bottom
    ) {
        Rect(
            left = left.coerceIn(rect.left, rect.right),
            top = top.coerceIn(rect.top, rect.bottom),
            right = right.coerceIn(rect.left, rect.right),
            bottom = bottom.coerceIn(rect.top, rect.bottom),
        )
    } else {
        this
    }

/**
 * Limit Rect to 0 to the range of size
 *
 * @see com.github.panpf.zoomimage.compose.common.test.util.ComposePlatformUtilsRectTest.testLimitToSize
 */
@Stable
internal fun Rect.limitTo(size: Size): Rect = limitTo(Rect(0f, 0f, size.width, size.height))

/**
 * Rotate the space by [rotation] degrees, and then return the rotated Rect
 *
 * @see com.github.panpf.zoomimage.compose.common.test.util.ComposePlatformUtilsRectTest.testRotateInSpace
 */
@Stable
internal fun Rect.rotateInSpace(spaceSize: Size, rotation: Int): Rect {
    require(rotation % 90 == 0) { "rotation must be a multiple of 90, rotation: $rotation" }
    return when ((rotation % 360).let { if (it < 0) 360 + it else it }) {
        90 -> {
            Rect(
                left = spaceSize.height - this.bottom,
                top = this.left,
                right = spaceSize.height - this.top,
                bottom = this.right
            )
        }

        180 -> {
            Rect(
                left = spaceSize.width - this.right,
                top = spaceSize.height - this.bottom,
                right = spaceSize.width - this.left,
                bottom = spaceSize.height - this.top,
            )
        }

        270 -> {
            Rect(
                left = this.top,
                top = spaceSize.width - this.right,
                right = this.bottom,
                bottom = spaceSize.width - this.left,
            )
        }

        else -> this
    }
}

/**
 * Reverse rotate the space by [rotation] degrees, and then returns the reverse rotated Rect
 *
 * @see com.github.panpf.zoomimage.compose.common.test.util.ComposePlatformUtilsRectTest.testReverseRotateInSpace
 */
@Stable
internal fun Rect.reverseRotateInSpace(spaceSize: Size, rotation: Int): Rect {
    val rotatedSpaceSize = spaceSize.rotate(rotation)
    val reverseRotation = (360 - rotation) % 360
    return rotateInSpace(rotatedSpaceSize, reverseRotation)
}

/**
 * Flip this rect horizontally or vertically within a given container
 *
 * @see com.github.panpf.zoomimage.compose.common.test.util.ComposePlatformUtilsRectTest.testFlip
 */
fun Rect.flip(spaceSize: Size, vertical: Boolean = false): Rect {
    return if (!vertical) {
        Rect(
            left = spaceSize.width - right,
            top = top,
            right = spaceSize.width - left,
            bottom = bottom
        )
    } else {
        Rect(
            left = left,
            top = spaceSize.height - bottom,
            right = right,
            bottom = spaceSize.height - top
        )
    }
}


/* **************************************** IntRect ********************************************* */

/**
 * Return short string descriptions, for example: '[0x0,500x400]'
 *
 * @see com.github.panpf.zoomimage.compose.common.test.util.ComposePlatformUtilsIntRectTest.testToShortString
 */
@Stable
internal fun IntRect.toShortString(): String = "[${left}x${top},${right}x${bottom}]"

/**
 * Returns an IntRect scaled by multiplying [scale]
 *
 * @see com.github.panpf.zoomimage.compose.common.test.util.ComposePlatformUtilsIntRectTest.testTimes
 */
@Stable
internal operator fun IntRect.times(scale: Float): IntRect =
    IntRect(
        left = (left * scale).roundToInt(),
        top = (top * scale).roundToInt(),
        right = (right * scale).roundToInt(),
        bottom = (bottom * scale).roundToInt(),
    )

/**
 * Returns an IntRect scaled by multiplying [scaleFactor]
 *
 * @see com.github.panpf.zoomimage.compose.common.test.util.ComposePlatformUtilsIntRectTest.testTimes
 */
@Stable
internal operator fun IntRect.times(scaleFactor: ScaleFactor): IntRect =
    IntRect(
        left = (left * scaleFactor.scaleX).roundToInt(),
        top = (top * scaleFactor.scaleY).roundToInt(),
        right = (right * scaleFactor.scaleX).roundToInt(),
        bottom = (bottom * scaleFactor.scaleY).roundToInt(),
    )

/**
 * Returns an IntRect scaled by dividing [scale]
 *
 * @see com.github.panpf.zoomimage.compose.common.test.util.ComposePlatformUtilsIntRectTest.testDiv
 */
@Stable
internal operator fun IntRect.div(scale: Float): IntRect =
    IntRect(
        left = (left / scale).roundToInt(),
        top = (top / scale).roundToInt(),
        right = (right / scale).roundToInt(),
        bottom = (bottom / scale).roundToInt(),
    )

/**
 * Returns an IntRect scaled by dividing [scaleFactor]
 *
 * @see com.github.panpf.zoomimage.compose.common.test.util.ComposePlatformUtilsIntRectTest.testDiv
 */
@Stable
internal operator fun IntRect.div(scaleFactor: ScaleFactor): IntRect =
    IntRect(
        left = (left / scaleFactor.scaleX).roundToInt(),
        top = (top / scaleFactor.scaleY).roundToInt(),
        right = (right / scaleFactor.scaleX).roundToInt(),
        bottom = (bottom / scaleFactor.scaleY).roundToInt(),
    )

/**
 * Limit the offset to the rectangular extent
 *
 * @see com.github.panpf.zoomimage.compose.common.test.util.ComposePlatformUtilsIntRectTest.testLimitToRect
 */
@Stable
internal fun IntRect.limitTo(rect: IntRect): IntRect =
    if (this.left < rect.left || this.left > rect.right
        || this.top < rect.top || this.top > rect.bottom
        || this.right < rect.left || this.right > rect.right
        || this.bottom < rect.top || this.bottom > rect.bottom
    ) {
        IntRect(
            left = left.coerceIn(rect.left, rect.right),
            top = top.coerceIn(rect.top, rect.bottom),
            right = right.coerceIn(rect.left, rect.right),
            bottom = bottom.coerceIn(rect.top, rect.bottom),
        )
    } else {
        this
    }

/**
 * Limit Rect to 0 to the range of size
 *
 * @see com.github.panpf.zoomimage.compose.common.test.util.ComposePlatformUtilsIntRectTest.testLimitToSize
 */
@Stable
internal fun IntRect.limitTo(size: IntSize): IntRect =
    limitTo(IntRect(0, 0, size.width, size.height))

/**
 * Rotate the space by [rotation] degrees, and then return the rotated Rect
 *
 * @see com.github.panpf.zoomimage.compose.common.test.util.ComposePlatformUtilsIntRectTest.testRotateInSpace
 */
@Stable
internal fun IntRect.rotateInSpace(spaceSize: IntSize, rotation: Int): IntRect {
    require(rotation % 90 == 0) { "rotation must be a multiple of 90, rotation: $rotation" }
    return when ((rotation % 360).let { if (it < 0) 360 + it else it }) {
        90 -> {
            IntRect(
                left = spaceSize.height - this.bottom,
                top = this.left,
                right = spaceSize.height - this.top,
                bottom = this.right
            )
        }

        180 -> {
            IntRect(
                left = spaceSize.width - this.right,
                top = spaceSize.height - this.bottom,
                right = spaceSize.width - this.left,
                bottom = spaceSize.height - this.top,
            )
        }

        270 -> {
            IntRect(
                left = this.top,
                top = spaceSize.width - this.right,
                right = this.bottom,
                bottom = spaceSize.width - this.left,
            )
        }

        else -> this
    }
}

/**
 * Reverse rotate the space by [rotation] degrees, and then returns the reverse rotated Rect
 *
 * @see com.github.panpf.zoomimage.compose.common.test.util.ComposePlatformUtilsIntRectTest.testReverseRotateInSpace
 */
@Stable
internal fun IntRect.reverseRotateInSpace(spaceSize: IntSize, rotation: Int): IntRect {
    val rotatedSpaceSize = spaceSize.rotate(rotation)
    val reverseRotation = (360 - rotation) % 360
    return rotateInSpace(rotatedSpaceSize, reverseRotation)
}

/**
 * Flip this rect horizontally or vertically within a given container
 */
fun IntRect.flip(spaceSize: IntSize, vertical: Boolean = false): IntRect {
    return if (!vertical) {
        IntRect(
            left = spaceSize.width - right,
            top = top,
            right = spaceSize.width - left,
            bottom = bottom
        )
    } else {
        IntRect(
            left = left,
            top = spaceSize.height - bottom,
            right = right,
            bottom = spaceSize.height - top
        )
    }
}


/* ************************************** ScaleFactor ******************************************* */

/**
 * Return short string descriptions, for example: '3.45x9.87'
 *
 * @see com.github.panpf.zoomimage.compose.common.test.util.ComposePlatformUtilsScaleFactorTest.testToShortString
 */
@Stable
internal fun ScaleFactor.toShortString(): String = "${scaleX.format(2)}x${scaleY.format(2)}"

/**
 * Create a ScaleFactor, scaleX and scaleY are both [scale]
 *
 * @see com.github.panpf.zoomimage.compose.common.test.util.ComposePlatformUtilsScaleFactorTest.testCreate
 */
@Stable
internal fun ScaleFactor(scale: Float): ScaleFactor = ScaleFactor(scale, scale)

/**
 * The scaling factor that remains the same scale, that is, scaleX and scaleY are both 1f
 *
 * @see com.github.panpf.zoomimage.compose.common.test.util.ComposePlatformUtilsScaleFactorTest.testOrigin
 */
@Stable
internal val ScaleFactor.Companion.Origin: ScaleFactor
    get() = scaleFactorOrigin
private val scaleFactorOrigin by lazy { ScaleFactor(scaleX = 1f, scaleY = 1f) }

/**
 * Returns an ScaleFactor scaled by multiplying [scaleFactor]
 *
 * @see com.github.panpf.zoomimage.compose.common.test.util.ComposePlatformUtilsScaleFactorTest.testTimes
 */
@Stable
internal operator fun ScaleFactor.times(scaleFactor: ScaleFactor) =
    ScaleFactor(scaleX * scaleFactor.scaleX, scaleY * scaleFactor.scaleY)

/**
 * Returns an ScaleFactor scaled by dividing [scaleFactor]
 *
 * @see com.github.panpf.zoomimage.compose.common.test.util.ComposePlatformUtilsScaleFactorTest.testDiv
 */
@Stable
internal operator fun ScaleFactor.div(scaleFactor: ScaleFactor) =
    ScaleFactor(scaleX / scaleFactor.scaleX, scaleY / scaleFactor.scaleY)

/**
 * Returns true if the scaling is 1
 *
 * @see com.github.panpf.zoomimage.compose.common.test.util.ComposePlatformUtilsScaleFactorTest.testIsOrigin
 */
internal fun ScaleFactor.isOrigin(): Boolean = scaleX.format(2) == 1f && scaleY.format(2) == 1f


/* ************************************** TransformOrigin *************************************** */

/**
 * Return short string descriptions, for example: '0.52x0.52'
 *
 * @see com.github.panpf.zoomimage.compose.common.test.util.ComposePlatformUtilsTransformOriginTest.testToShortString
 */
@Stable
internal fun TransformOrigin.toShortString(): String =
    "${pivotFractionX.format(2)}x${pivotFractionY.format(2)}"

/**
 * [TransformOrigin] constant to indicate that the top start of the content should
 * be used for rotation and scale transformations
 *
 * @see com.github.panpf.zoomimage.compose.common.test.util.ComposePlatformUtilsTransformOriginTest.testTopStart
 */
@Stable
internal val TransformOrigin.Companion.TopStart: TransformOrigin
    get() = transformOriginTopStart
private val transformOriginTopStart by lazy { TransformOrigin(0f, 0f) }

/**
 * Return a new [TransformOrigin] with the width and height multiplied by the [operand]
 *
 * @see com.github.panpf.zoomimage.compose.common.test.util.ComposePlatformUtilsTransformOriginTest.testTimes
 */
@Stable
internal operator fun TransformOrigin.times(operand: Float) =
    TransformOrigin(pivotFractionX * operand, pivotFractionY * operand)

/**
 * Return a new [TransformOrigin] with the width and height dividing by the [operand]
 *
 * @see com.github.panpf.zoomimage.compose.common.test.util.ComposePlatformUtilsTransformOriginTest.testDiv
 */
@Stable
internal operator fun TransformOrigin.div(operand: Float) =
    TransformOrigin(pivotFractionX / operand, pivotFractionY / operand)

/**
 * Multiplication operator with [IntSize].
 *
 * Return a new [IntSize] with the width and height multiplied by the [TransformOrigin.pivotFractionX] and
 * [TransformOrigin.pivotFractionY] respectively
 *
 * @see com.github.panpf.zoomimage.compose.common.test.util.ComposePlatformUtilsTransformOriginTest.testTimes
 */
@Stable
internal operator fun IntSize.times(origin: TransformOrigin): IntSize =
    IntSize(
        width = (this.width * origin.pivotFractionX).roundToInt(),
        height = (this.height * origin.pivotFractionY).roundToInt()
    )

/**
 * Multiplication operator with [Size].
 *
 * Return a new [Size] with the width and height multiplied by the [TransformOrigin.pivotFractionX] and
 * [TransformOrigin.pivotFractionY] respectively
 *
 * @see com.github.panpf.zoomimage.compose.common.test.util.ComposePlatformUtilsTransformOriginTest.testTimes
 */
@Stable
internal operator fun Size.times(origin: TransformOrigin): Size =
    Size(
        width = this.width * origin.pivotFractionX,
        height = this.height * origin.pivotFractionY
    )

/**
 * Linearly interpolate between two [TransformOrigin] parameters
 *
 * The [fraction] argument represents position on the timeline, with 0.0 meaning
 * that the interpolation has not started, returning [start] (or something
 * equivalent to [start]), 1.0 meaning that the interpolation has finished,
 * returning [stop] (or something equivalent to [stop]), and values in between
 * meaning that the interpolation is at the relevant point on the timeline
 * between [start] and [stop]. The interpolation can be extrapolated beyond 0.0 and
 * 1.0, so negative values and values greater than 1.0 are valid (and can
 * easily be generated by curves).
 *
 * Values for [fraction] are usually obtained from an [Animation<Float>], such as
 * an `AnimationController`.
 *
 * @see com.github.panpf.zoomimage.compose.common.test.util.ComposePlatformUtilsTransformOriginTest.testLerp
 */
@Stable
internal fun lerp(
    start: TransformOrigin,
    stop: TransformOrigin,
    fraction: Float
): TransformOrigin {
    return TransformOrigin(
        pivotFractionX = androidx.compose.ui.util.lerp(
            start.pivotFractionX,
            stop.pivotFractionX,
            fraction
        ),
        pivotFractionY = androidx.compose.ui.util.lerp(
            start.pivotFractionY,
            stop.pivotFractionY,
            fraction
        )
    )
}


/* ************************************** ContentScale ****************************************** */

/**
 * Returns the name of [ContentScale], which can also be converted back via the [valueOf] method
 *
 * @see com.github.panpf.zoomimage.compose.common.test.util.ComposePlatformUtilsContentScaleTest.testName
 */
@Stable
internal val ContentScale.name: String
    get() = when (this) {
        ContentScale.FillWidth -> "FillWidth"
        ContentScale.FillHeight -> "FillHeight"
        ContentScale.FillBounds -> "FillBounds"
        ContentScale.Fit -> "Fit"
        ContentScale.Crop -> "Crop"
        ContentScale.Inside -> "Inside"
        ContentScale.None -> "None"
        else -> "Unknown ContentScale: $this"
    }

/**
 * Returns the [ContentScale] corresponding to the given [name], or throws [IllegalArgumentException]. see [name] property
 *
 * @see com.github.panpf.zoomimage.compose.common.test.util.ComposePlatformUtilsContentScaleTest.testValueOf
 */
@Stable
internal fun ContentScale.Companion.valueOf(name: String): ContentScale {
    return when (name) {
        "FillWidth" -> FillWidth
        "FillHeight" -> FillHeight
        "FillBounds" -> FillBounds
        "Fit" -> Fit
        "Crop" -> Crop
        "Inside" -> Inside
        "None" -> None
        else -> throw IllegalArgumentException("Unknown ContentScale name: $name")
    }
}


/* ************************************** Alignment ********************************************* */

/**
 * Returns the name of [Alignment], which can also be converted back via the [valueOf] method
 *
 * @see com.github.panpf.zoomimage.compose.common.test.util.ComposePlatformUtilsAlignmentTest.testName
 */
@Stable
internal val Alignment.name: String
    get() = when (this) {
        Alignment.TopStart -> "TopStart"
        Alignment.TopCenter -> "TopCenter"
        Alignment.TopEnd -> "TopEnd"
        Alignment.CenterStart -> "CenterStart"
        Alignment.Center -> "Center"
        Alignment.CenterEnd -> "CenterEnd"
        Alignment.BottomStart -> "BottomStart"
        Alignment.BottomCenter -> "BottomCenter"
        Alignment.BottomEnd -> "BottomEnd"
        else -> "Unknown Alignment: $this"
    }

/**
 * Returns the [Alignment] corresponding to the given [name], or throws [IllegalArgumentException]. see [name] property
 *
 * @see com.github.panpf.zoomimage.compose.common.test.util.ComposePlatformUtilsAlignmentTest.testValueOf
 */
@Stable
internal fun Alignment.Companion.valueOf(name: String): Alignment {
    return when (name) {
        "TopStart" -> TopStart
        "TopCenter" -> TopCenter
        "TopEnd" -> TopEnd
        "CenterStart" -> CenterStart
        "Center" -> Center
        "CenterEnd" -> CenterEnd
        "BottomStart" -> BottomStart
        "BottomCenter" -> BottomCenter
        "BottomEnd" -> BottomEnd
        else -> throw IllegalArgumentException("Unknown alignment name: $name")
    }
}

/**
 * If true is returned, this [Alignment] is the horizontal starting position
 *
 * @see com.github.panpf.zoomimage.compose.common.test.util.ComposePlatformUtilsAlignmentTest.testIsStart
 */
@Stable
internal val Alignment.isStart: Boolean
    get() = this == Alignment.TopStart
            || this == Alignment.CenterStart
            || this == Alignment.BottomStart

/**
 * If true is returned, this [Alignment] is the horizontal center position
 *
 * @see com.github.panpf.zoomimage.compose.common.test.util.ComposePlatformUtilsAlignmentTest.testIsHorizontalCenter
 */
@Stable
internal val Alignment.isHorizontalCenter: Boolean
    get() = this == Alignment.TopCenter
            || this == Alignment.Center
            || this == Alignment.BottomCenter

/**
 * If true is returned, this [Alignment] is the horizontal ending position
 *
 * @see com.github.panpf.zoomimage.compose.common.test.util.ComposePlatformUtilsAlignmentTest.testIsEnd
 */
@Stable
internal val Alignment.isEnd: Boolean
    get() = this == Alignment.TopEnd
            || this == Alignment.CenterEnd
            || this == Alignment.BottomEnd

/**
 * If true is returned, this [Alignment] is the horizontal and vertical center position
 *
 * @see com.github.panpf.zoomimage.compose.common.test.util.ComposePlatformUtilsAlignmentTest.testIsCenter
 */
@Stable
internal val Alignment.isCenter: Boolean
    get() = this == Alignment.Center

/**
 * If true is returned, this [Alignment] is the vertical starting position
 *
 * @see com.github.panpf.zoomimage.compose.common.test.util.ComposePlatformUtilsAlignmentTest.testIsTop
 */
@Stable
internal val Alignment.isTop: Boolean
    get() = this == Alignment.TopStart
            || this == Alignment.TopCenter
            || this == Alignment.TopEnd

/**
 * If true is returned, this [Alignment] is the vertical center position
 *
 * @see com.github.panpf.zoomimage.compose.common.test.util.ComposePlatformUtilsAlignmentTest.testIsVerticalCenter
 */
@Stable
internal val Alignment.isVerticalCenter: Boolean
    get() = this == Alignment.CenterStart
            || this == Alignment.Center
            || this == Alignment.CenterEnd

/**
 * If true is returned, this [Alignment] is the vertical ending position
 *
 * @see com.github.panpf.zoomimage.compose.common.test.util.ComposePlatformUtilsAlignmentTest.testIsBottom
 */
@Stable
internal val Alignment.isBottom: Boolean
    get() = this == Alignment.BottomStart
            || this == Alignment.BottomCenter
            || this == Alignment.BottomEnd

/**
 * If [layoutDirection] is [LayoutDirection.Rtl], returns the horizontally flipped [Alignment], otherwise returns itself
 *
 * @see com.github.panpf.zoomimage.compose.common.test.util.ComposePlatformUtilsAlignmentTest.testRtlFlipped
 */
@Stable
fun Alignment.rtlFlipped(layoutDirection: LayoutDirection? = null): Alignment {
    return if (layoutDirection == null || layoutDirection == LayoutDirection.Rtl) {
        when (this) {
            Alignment.TopStart -> Alignment.TopEnd
            Alignment.TopCenter -> Alignment.TopCenter
            Alignment.TopEnd -> Alignment.TopStart
            Alignment.CenterStart -> Alignment.CenterEnd
            Alignment.Center -> Alignment.Center
            Alignment.CenterEnd -> Alignment.CenterStart
            Alignment.BottomStart -> Alignment.BottomEnd
            Alignment.BottomCenter -> Alignment.BottomCenter
            Alignment.BottomEnd -> Alignment.BottomStart
            else -> this
        }
    } else {
        this
    }
}