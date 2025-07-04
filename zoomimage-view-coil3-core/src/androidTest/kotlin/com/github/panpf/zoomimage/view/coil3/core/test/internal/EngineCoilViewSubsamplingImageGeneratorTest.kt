package com.github.panpf.zoomimage.view.coil3.core.test.internal

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.test.platform.app.InstrumentationRegistry
import coil3.ImageLoader
import coil3.asImage
import coil3.decode.DataSource
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import com.github.panpf.zoomimage.coil.CoilHttpImageSource
import com.github.panpf.zoomimage.subsampling.SubsamplingImage
import com.github.panpf.zoomimage.subsampling.SubsamplingImageGenerateResult
import com.github.panpf.zoomimage.view.coil.internal.EngineCoilViewSubsamplingImageGenerator
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class EngineCoilViewSubsamplingImageGeneratorTest {

    @Test
    fun testGenerateImage() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().context
        val imageLoader = ImageLoader(context)
        val generator = EngineCoilViewSubsamplingImageGenerator()

        assertEquals(
            expected = SubsamplingImageGenerateResult.Success(
                SubsamplingImage(
                    imageSource = CoilHttpImageSource.Factory(
                        context,
                        imageLoader,
                        ImageRequest.Builder(context).data("http://sample.com/sample.jpeg").build()
                    ),
                    imageInfo = null
                )
            ),
            actual = generator.generateImage(
                context = context,
                imageLoader = imageLoader,
                result = SuccessResult(
                    image = ColorDrawable(Color.CYAN).asImage(),
                    request = ImageRequest.Builder(context).data("http://sample.com/sample.jpeg")
                        .build(),
                    dataSource = DataSource.DISK,
                ),
                drawable = ColorDrawable(Color.BLUE)
            )
        )

        assertEquals(
            expected = SubsamplingImageGenerateResult.Error("Unsupported data"),
            actual = generator.generateImage(
                context = context,
                imageLoader = imageLoader,
                result = SuccessResult(
                    image = ColorDrawable(Color.CYAN).asImage(),
                    request = ImageRequest.Builder(context)
                        .data("fakehttp://sample.com/sample.jpeg").build(),
                    dataSource = DataSource.DISK,
                ),
                drawable = ColorDrawable(Color.BLUE)
            )
        )
    }

    @Test
    fun testEqualsAndHashCode() {
        val element1 = EngineCoilViewSubsamplingImageGenerator()
        val element11 = EngineCoilViewSubsamplingImageGenerator()

        assertEquals(expected = element1, actual = element11)
        assertNotEquals(illegal = element1, actual = null as Any?)
        assertNotEquals(illegal = element1, actual = Any())

        assertEquals(expected = element1.hashCode(), actual = element11.hashCode())
    }

    @Test
    fun testToString() {
        assertEquals(
            expected = "EngineCoilViewSubsamplingImageGenerator",
            actual = EngineCoilViewSubsamplingImageGenerator().toString()
        )
    }
}