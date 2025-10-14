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
import ru.yandexpraktikum.cardsanimation.compose.AnimationConstants.ANIMATION_DURATION
import ru.yandexpraktikum.cardsanimation.compose.AnimationConstants.BASE_ELEVATION
import ru.yandexpraktikum.cardsanimation.compose.AnimationConstants.CARD_CORNER_RADIUS
import ru.yandexpraktikum.cardsanimation.compose.AnimationConstants.CARD_HEIGHT
import ru.yandexpraktikum.cardsanimation.compose.AnimationConstants.CARD_WIDTH
import ru.yandexpraktikum.cardsanimation.compose.AnimationConstants.QUICK_ANIMATION_DURATION
import ru.yandexpraktikum.cardsanimation.compose.enums.AnimationStep
import ru.yandexpraktikum.cardsanimation.model.CardData
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AnimatedCard(
    cardIndex: Int,
    cardData: CardData,
    targetRotation: Float,
    animationStep: AnimationStep,
    isAnimating: Boolean,
    onAnimationStepComplete: ((step: AnimationStep) -> Unit)? = null
) {
    val density = LocalDensity.current

    val finalRotation = if (cardIndex == 0) 0f else targetRotation


    val animatedRotation by animateFloatAsState(
        targetValue = when {
            animationStep == AnimationStep.FINAL_ROTATION -> finalRotation
            isAnimating -> targetRotation
            else -> targetRotation
        },
        animationSpec = tween(durationMillis = if (animationStep == AnimationStep.FINAL_ROTATION) QUICK_ANIMATION_DURATION else ANIMATION_DURATION),
        finishedListener = {
            if (animationStep == AnimationStep.FINAL_ROTATION && isAnimating) {
                onAnimationStepComplete?.invoke(AnimationStep.FINAL_ROTATION)
            }
        },
        label = "rotation"
    )

    val animatedTranslationX by animateFloatAsState(
        targetValue = when {
            cardIndex == 0 && isAnimating && animationStep == AnimationStep.MOVE_OUT -> {
                val moveDistance = with(density) { 50.dp.toPx() }
                val rotationRad = Math.toRadians(targetRotation.toDouble())
                moveDistance * cos(rotationRad).toFloat()
            }
            cardIndex == 0 && isAnimating && animationStep == AnimationStep.MOVE_BACK -> {
                0f
            }
            else -> 0f
        },
        animationSpec = tween(durationMillis = 300),
        finishedListener = {
            if (isAnimating && cardIndex == 0) {
                if (animationStep == AnimationStep.MOVE_BACK) {
                    onAnimationStepComplete?.invoke(AnimationStep.MOVE_OUT)
                } else if(animationStep==AnimationStep.MOVE_OUT) {
                    onAnimationStepComplete?.invoke(AnimationStep.MOVE_BACK)
                }
            }
        },
        label = "translationX"
    )

    val animatedTranslationY by animateFloatAsState(
        targetValue = when {
            cardIndex == 0 && isAnimating && animationStep == AnimationStep.MOVE_OUT -> {
                val moveDistance = with(density) { 50.dp.toPx() }
                val rotationRad = Math.toRadians(targetRotation.toDouble())
                moveDistance * sin(rotationRad).toFloat()
            }
            cardIndex == 0 && isAnimating && animationStep == AnimationStep.MOVE_BACK -> {
                0f
            }
            else -> 0f
        },
        animationSpec = tween(durationMillis = 300),
        label = "translationY"
    )

    val shouldBringToFront = isAnimating && animationStep >= AnimationStep.MOVE_BACK && cardIndex == 0
    Card(
        modifier = Modifier
            .size(width = CARD_WIDTH, height = CARD_HEIGHT)
            .graphicsLayer {
                translationX =  animatedTranslationX
                translationY =  animatedTranslationY
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
        shape = RoundedCornerShape(CARD_CORNER_RADIUS),
        elevation = CardDefaults.cardElevation(
            defaultElevation = (BASE_ELEVATION + cardIndex).dp
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