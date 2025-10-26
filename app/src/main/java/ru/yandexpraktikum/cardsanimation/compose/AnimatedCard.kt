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
import ru.yandexpraktikum.cardsanimation.model.AnimationStep
import ru.yandexpraktikum.cardsanimation.model.CardData
import ru.yandexpraktikum.cardsanimation.model.CardSwapAnimationState
import kotlin.math.cos
import kotlin.math.sin

private const val ANIMATION_DURATION_IN_MILLIS = 300
private const val LONG_ANIMATION_DURATION_IN_MILLIS = 800
private const val MOVE_DISTANCE_IN_DP = 50
private const val Z_INDEX = 1000f

@Composable
fun AnimatedCard(
    cardIndex: Int,
    cardData: CardData,
    targetRotation: Float,
    finalRotation: Float,
    animationState: CardSwapAnimationState,
    onAnimationStepComplete: (AnimationStep) -> Unit
) {
    val density = LocalDensity.current
    val animationStep = animationState.animationStep
    val isAnimating = animationState.isAnimating
    val needAnimateCard = cardIndex == 0 && isAnimating
    val shouldBringToFront = needAnimateCard && animationStep.shouldBringToFront()
    val animatedRotationDuration = if (animationStep == AnimationStep.MOVE_TO_START_POSITION) {
        ANIMATION_DURATION_IN_MILLIS
    } else {
        LONG_ANIMATION_DURATION_IN_MILLIS
    }
    val moveDistanceInPx = with(density) { MOVE_DISTANCE_IN_DP.dp.toPx() }
    val rotationRad = Math.toRadians(targetRotation.toDouble())

    val animatedRotation by animateFloatAsState(
        targetValue = if (animationStep == AnimationStep.MOVE_TO_START_POSITION) finalRotation else targetRotation,
        animationSpec = tween(durationMillis = animatedRotationDuration),
        finishedListener = {
            if (animationStep == AnimationStep.MOVE_TO_START_POSITION && isAnimating) {
                onAnimationStepComplete.invoke(animationStep)
            }
        },
        label = "rotation"
    )
    val animatedTranslationX by animateFloatAsState(
        targetValue = if (isAnimating && animationStep == AnimationStep.MOVE_TO_RIGHT) {
            moveDistanceInPx * cos(rotationRad).toFloat()
        } else {
            0f
        },
        animationSpec = tween(durationMillis = ANIMATION_DURATION_IN_MILLIS),
        finishedListener = {
            if (isAnimating) {
                when (animationStep) {
                    AnimationStep.MOVE_TO_RIGHT -> onAnimationStepComplete.invoke(animationStep)
                    AnimationStep.MOVE_TO_CENTER -> onAnimationStepComplete.invoke(animationStep)
                    else -> {}
                }
            }
        },
        label = "translationX"
    )

    val animatedTranslationY by animateFloatAsState(
        targetValue = if (isAnimating && animationStep == AnimationStep.MOVE_TO_RIGHT) {
            moveDistanceInPx * sin(rotationRad).toFloat()
        } else {
            0f
        },
        animationSpec = tween(durationMillis = ANIMATION_DURATION_IN_MILLIS),
        label = "translationY"
    )

    Card(
        modifier = Modifier
            .size(width = 100.dp, height = 160.dp)
            .graphicsLayer {
                translationX = if (needAnimateCard) animatedTranslationX else 0f
                translationY = if (needAnimateCard) animatedTranslationY else 0f
                rotationZ = animatedRotation
                transformOrigin = TransformOrigin(0.5f, 1.0f)
            }
            .let { modifier -> if (shouldBringToFront) modifier.zIndex(Z_INDEX) else modifier },
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