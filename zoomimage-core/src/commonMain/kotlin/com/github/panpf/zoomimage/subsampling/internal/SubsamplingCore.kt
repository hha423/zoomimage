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

package com.github.panpf.zoomimage.subsampling.internal

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Lifecycle.State.STARTED
import androidx.lifecycle.LifecycleEventObserver
import com.github.panpf.zoomimage.subsampling.ImageInfo
import com.github.panpf.zoomimage.subsampling.ImageSource
import com.github.panpf.zoomimage.subsampling.RegionDecoder
import com.github.panpf.zoomimage.subsampling.SubsamplingImage
import com.github.panpf.zoomimage.subsampling.TileAnimationSpec
import com.github.panpf.zoomimage.subsampling.TileImageCache
import com.github.panpf.zoomimage.subsampling.TileSnapshot
import com.github.panpf.zoomimage.subsampling.internal.TileManager.Companion.DefaultPausedContinuousTransformTypes
import com.github.panpf.zoomimage.util.IntOffsetCompat
import com.github.panpf.zoomimage.util.IntRectCompat
import com.github.panpf.zoomimage.util.IntSizeCompat
import com.github.panpf.zoomimage.util.Logger
import com.github.panpf.zoomimage.util.closeQuietly
import com.github.panpf.zoomimage.util.ioCoroutineDispatcher
import com.github.panpf.zoomimage.util.isEmpty
import com.github.panpf.zoomimage.util.requiredMainThread
import com.github.panpf.zoomimage.util.round
import com.github.panpf.zoomimage.util.toShortString
import com.github.panpf.zoomimage.zoom.ContinuousTransformType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException
import kotlin.math.roundToInt

/**
 * Core that control subsampling
 *
 * @see com.github.panpf.zoomimage.core.common.test.subsampling.internal.SubsamplingCoreTest
 * @see com.github.panpf.zoomimage.compose.common.test.subsampling.SubsamplingStateTest
 * @see com.github.panpf.zoomimage.view.test.subsampling.SubsamplingEngineTest
 */
