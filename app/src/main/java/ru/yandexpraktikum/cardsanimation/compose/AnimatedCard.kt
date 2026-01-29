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
    animationState: CardSwapAnimationState,
    onStepChange: (Int) -> Unit,
    onAnimationComplete: () -> Unit,
) {
    val animatedRotation by animateFloatAsState(
        targetValue = when {
            animationState.animationStep == 3 -> 0f
            animationState.isAnimating -> targetRotation
            else -> targetRotation
        },
        animationSpec = tween(durationMillis = if (animationState.animationStep == 3) 300 else 800),
        finishedListener = {
            handleAnimationStepComplete(
                step = animationState.animationStep,
                cardIndex = cardIndex,
                onStepChange = onStepChange,
                onAnimationComplete = onAnimationComplete
            )
        },
        label = "rotation"
    )

    val density = LocalDensity.current
    val shouldBringToFront =
        animationState.isAnimating && animationState.animationStep >= 2 && cardIndex == 0

    val animatedTranslationX by animateFloatAsState(
        targetValue = when {
            animationState.isAnimating && animationState.animationStep == 1 -> {
                val moveDistance = with(density) { 50.dp.toPx() }
                val rotationRad = Math.toRadians(targetRotation.toDouble())
                moveDistance * cos(rotationRad).toFloat()
            }

            animationState.isAnimating && animationState.animationStep == 2 -> 0f
            else -> 0f
        },
        animationSpec = tween(durationMillis = 300),
        finishedListener = {
            handleAnimationStepComplete(
                step = animationState.animationStep,
                cardIndex = cardIndex,
                onStepChange = onStepChange,
                onAnimationComplete = onAnimationComplete
            )
        },
        label = "translationX"
    )

    val animatedTranslationY by animateFloatAsState(
        targetValue = when {
            animationState.isAnimating && animationState.animationStep == 1 -> {
                val moveDistance = with(density) { 50.dp.toPx() }
                val rotationRad = Math.toRadians(targetRotation.toDouble())
                moveDistance * sin(rotationRad).toFloat()
            }

            animationState.isAnimating && animationState.animationStep == 2 -> 0f
            else -> 0f
        },
        animationSpec = tween(durationMillis = 300),
        label = "translationY"
    )

    Card(
        modifier = Modifier
            .size(width = 100.dp, height = 160.dp)
            .graphicsLayer {
                translationX =
                    if (animationState.isAnimating) animatedTranslationX else 0f
                translationY =
                    if (animationState.isAnimating) animatedTranslationY else 0f
                rotationZ = animatedRotation
                transformOrigin = TransformOrigin(0.5f, 1.0f)
            }
            .let { modifier ->
                if (shouldBringToFront) {
                    modifier.zIndex(1000f)
                } else {
                    modifier
                }
            },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = (4 + cardIndex).dp
        )
    ) {
        Image(
            modifier = Modifier.fillMaxSize(),
            painter = painterResource(cardData.imageResId),
            contentDescription = null,
            contentScale = ContentScale.Crop
        )
    }
}

fun handleAnimationStepComplete(
    step: Int,
    cardIndex: Int,
    onStepChange: (Int) -> Unit,
    onAnimationComplete: () -> Unit
) {
    if (cardIndex == 0) {
        when (step) {
            1 -> onStepChange(2)
            2 -> onStepChange(3)
            3 -> onAnimationComplete()
        }
    }
}
