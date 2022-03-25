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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.ponykamni.arcanoid.ui.theme.ArcanoidTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class DummyViewModel() {

    val coordinates = MutableStateFlow<Coordinates>(Coordinates(0f, 0f))

    suspend fun updateCoordinates(x: Float, y: Float) {
        coordinates.emit(Coordinates(x, y))
    }
}

data class Coordinates(
    val x: Float,
    val y: Float,
)

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val dummyViewModel = DummyViewModel()



        setContent {
            ArcanoidTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    Box(
                        modifier = Modifier
                    ) {
                        BallScreen(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Gray),
                            dummyViewModel
                        )
                        PlatformContainer(Modifier.align(Alignment.BottomStart))
                    }
                }
            }
        }

        CoroutineScope(Dispatchers.IO).launch {
            var x = 0f
            var y = 0f
            while (true) {
                dummyViewModel.updateCoordinates(x++, y++)
                delay(10)
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
fun PlatformContainer(modifier: Modifier) {
    var offsetX by remember { mutableStateOf(0f) }
    var size by remember { mutableStateOf(IntSize.Zero) }

    val screenSize = getScreenWidthInPixels()

    Column(modifier) {
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
fun BallScreen(modifier: Modifier, dummyViewModel: DummyViewModel) {
    Box(modifier = modifier) {
        Ball(dummyViewModel)
    }
}

@Composable
fun Ball(dummyViewModel: DummyViewModel) {
    val pos = dummyViewModel.coordinates.collectAsState()
    val posX = pos.value.x
    val posY = pos.value.y

    Box(
        Modifier
            .offset { IntOffset(posX.toInt(), posY.toInt()) }
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(Color.Black)
        )
        Text(text = "$posX, $posY")
    }
}

@Composable
fun Platform(modifier: Modifier) {
    Box(
        modifier = modifier
            .width(150.dp)
            .height(50.dp)
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