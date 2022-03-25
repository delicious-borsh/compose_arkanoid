package com.ponykamni.arcanoid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.ponykamni.arcanoid.ui.theme.ArcanoidTheme
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ArcanoidTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier
                        .fillMaxWidth(),
                    color = MaterialTheme.colors.background
                ) {
                    PlatformContainer()
                }
            }
        }
    }
}

@Composable
fun getScreenWidthInPixels(): Int {
    val configuration = LocalConfiguration.current
    val widthFloat = configuration.screenWidthDp * getScreenDensity()
    return widthFloat.toInt()
}

@Composable
fun getScreenDensity(): Float {
    return LocalConfiguration.current.densityDpi.toFloat() / 160f
}

@Composable
fun PlatformContainer() {
    var offsetX by remember { mutableStateOf(0f) }
    var size by remember { mutableStateOf(IntSize.Zero) }

    val screenSize = getScreenWidthInPixels()

    Column {
        Text(text = "Offset = $offsetX, size = $size")
        Box(modifier = Modifier.wrapContentSize()) {
            Platform(
                Modifier
                    .onGloballyPositioned { coordinates -> size = coordinates.size }
                    .offset { IntOffset(offsetX.roundToInt(), 0) }
                    .draggable(
                        orientation = Orientation.Horizontal,
                        state = rememberDraggableState { delta ->
                            val newOffset = offsetX.plus(delta)

                            if (newOffset > 0 && newOffset + size.width < screenSize) {
                                offsetX = newOffset
                            }
                        }
                    )
            )
        }
    }
}

@Composable
fun Platform(modifier: Modifier) {
    Box(
        modifier = modifier
            .width(150.dp)
            .height(20.dp)
            .background(MaterialTheme.colors.secondary),
    )
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    ArcanoidTheme {
        Platform(Modifier)
    }
}