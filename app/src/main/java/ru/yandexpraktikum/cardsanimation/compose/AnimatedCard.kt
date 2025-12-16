package ru.yandexpraktikum.cardsanimation.compose

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import ru.yandexpraktikum.cardsanimation.model.CardData
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AnimatedCard(
    cardIndex: Int,
    cardData: CardData,
    targetRotation: Float,
    animationState: Animation,
    onAnimationStepComplete: (Int) -> Unit,
) {
    val density = LocalDensity.current
    val isLeadCard = cardIndex == 0
    val isRunning = animationState.isAnimating
    val phase = animationState.animationStep

    val tilt by animateFloatAsState(
        targetValue = targetRotation,
        animationSpec = tween(300),
        label = "tilt"
    )

    val travel = with(density) { 50.dp.toPx() }
    val radians = Math.toRadians(targetRotation.toDouble())
    val dx = (travel * cos(radians)).toFloat()
    val dy = (travel * sin(radians)).toFloat()

    val (targetX, targetY) = when {
        isRunning && phase == 1 && isLeadCard -> dx to dy
        isRunning && phase == 2 && isLeadCard -> 0f to 0f
        else                                  -> 0f to 0f
    }

    val shiftX by animateFloatAsState(
        targetValue = targetX,
        animationSpec = tween(300),
        finishedListener = {
            if (!isLeadCard || !isRunning) return@animateFloatAsState
            when (phase) {
                1 -> onAnimationStepComplete(2)
                2 -> onAnimationStepComplete(3)
            }
        },
        label = "shiftX"
    )

    val shiftY by animateFloatAsState(
        targetValue = targetY,
        animationSpec = tween(300),
        label = "shiftY"
    )

    val elevate = isRunning && phase >= 2 && isLeadCard

    Card(
        modifier = Modifier
            .size(100.dp, 160.dp)
            .graphicsLayer {
                translationX = if (isRunning) shiftX else 0f
                translationY = if (isRunning) shiftY else 0f
                rotationZ = tilt
                transformOrigin = TransformOrigin(0.5f, 1f)
            }
            .let { if (elevate) it.zIndex(1000f) else it },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = (4 + cardIndex).dp)
    ) {
        Image(
            painter = painterResource(cardData.imageResId),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}