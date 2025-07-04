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
import coil3.request.ImageRequest
import com.github.panpf.zoomimage.subsampling.ImageSource

/**
 * @see com.github.panpf.zoomimage.core.coil3.android.test.internal.CoilCoreUtilsAndroidTest.testDataToImageSource
 * @see com.github.panpf.zoomimage.core.coil3.desktop.test.internal.CoilCoreUtilsDesktopTest.testDataToImageSource
 * @see com.github.panpf.zoomimage.core.coil3.ios.test.internal.CoilCoreUtilsIosTest.testDataToImageSource
 * @see com.github.panpf.zoomimage.core.coil3.jscommon.test.internal.CoilCoreUtilsJsCommonTest.testDataToImageSource
 */
expect suspend fun dataToImageSource(
    context: PlatformContext,
    imageLoader: ImageLoader,
    request: ImageRequest,
): ImageSource.Factory?

/**
 * @see com.github.panpf.zoomimage.core.coil3.android.test.internal.CoilCoreUtilsAndroidTest.testDataToImageSource
 * @see com.github.panpf.zoomimage.core.coil3.desktop.test.internal.CoilCoreUtilsDesktopTest.testDataToImageSource
 * @see com.github.panpf.zoomimage.core.coil3.ios.test.internal.CoilCoreUtilsIosTest.testDataToImageSource
 * @see com.github.panpf.zoomimage.core.coil3.jscommon.test.internal.CoilCoreUtilsJsCommonTest.testDataToImageSource
 */
@Deprecated("Please use dataToImageSource(context, imageLoader, request) instead")
expect suspend fun dataToImageSource(
    context: PlatformContext,
    imageLoader: ImageLoader,
    model: Any
): ImageSource.Factory?