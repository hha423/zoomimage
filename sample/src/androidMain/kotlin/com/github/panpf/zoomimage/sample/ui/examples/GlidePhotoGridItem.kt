package com.github.panpf.zoomimage.sample.ui.examples

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import com.bumptech.glide.integration.compose.CrossFade
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.github.panpf.zoomimage.sample.R
import com.github.panpf.zoomimage.sample.image.sketchUri2GlideModel
import com.github.panpf.zoomimage.sample.ui.model.Photo

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun GlidePhotoGridItem(
    index: Int,
    photo: Photo,
    modifier: Modifier,
    onClick: (photo: Photo, index: Int) -> Unit,
) {
    val sketchImageUri = photo.listThumbnailUrl
    val context = LocalContext.current
    val glideModel = remember(sketchImageUri) {
        sketchUri2GlideModel(context, sketchImageUri)
    }
    GlideImage(
        model = glideModel,
        modifier = modifier
            .pointerInput(photo, index) {
                detectTapGestures(
                    onTap = { onClick(photo, index) },
                )
            },
        contentScale = ContentScale.Crop,
        contentDescription = "photo",
        transition = CrossFade
    ) {
        it.placeholder(R.drawable.im_placeholder)
            .error(R.drawable.im_error)
    }
}