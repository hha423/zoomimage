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

package com.github.panpf.zoomimage.util

import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.round
import kotlin.math.roundToInt
import kotlin.math.roundToLong

/**
 * Format the float number to the specified number of decimal places
 *
 * @see com.github.panpf.zoomimage.core.common.test.util.CoreUtilsTest.testFormat
 */
internal fun Float.format(newScale: Int): Float {
    return if (this.isNaN()) {
        this
    } else {
        val multiplier = 10.0.pow(newScale)
        (round(this * multiplier) / multiplier).toFloat()
    }
}

/**
 * Convert the object to a hexadecimal string
 *
 * @see com.github.panpf.zoomimage.core.common.test.util.CoreUtilsTest.testToHexString
 */
internal fun Any.toHexString(): String = this.hashCode().toString(16)

/**
 * Close the Closeable quietly
 *
 * @see com.github.panpf.zoomimage.core.common.test.util.CoreUtilsTest.testCloseQuietly
 */
internal fun okio.Closeable.closeQuietly() {
    try {
        close()
    } catch (e: RuntimeException) {
        throw e
    } catch (_: Exception) {
    }
}

/**
 * Close the Closeable quietly
 *
 * @see com.github.panpf.zoomimage.core.common.test.util.CoreUtilsTest.testCloseQuietly
 */
internal fun AutoCloseable.closeQuietly() {
    try {
        close()
    } catch (e: RuntimeException) {
        throw e
    } catch (_: Exception) {
    }
}


/**
 * Linearly interpolate between [start] and [stop] with [fraction] fraction between them.
 *
 * Copy from androidx/compose/ui/util/MathHelpers.kt
 */
internal fun lerp(start: Float, stop: Float, fraction: Float): Float {
    return (1 - fraction) * start + fraction * stop
}

/**
 * Linearly interpolate between [start] and [stop] with [fraction] fraction between them.
 *
 * Copy from androidx/compose/ui/util/MathHelpers.kt
 */
internal fun lerp(start: Int, stop: Int, fraction: Float): Int {
    return start + ((stop - start) * fraction.toDouble()).roundToInt()
}

/**
 * Linearly interpolate between [start] and [stop] with [fraction] fraction between them.
 *
 * Copy from androidx/compose/ui/util/MathHelpers.kt
 */
internal fun lerp(start: Long, stop: Long, fraction: Float): Long {
    return start + ((stop - start) * fraction.toDouble()).roundToLong()
}

/**
 * Convert the float to a string with the specified number of decimal places
 *
 * Copy from androidx/compose/ui/geometry/GeometryUtils.kt
 */
internal fun Float.toStringAsFixed(digits: Int): String {
    val clampedDigits: Int = kotlin.math.max(digits, 0) // Accept positive numbers and 0 only
    val pow = 10f.pow(clampedDigits)
    val shifted = this * pow // shift the given value by the corresponding power of 10
    val decimal = shifted - shifted.toInt() // obtain the decimal of the shifted value
    // Manually round up if the decimal value is greater than or equal to 0.5f.
    // because kotlin.math.round(0.5f) rounds down
    val roundedShifted = if (decimal >= 0.5f) {
        shifted.toInt() + 1
    } else {
        shifted.toInt()
    }

    val rounded = roundedShifted / pow // divide off the corresponding power of 10 to shift back
    return if (clampedDigits > 0) {
        // If we have any decimal points, convert the float to a string
        rounded.toString()
    } else {
        // If we do not have any decimal points, return the int
        // based string representation
        rounded.toInt().toString()
    }
}

/**
 * Compare two versions
 *
 * The following version formats are supported:
 * 1.0, 1.0.0, 1.0.0.1, 1.0.0-snapshot1, 1.0.0-snapshot.1, 1.0.0-snapshot01, 1.0.0-alpha01, 1.0.0-beta01, 1.0.0-rc01
 *
 * @see com.github.panpf.zoomimage.core.common.test.util.CoreUtilsTest.testCompareVersions
 */
internal fun compareVersions(version1: String, version2: String): Int {
    val (numbers1, suffix1) = version1.split("-", limit = 2)
        .let { it[0].trim() to it.getOrNull(1)?.trim() }
    val (numbers2, suffix2) = version2.split("-", limit = 2)
        .let { it[0].trim() to it.getOrNull(1)?.trim() }

    // Compare numeric parts
    val numberParts1 = numbers1.split(".")
    val numberParts2 = numbers2.split(".")
    val maxLength = maxOf(numberParts1.size, numberParts2.size)
    var numberCompareResult = 0
    for (i in 0 until maxLength) {
        val numberPar1 = numberParts1.getOrNull(i)?.toIntOrNull() ?: 0
        val numberPart2 = numberParts2.getOrNull(i)?.toIntOrNull() ?: 0
        if (numberPar1 != numberPart2) {
            numberCompareResult = numberPar1.compareTo(numberPart2)
            break
        }
    }
    if (numberCompareResult != 0) {
        return numberCompareResult
    }

    // Compare suffix parts
    val suffixCompareResult: Int
    if (suffix1 == suffix2) {
        suffixCompareResult = 0
    } else if (suffix1 == null) {
        suffixCompareResult = 1
    } else if (suffix2 == null) {
        suffixCompareResult = -1
    } else {
        val lowercaseSuffix1 = suffix1.lowercase()
        val lowercaseSuffix2 = suffix2.lowercase()
        val suffixTypes = listOf("snapshot", "alpha", "beta", "rc")
            .mapIndexed { index, s -> index to s }
        val suffixType1 = suffixTypes.find { lowercaseSuffix1.startsWith(it.second) }
        val suffixType2 = suffixTypes.find { lowercaseSuffix2.startsWith(it.second) }
        if (suffixType1 != null && suffixType2 != null) {
            if (suffixType1 == suffixType2) {
                val suffix1Number = lowercaseSuffix1
                    .replace(suffixType1.second, "")
                    .replace(".", "")
                    .toIntOrNull() ?: 0
                val suffix2Number = lowercaseSuffix2.replace(suffixType2.second, "")
                    .replace(".", "")
                    .toIntOrNull() ?: 0
                suffixCompareResult = suffix1Number.compareTo(suffix2Number)
            } else {
                suffixCompareResult = suffixType1.first.compareTo(suffixType2.first)
            }
        } else if (suffixType1 == null) {
            suffixCompareResult = 1
        } else {
            // suffixType2 == null
            suffixCompareResult = -1
        }
    }
    return suffixCompareResult
}

