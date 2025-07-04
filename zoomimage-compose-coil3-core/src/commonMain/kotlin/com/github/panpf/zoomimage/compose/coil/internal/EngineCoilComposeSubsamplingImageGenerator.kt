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

package com.github.panpf.zoomimage.compose.coil.internal

import androidx.compose.ui.graphics.painter.Painter
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.request.SuccessResult
import com.github.panpf.zoomimage.coil.internal.dataToImageSource
import com.github.panpf.zoomimage.compose.coil.CoilComposeSubsamplingImageGenerator
import com.github.panpf.zoomimage.subsampling.SubsamplingImage
import com.github.panpf.zoomimage.subsampling.SubsamplingImageGenerateResult

/**
 * [CoilComposeSubsamplingImageGenerator] implementation that uses the Coil library to generate images
 *
 * @see com.github.panpf.zoomimage.compose.coil3.core.test.internal.EngineCoilComposeSubsamplingImageGeneratorTest
 */
class EngineCoilComposeSubsamplingImageGenerator : CoilComposeSubsamplingImageGenerator {

    override suspend fun generateImage(
        context: PlatformContext,
        imageLoader: ImageLoader,
        result: SuccessResult,
        painter: Painter
    ): SubsamplingImageGenerateResult {
        val imageSource = dataToImageSource(context, imageLoader, result.request)
            ?: return SubsamplingImageGenerateResult.Error("Unsupported data")
        return SubsamplingImageGenerateResult.Success(SubsamplingImage(imageSource, null))
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        return other != null && this::class == other::class
    }

    override fun hashCode(): Int {
        return this::class.hashCode()
    }

    override fun toString(): String {
        return "EngineCoilComposeSubsamplingImageGenerator"
    }
}