package com.ponykamni.arcanoid

import android.app.Activity
import android.graphics.Point
import android.os.Bundle
import android.view.Display
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

class BallViewModel() {

    val coordinates = MutableStateFlow<BallState>(
        BallState(
            Vector(0f, 0f),
            0,
        )
    )

    suspend fun updateState(ballSize: Int, x: Float, y: Float) {
        coordinates.emit(
            BallState(
                Vector(x, y),
                ballSize,
            )
        )
    }
}

data class BallState(
    val position: Vector,
    val size: Int,
)

data class Vector(
    val x: Float,
    val y: Float,
)

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val (width, height) = getScreenSize(this)
        val game = Game(width, height)
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
                            game.ballViewModel
                        )
                        PlatformContainer(Modifier.align(Alignment.BottomStart))
                    }
                }
            }
        }

        CoroutineScope(Dispatchers.IO).launch {

            while (true) {
                game.updateState()
                delay(10)
            }
        }
    }
}

class Game(
    private val screenWidth: Int,
    private val screenHeight: Int,
) {

    private var ball: Ball = Ball(0f, 0f)

    val ballViewModel: BallViewModel = BallViewModel()

    suspend fun updateState() {
        updateBallPosition()
        updateBallDirection()

        ballViewModel.updateState(ball.size, ball.position.x, ball.position.y)
    }

    private fun updateBallDirection() {
        if (ball.position.x + ball.size > screenWidth || ball.position.x < 0) {
            val currentDirection = ball.direction
            val newDirection = Vector(
                -currentDirection.x,
                currentDirection.y
            )
            ball.direction = newDirection
        }
        if (ball.position.y + ball.size > screenHeight || ball.position.y < 0) {
            val currentDirection = ball.direction
            val newDirection = Vector(
                currentDirection.x,
                -currentDirection.y
            )
            ball.direction = newDirection
        }
    }

    private fun updateBallPosition() {
        val currentPosition = ball.position
        val direction = ball.direction

        val newPositionX = (currentPosition.x + direction.x)
        val newPositionY = (currentPosition.y + direction.y)

        ball.position = Vector(newPositionX, newPositionY)
    }
}

class Ball(
    posX: Float,
    posY: Float,
) {

    var size = 10
    var position = Vector(posX, posY)

    var direction = Vector(1f, 1f)
    var velocity: Float = 1f
}

fun getScreenSize(activity: Activity): Pair<Int, Int> {
    val display: Display = activity.windowManager.defaultDisplay
    val size = Point()
    display.getSize(size)
    val width: Int = size.x
    val height: Int = size.y

    return width to height
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
fun BallScreen(modifier: Modifier, ballViewModel: BallViewModel) {
    Box(modifier = modifier) {
        Ball(ballViewModel)
    }
}

@Composable
fun Ball(ballViewModel: BallViewModel) {
    val state = ballViewModel.coordinates.collectAsState().value
    val posX = state.position.x
    val posY = state.position.y
    val ballSize = state.size

    Box(
        Modifier
            .offset { IntOffset(posX.toInt(), posY.toInt()) }
    ) {
        Box(
            modifier = Modifier
                .size(ballSize.dp)
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