internal fun Float.aboutEquals(other: Float, delta: Float, scale: Int): Boolean {
    return abs(this - other).format(scale) <= delta
}

internal fun Float.filterNegativeZeros(): Float {
    return if (this == -0f) 0f else this
}

internal fun OffsetCompat.filterNegativeZeros(): OffsetCompat {
    if (this.x == -0f || this.y == -0f) {
        return OffsetCompat(this.x.filterNegativeZeros(), this.y.filterNegativeZeros())
    }
    return this
}

/**
 * Check if the current thread is the UI thread
 *
 * @see com.github.panpf.zoomimage.core.android.test.util.CoreUtilsAndroidTest.testRequiredMainThread
 * @see com.github.panpf.zoomimage.core.desktop.test.util.CoreUtilsDesktopTest.testRequiredMainThread
 * @see com.github.panpf.zoomimage.core.jscommon.test.util.CoreUtilsJsCommonTest.testRequiredMainThread
 * @see com.github.panpf.zoomimage.core.ios.test.util.CoreUtilsIosTest.testRequiredMainThread
 */
internal expect fun requiredMainThread()

/**
 * Check if the current thread is the work thread
 *
 * @see com.github.panpf.zoomimage.core.android.test.util.CoreUtilsAndroidTest.testRequiredWorkThread
 * @see com.github.panpf.zoomimage.core.desktop.test.util.CoreUtilsDesktopTest.testRequiredWorkThread
 * @see com.github.panpf.zoomimage.core.jscommon.test.util.CoreUtilsJsCommonTest.testRequiredWorkThread
 * @see com.github.panpf.zoomimage.core.ios.test.util.CoreUtilsIosTest.testRequiredWorkThread
 */
internal expect fun requiredWorkThread()

/**
 * If one of the size is a thumbnail of the other size, it returns true.
 * The rule is that their scaling ratio is the same and the error after scaling does not exceed [epsilonPixels] pixels
 *
 * @param epsilonPixels The maximum allowable error pixels, default is 1.0f
 */
internal fun isThumbnailWithSize(
    size: IntSizeCompat,
    otherSize: IntSizeCompat,
    epsilonPixels: Float = 1.0f
): Boolean {
    // There is no need to compare pictures with width or height of 0
    if (size.isEmpty() || otherSize.isEmpty()) return false
    // There is no need to compare the image direction (horizontal or vertical)
    if (size.width > size.height && otherSize.width < otherSize.height) return false
    if (size.width < size.height && otherSize.width > otherSize.height) return false

    val (originSize, thumbnailSize) = when {
        size.width > otherSize.width && size.height > otherSize.height -> size to otherSize
        size.width < otherSize.width && size.height < otherSize.height -> otherSize to size
        else -> return false
    }

    // Verify height with width scaling
    val widthScale = originSize.width.toFloat() / thumbnailSize.width
    val targetHeight = originSize.height.toFloat() / widthScale
    val heightDiff = abs(targetHeight - thumbnailSize.height)
    val validByWidth = widthScale >= 1 - epsilonPixels && heightDiff <= epsilonPixels

    // Verify width with height scaling
    val heightScale = originSize.height.toFloat() / thumbnailSize.height
    val targetWidth = originSize.width.toFloat() / heightScale
    val widthDiff = abs(targetWidth - thumbnailSize.width)
    val validByHeight = heightScale >= 1 - epsilonPixels && widthDiff <= epsilonPixels

    val pass = validByWidth || validByHeight
//    println(
//        "isThumbnailWithSize: " +
//                "originSize=${originSize.toShortString()}, " +
//                "thumbnailSize=${thumbnailSize.toShortString()}. " +
//                "widthScale=${widthScale.format(2)}, " +
//                "targetHeight=${targetHeight.format(2)}, " +
//                "heightDiff=${heightDiff.format(2)}, " +
//                "validByWidth=${validByWidth}. " +
//                "heightScale=${heightScale.format(2)}, " +
//                "targetWidth=${targetWidth.format(2)}, " +
//                "widthDiff=${widthDiff.format(2)}, " +
//                "validByHeight=${validByHeight}. " +
//                "pass=$pass"
//    )
    return pass
}