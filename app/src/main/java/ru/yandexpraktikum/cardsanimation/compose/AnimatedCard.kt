package ru.yandexpraktikum.cardsanimation.compose

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
    isAnimating: Boolean = false,
    animationStep: Int = 0,
    isSwipeRight: Boolean = true,
    onAnimationStepComplete: ((Int) -> Unit)? = null
) {
    val density = LocalDensity.current

    val animatedRotation by animateFloatAsState(
        targetValue = targetRotation,
        animationSpec = if (animationStep == 3) {
            tween(durationMillis = 500)
        } else {
            spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow)
        },
        finishedListener = {
            if (animationStep == 3 && isAnimating) onAnimationStepComplete?.invoke(3)
        },
        label = "rotation"
    )

    val animatedTranslationX by animateFloatAsState(
        targetValue = when {
            isAnimating && animationStep == 1 -> {
                val moveDistance = with(density) { 60.dp.toPx() } * (if (isSwipeRight) 1f else -1f)
                val rotationRad = Math.toRadians(targetRotation.toDouble())
                (moveDistance * cos(rotationRad)).toFloat()
            }

            isAnimating && animationStep == 2 -> 0f
            else -> 0f
        },
        animationSpec = tween(durationMillis = 400),
        finishedListener = {
            if (isAnimating) {
                when (animationStep) {
                    1 -> onAnimationStepComplete?.invoke(1)
                    2 -> onAnimationStepComplete?.invoke(2)
                }
            }
        },
        label = "translationX"
    )

    val animatedTranslationY by animateFloatAsState(
        targetValue = when {
            isAnimating && animationStep == 1 -> {
                val moveDistance = with(density) { 60.dp.toPx() } * (if (isSwipeRight) 1f else -1f)
                val rotationRad = Math.toRadians(targetRotation.toDouble())
                (-moveDistance * sin(rotationRad)).toFloat()
            }

            isAnimating && animationStep == 2 -> 0f
            else -> 0f
        },
        animationSpec = tween(durationMillis = 400),
        label = "translationY"
    )

    val animatedElevation by animateDpAsState(
        targetValue = if (isAnimating && animationStep >= 2) 20.dp else (4 + cardIndex).dp,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "elevation"
    )

    val zIndexValue = when {
        isAnimating && animationStep >= 2 -> 1000f
        else -> cardIndex.toFloat()
    }

    Card(
        modifier = Modifier
            .size(width = 100.dp, height = 160.dp)
            .zIndex(zIndexValue)
            .graphicsLayer {
                translationX = if (isAnimating) animatedTranslationX else 0f
                translationY = if (isAnimating) animatedTranslationY else 0f
                rotationZ = animatedRotation
                transformOrigin = TransformOrigin(0.5f, 1.0f)

                if (isAnimating && animationStep == 2) {
                    scaleX = 1.05f
                    scaleY = 1.05f
                }
            },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = animatedElevation
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
