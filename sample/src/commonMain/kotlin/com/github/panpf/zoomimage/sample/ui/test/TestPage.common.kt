package com.github.panpf.zoomimage.sample.ui.test

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import com.github.panpf.zoomimage.sample.resources.Res
import com.github.panpf.zoomimage.sample.resources.ic_github
import com.github.panpf.zoomimage.sample.ui.components.AutoLinkText
import com.github.panpf.zoomimage.sample.util.Platform
import com.github.panpf.zoomimage.sample.util.current
import com.github.panpf.zoomimage.sample.util.isMobile
import org.jetbrains.compose.resources.painterResource

fun testItems(): List<TestItem> = listOf(
    TestItem("ImageSource", ImageSourceTestScreen()),
    TestItem("Exif Orientation", ExifOrientationTestScreen()),
    TestItem("Graphics Layer", GraphicsLayerTestScreen()),
    TestItem("Modifier.zoom()", ModifierZoomTestScreen()),
    TestItem("Mouse", MouseTestScreen()),
    TestItem("KeyZoom", KeyTestScreen()),
    TestItem("ZoomImage (Switch)", ZoomImageSwitchTestScreen()),
    TestItem("CoilBigStartCrossfade", CoilBigStartCrossfadeTestScreen()),
    TestItem("Overlay", OverlayTestScreen()),
    TestItem("Temp", TempTestScreen()),
).plus(platformTestItems())

expect fun platformTestItems(): List<TestItem>

@Composable
fun TestPage() {
    val testItems = remember { testItems() }
    val gridState = rememberLazyGridState()
    Column(modifier = Modifier.fillMaxSize()) {
        LazyVerticalGrid(
            modifier = Modifier.fillMaxWidth().weight(1f),
            columns = GridCells.Fixed(if (Platform.current.isMobile()) 2 else 4),
            state = gridState,
            contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 96.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(
                count = testItems.size,
                key = { testItems[it].title },
                contentType = { 1 },
            ) { index ->
                TestGridItem(testItems[index])
            }
        }

        ProjectInfoItem()
    }
}

data class TestItem(val title: String, val screen: Screen)

@Composable
fun TestGridItem(item: TestItem) {
    val navigator = LocalNavigator.current!!
    val colorScheme = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .widthIn(100.dp, 1000.dp)
            .heightIn(100.dp, 1000.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(colorScheme.primaryContainer)
            .clickable { navigator.push(item.screen) }
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = item.title,
            color = colorScheme.onPrimaryContainer,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
fun ProjectInfoItem() {
    val colorScheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp, horizontal = 16.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(colorScheme.tertiaryContainer)
            .padding(16.dp),
    ) {
        Icon(
            painter = painterResource(Res.drawable.ic_github),
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.size(10.dp))
        AutoLinkText(text = "https://github.com/panpf/zoomimage")
    }
}