@Suppress("RedundantConstructorKeyword")
class SubsamplingCore constructor(
    val module: String,
    val logger: Logger,
    val tileImageConvertor: TileImageConvertor?,
    val zoomableBridge: ZoomableBridge,
    val onReadyChanged: (SubsamplingCore) -> Unit,
    val onTileChanged: (SubsamplingCore) -> Unit,
) {

    private var coroutineScope: CoroutineScope? = null
    private var tileManager: TileManager? = null
    private var tileDecoder: TileDecoder? = null
    private var tileImageCacheHelper = TileImageCacheHelper()
    private var resetTileDecoderJob: Job? = null
    private val refreshTilesFlow = MutableSharedFlow<String>()
    private var preferredTileSize = IntSizeCompat.Zero
    private var contentSize = IntSizeCompat.Zero
    private var cachedImage: SubsamplingImage? = null
    private val logKey: String?
        get() = subsamplingImage?.key ?: "null"
    private val stoppedLifecycleObserver = LifecycleEventObserver { owner, _ ->
        val disabledAutoStopWithLifecycle = disabledAutoStopWithLifecycle
        logger.d {
            "$module. lifecycle. ${owner.lifecycle.currentState}. " +
                    "disabledAutoStopWithLifecycle=$disabledAutoStopWithLifecycle. " +
                    "'$logKey'"
        }
        if (!disabledAutoStopWithLifecycle) {
            refreshStoppedState()
        }
    }

    var subsamplingImage: SubsamplingImage? = null
        private set
    var disabled: Boolean = false
        private set
    val tileImageCache: TileImageCache?
        get() = tileImageCacheHelper.tileImageCache
    val disabledTileImageCache: Boolean
        get() = tileImageCacheHelper.disabled
    var pausedContinuousTransformTypes: Int = DefaultPausedContinuousTransformTypes
        private set
    var tileAnimationSpec: TileAnimationSpec = TileAnimationSpec.Default
        private set
    var disabledBackgroundTiles: Boolean = false
        private set
    var stopped: Boolean = false
        private set
    var lifecycle: Lifecycle? = null
        private set
    var disabledAutoStopWithLifecycle: Boolean = false
        private set
    var regionDecoders: List<RegionDecoder.Factory> = emptyList()
        private set

    var imageInfo: ImageInfo? = null
        private set
    var ready: Boolean = false
        private set
    var foregroundTiles: List<TileSnapshot> = emptyList()
        private set
    var backgroundTiles: List<TileSnapshot> = emptyList()
        private set
    var sampleSize: Int = 0
        private set
    var imageLoadRect: IntRectCompat = IntRectCompat.Zero
        private set
    var tileGridSizeMap: Map<Int, IntOffsetCompat> = emptyMap()
        private set


    fun setImage(subsamplingImage: SubsamplingImage?): Boolean {
        requiredMainThread()
        if (disabled) {
            logger.d { "$module. setImage. disabled. '${subsamplingImage}'" }
            cachedImage = subsamplingImage
            return false
        }

        if (this.subsamplingImage == subsamplingImage) return false
        logger.d { "$module. setImage. '${this.subsamplingImage}' -> '${subsamplingImage}'" }
        clean("setImage")
        this.subsamplingImage = subsamplingImage
        if (coroutineScope != null && subsamplingImage != null) {
            resetTileDecoder("setImage")
        }
        return true
    }

    fun setImage(imageSource: ImageSource.Factory?, imageInfo: ImageInfo? = null): Boolean {
        return setImage(imageSource?.let { SubsamplingImage(it, imageInfo) })
    }

    fun setImage(imageSource: ImageSource?, imageInfo: ImageInfo? = null): Boolean {
        return setImage(imageSource?.let {
            SubsamplingImage(ImageSource.WrapperFactory(it), imageInfo)
        })
    }

    fun setDisabled(disabled: Boolean) {
        requiredMainThread()
        if (this.disabled != disabled) {
            logger.d { "$module. disabled=$disabled. '$logKey'" }
            if (disabled) {
                cachedImage = subsamplingImage
                setImage(null as SubsamplingImage?)
                this.disabled = disabled
            } else {
                this.disabled = disabled
                setImage(cachedImage)
                cachedImage = null
            }
        }
    }

    fun setTileImageCache(tileImageCache: TileImageCache?) {
        requiredMainThread()
        if (tileImageCacheHelper.tileImageCache != tileImageCache) {
            logger.d { "$module. tileImageCache=$tileImageCache. '$logKey'" }
            tileImageCacheHelper.tileImageCache = tileImageCache
        }
    }

    fun setDisabledTileImageCache(disabled: Boolean) {
        requiredMainThread()
        if (tileImageCacheHelper.disabled != disabled) {
            logger.d { "$module. disabledTileImageCache=$disabled. '$logKey'" }
            tileImageCacheHelper.disabled = disabled
        }
    }

    fun setTileAnimationSpec(tileAnimationSpec: TileAnimationSpec) {
        requiredMainThread()
        if (this.tileAnimationSpec != tileAnimationSpec) {
            logger.d { "$module. tileAnimationSpec=$tileAnimationSpec. '$logKey'" }
            this.tileAnimationSpec = tileAnimationSpec
            tileManager?.tileAnimationSpec = tileAnimationSpec
        }
    }

    fun setPausedContinuousTransformTypes(@ContinuousTransformType pausedContinuousTransformTypes: Int) {
        requiredMainThread()
        if (this.pausedContinuousTransformTypes != pausedContinuousTransformTypes) {
            logger.d {
                val namesString = ContinuousTransformType.names(pausedContinuousTransformTypes)
                    .joinToString(prefix = "[", postfix = "]")
                "$module. pausedContinuousTransformTypes=$namesString. '$logKey'"
            }
            this.pausedContinuousTransformTypes = pausedContinuousTransformTypes
            tileManager?.pausedContinuousTransformTypes = pausedContinuousTransformTypes
        }
    }

    fun setDisabledBackgroundTiles(disabledBackgroundTiles: Boolean) {
        requiredMainThread()
        if (this.disabledBackgroundTiles != disabledBackgroundTiles) {
            logger.d { "$module. disabledBackgroundTiles=$disabledBackgroundTiles. '$logKey'" }
            this.disabledBackgroundTiles = disabledBackgroundTiles
            tileManager?.disabledBackgroundTiles = disabledBackgroundTiles
        }
    }

    fun setStopped(stopped: Boolean) {
        requiredMainThread()
        if (this.stopped != stopped) {
            logger.d { "$module. stopped=$stopped. '$logKey'" }
            this.stopped = stopped
            val stoppedState = if (stopped) "stopped" else "started"
            if (stopped) {
                tileManager?.clean(stoppedState)
            }
            refreshReadyState(stoppedState)
        }
    }

    fun setLifecycle(lifecycle: Lifecycle?) {
        requiredMainThread()
        if (this.lifecycle != lifecycle) {
            this.lifecycle?.removeObserver(stoppedLifecycleObserver)
            this.lifecycle = lifecycle
            if (coroutineScope != null) {
                lifecycle?.addObserver(stoppedLifecycleObserver)
            }
        }
    }

    fun setDisabledAutoStopWithLifecycle(disabled: Boolean) {
        requiredMainThread()
        if (this.disabledAutoStopWithLifecycle != disabled) {
            logger.d { "$module. disabledAutoStopWithLifecycle=$disabled. '$logKey'" }
            this.disabledAutoStopWithLifecycle = disabled
            if (disabled) {
                setStopped(false)
            } else {
                refreshStoppedState()
            }
        }
    }

    fun setRegionDecoders(regionDecoders: List<RegionDecoder.Factory>) {
        requiredMainThread()
        if (this.regionDecoders != regionDecoders) {
            this.regionDecoders = regionDecoders
            logger.d {
                val regionDecodersString = regionDecoders.joinToString(prefix = "[", postfix = "]")
                "$module. regionDecoders=$regionDecodersString. '$logKey'"
            }
            resetTileDecoder("regionDecodersChanged")
        }
    }


    /* *************************************** Internal ***************************************** */

    fun onAttached() {
        requiredMainThread()
        if (this.coroutineScope != null) return
        val coroutineScope = CoroutineScope(Dispatchers.Main)
        this.coroutineScope = coroutineScope

        coroutineScope.launch {
            // Changes in containerSize cause a large chain reaction that can cause large memory fluctuations.
            // Container size animations cause frequent changes in v, so a delayed reset avoids this problem
            @Suppress("OPT_IN_USAGE")
            zoomableBridge.containerSizeFlow.debounce(80).collect {
                updatePreferredTileSize(it)
            }
        }
        coroutineScope.launch {
            zoomableBridge.contentSizeFlow.collect {
                updateContentSize(it)
            }
        }
        coroutineScope.launch {
            zoomableBridge.transformFlow.collect {
                refreshTiles(caller = "transformChanged")
            }
        }
        coroutineScope.launch {
            zoomableBridge.continuousTransformTypeFlow.collect {
                refreshTiles(caller = "continuousTransformTypeChanged")
            }
        }
        coroutineScope.launch {
            refreshTilesFlow.collect {
                refreshTiles(caller = it)
            }
        }
        lifecycle?.addObserver(stoppedLifecycleObserver)
        if (subsamplingImage != null) {
            resetTileDecoder("setImage")
        }
    }

    fun onDetached() {
        requiredMainThread()
        val coroutineScope = this.coroutineScope ?: return
        lifecycle?.removeObserver(stoppedLifecycleObserver)
        clean("setCoroutineScope")
        coroutineScope.cancel()
        this.coroutineScope = null
    }

    private fun updatePreferredTileSize(containerSize: IntSizeCompat) {
        val oldPreferredTileSize = preferredTileSize
        val newPreferredTileSize = calculatePreferredTileSize(containerSize)
        val checkPassed = checkNewPreferredTileSize(
            oldPreferredTileSize = oldPreferredTileSize,
            newPreferredTileSize = newPreferredTileSize
        )
        logger.d {
            "$module. preferredTileSize. " +
                    "${if (checkPassed) "changed" else "keep"}. " +
                    "${oldPreferredTileSize.toShortString()} -> ${newPreferredTileSize.toShortString()}, " +
                    "containerSize=${containerSize.toShortString()}. " +
                    "'$logKey'"
        }
        if (checkPassed) {
            this.preferredTileSize = newPreferredTileSize
            resetTileManager("preferredTileSizeChanged")
        }
    }

    private fun updateContentSize(contentSize: IntSizeCompat) {
        if (this.contentSize != contentSize) {
            this.contentSize = contentSize
            logger.d { "$module. contentSize=$contentSize. '$logKey'" }
            resetTileDecoder("contentSizeChanged")
        }
    }

    private fun resetTileDecoder(caller: String) {
        cleanTileManager(caller)
        cleanTileDecoder(caller)

        val subsamplingImage = subsamplingImage
        val contentSize = contentSize
        val coroutineScope = coroutineScope
        if (subsamplingImage == null || contentSize.isEmpty() || coroutineScope == null) {
            logger.d {
                "$module. resetTileDecoder:$caller. skipped. " +
                        "parameters are not ready yet. " +
                        "subsamplingImage=${subsamplingImage}, " +
                        "contentSize=${contentSize.toShortString()}, " +
                        "coroutineScope=$coroutineScope"
            }
            return
        }

        resetTileDecoderJob = coroutineScope.launch {
            val tileDecoderResult = createTileDecoder(
                logger = logger,
                subsamplingImage = subsamplingImage,
                contentSize = contentSize,
                regionDecoders = regionDecoders,
            )
            if (tileDecoderResult.isFailure
                && tileDecoderResult.exceptionOrNull() !is CancellationException
                && tileDecoderResult.exceptionOrNull() !is kotlinx.coroutines.CancellationException
            ) {
                logger.d {
                    "$module. resetTileDecoder:$caller. failed. " +
                            "${tileDecoderResult.exceptionOrNull()}. " +
                            "'${subsamplingImage.key}'"
                }
                return@launch
            }

            val tileDecoder = tileDecoderResult.getOrThrow()
            val imageInfo = subsamplingImage.imageInfo ?: tileDecoder.imageInfo
            this@SubsamplingCore.imageInfo = imageInfo
            this@SubsamplingCore.tileDecoder = tileDecoder
            this@SubsamplingCore.zoomableBridge.setContentOriginSize(imageInfo.size)
            logger.d {
                "$module. resetTileDecoder:$caller. success. " +
                        "contentSize=${contentSize.toShortString()}, " +
                        "imageInfo=${imageInfo.toShortString()}. " +
                        "'${subsamplingImage.key}'"
            }
            refreshReadyState(caller)
            resetTileManager(caller)
        }
    }

    private fun resetTileManager(caller: String) {
        cleanTileManager(caller)

        val subsamplingImage = subsamplingImage
        val tileDecoder = tileDecoder
        val imageInfo = imageInfo
        val preferredTileSize = preferredTileSize
        val contentSize = contentSize
        if (subsamplingImage == null || tileDecoder == null || imageInfo == null || preferredTileSize.isEmpty() || contentSize.isEmpty()) {
            logger.d {
                "$module. resetTileManager:$caller. skipped. " +
                        "parameters are not ready yet. " +
                        "subsamplingImage=${subsamplingImage}, " +
                        "contentSize=${contentSize.toShortString()}, " +
                        "preferredTileSize=${preferredTileSize.toShortString()}, " +
                        "tileDecoder=${tileDecoder}, " +
                        "'$logKey'"
            }
            return
        }

        val tileManager = TileManager(
            logger = logger,
            subsamplingImage = subsamplingImage,
            tileDecoder = tileDecoder,
            tileImageConvertor = tileImageConvertor,
            preferredTileSize = preferredTileSize,
            contentSize = contentSize,
            tileImageCacheHelper = tileImageCacheHelper,
            imageInfo = imageInfo,
            onTileChanged = { manager ->
                if (this@SubsamplingCore.tileManager == manager) {
                    this@SubsamplingCore.backgroundTiles = manager.backgroundTiles
                    this@SubsamplingCore.foregroundTiles = manager.foregroundTiles
                    onTileChanged(this@SubsamplingCore)
                }
            },
            onSampleSizeChanged = { manager ->
                if (this@SubsamplingCore.tileManager == manager) {
                    this@SubsamplingCore.sampleSize = manager.sampleSize
                    onTileChanged(this@SubsamplingCore)
                }
            },
            onImageLoadRectChanged = { manager ->
                if (this@SubsamplingCore.tileManager == manager) {
                    this@SubsamplingCore.imageLoadRect = manager.imageLoadRect
                    onTileChanged(this@SubsamplingCore)
                }
            }
        )
        tileManager.pausedContinuousTransformTypes =
            this@SubsamplingCore.pausedContinuousTransformTypes
        tileManager.disabledBackgroundTiles =
            this@SubsamplingCore.disabledBackgroundTiles
        tileManager.tileAnimationSpec = this@SubsamplingCore.tileAnimationSpec

        this@SubsamplingCore.tileGridSizeMap = tileManager.sortedTileGridMap.associate { entry ->
            entry.sampleSize to entry.tiles.last().coordinate
                .let { IntOffsetCompat(it.x + 1, it.y + 1) }
        }
        logger.d {
            "$module. resetTileManager:$caller. success. " +
                    "imageInfo=${imageInfo.toShortString()}. " +
                    "preferredTileSize=${preferredTileSize.toShortString()}, " +
                    "tileGridMap=${tileManager.sortedTileGridMap.toIntroString()}. " +
                    "'${subsamplingImage.key}'"
        }
        this@SubsamplingCore.tileManager = tileManager
        refreshReadyState(caller)
    }

    private fun refreshTiles(
        contentVisibleRect: IntRectCompat = zoomableBridge.contentVisibleRect.round(),
        scale: Float = zoomableBridge.transform.scaleX,
        rotation: Int = zoomableBridge.transform.rotation.roundToInt(),
        @ContinuousTransformType continuousTransformType: Int = zoomableBridge.continuousTransformType,
        caller: String,
    ) {
        val tileManager = tileManager ?: return
        if (stopped) {
            logger.d { "$module. refreshTiles:$caller. interrupted, stopped. '$logKey'" }
            return
        }
        tileManager.refreshTiles(
            scale = scale,
            contentVisibleRect = contentVisibleRect,
            rotation = rotation,
            continuousTransformType = continuousTransformType,
            caller = caller
        )
    }

    private fun refreshReadyState(caller: String) {
        val imageInfoReady = imageInfo != null
        val tileManagerReady = tileManager != null
        val tileDecoderReady = tileDecoder != null
        val stoppedReady = !stopped
        val newReady = imageInfoReady && tileManagerReady && tileDecoderReady && stoppedReady
        logger.d {
            "$module. refreshReadyState:$caller. " +
                    "ready=$newReady, " +
                    "imageInfoReady=$imageInfoReady, " +
                    "tileManagerReady=$tileManagerReady, " +
                    "tileDecoderReady=$tileDecoderReady, " +
                    "stoppedReady=$stoppedReady. " +
                    "'$logKey'"
        }
        // Duplicate callbacks cannot be intercepted by validating if('this@SubsamplingCore.ready != newReady)',
        // because SubsamplingState and SubsamplingEngine need to rely on this callback to update properties such as stopped, imageInfo, tileGridSizeMap, etc
        this@SubsamplingCore.ready = newReady
        onReadyChanged(this@SubsamplingCore)
        coroutineScope?.launch {
            refreshTilesFlow.emit("refreshReadyState:$caller")
        }
    }

    private fun refreshStoppedState() {
        val lifecycle = lifecycle
        if (lifecycle != null) {
            val stopped = !lifecycle.currentState.isAtLeast(STARTED)
            this@SubsamplingCore.setStopped(stopped)
        }
    }

    private fun cleanTileDecoder(caller: String) {
        val resetTileDecoderJob1 = this@SubsamplingCore.resetTileDecoderJob
        if (resetTileDecoderJob1 != null && resetTileDecoderJob1.isActive) {
            resetTileDecoderJob1.cancel("cleanTileDecoder:$caller")
            this@SubsamplingCore.resetTileDecoderJob = null
        }

        val tileDecoder = this@SubsamplingCore.tileDecoder
        val imageInfo = this@SubsamplingCore.imageInfo
        if (tileDecoder != null) {
            logger.d { "$module. cleanTileDecoder:$caller. '$logKey'" }
            @Suppress("OPTthis@SubsamplingCore.IN_USAGE", "OPT_IN_USAGE")
            GlobalScope.launch(ioCoroutineDispatcher()) {
                tileDecoder.closeQuietly()
            }
            this@SubsamplingCore.tileDecoder = null
        }
        if (imageInfo != null) {
            this@SubsamplingCore.imageInfo = null
        }
        if (tileDecoder != null || imageInfo != null) {
            refreshReadyState(caller)
        }

        zoomableBridge.setContentOriginSize(IntSizeCompat.Zero)
    }

    private fun cleanTileManager(caller: String) {
        val tileManager = this@SubsamplingCore.tileManager
        if (tileManager != null) {
            logger.d { "$module. cleanTileManager:$caller. '$logKey'" }
            tileManager.clean(caller)
            this@SubsamplingCore.tileManager = null
            this@SubsamplingCore.tileGridSizeMap = emptyMap()
            this@SubsamplingCore.foregroundTiles = emptyList()
            this@SubsamplingCore.backgroundTiles = emptyList()
            this@SubsamplingCore.sampleSize = 0
            this@SubsamplingCore.imageLoadRect = IntRectCompat.Zero
            refreshReadyState(caller)
            onTileChanged(this@SubsamplingCore)
        }
    }

    private fun clean(caller: String) {
        cleanTileDecoder(caller)
        cleanTileManager(caller)
    }
}