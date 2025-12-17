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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.zIndex
import kotlin.math.cos
import kotlin.math.sin
import ru.yandexpraktikum.cardsanimation.model.CardData

@Composable
fun AnimatedCard(
    cardIndex: Int,
    cardData: CardData,
    targetRotation: Float,
    isAnimating: Boolean,
    animationStep: Int,
    finalRotation: Float,
    onAnimationStepComplete: (Int) -> Unit,
) {
    val animatedRotation by animateFloatAsState(
        targetValue = if (animationStep == 3) finalRotation else targetRotation,
        animationSpec = tween(durationMillis = if (animationStep == 3) 300 else 800),
        finishedListener = {
            if (animationStep == 3 && isAnimating) onAnimationStepComplete(3)
        },
        label = "rotation"
    )
    val animatedTranslationX by animateFloatAsState(
        targetValue = if (isAnimating && animationStep == 1 && cardIndex == 0) {
            val moveDistance = with(LocalDensity.current) { 50.dp.toPx() }
            val rotationRad = Math.toRadians(targetRotation.toDouble())
            moveDistance * cos(rotationRad).toFloat()
        } else {
            0f
        },
        animationSpec = tween(durationMillis = 300),
        finishedListener = {
            if (isAnimating) {
                onAnimationStepComplete(animationStep)
            }
        },
        label = "translationX"
    )
    val animatedTranslationY by animateFloatAsState(
        targetValue = if (isAnimating && animationStep == 1 && cardIndex == 0) {
            val moveDistance = with(LocalDensity.current) { 50.dp.toPx() }
            val rotationRad = Math.toRadians(targetRotation.toDouble())
            moveDistance * sin(rotationRad).toFloat()
        } else {
            0f
        },
        animationSpec = tween(durationMillis = 300),
        label = "translationY"
    )
    Card(
        modifier = Modifier
            .size(width = 100.dp, height = 160.dp)
            .graphicsLayer {
                rotationZ = animatedRotation
                transformOrigin = TransformOrigin(0.5f, 1.0f)
                translationX = if (isAnimating) animatedTranslationX else 0f
                translationY = if (isAnimating) animatedTranslationY else 0f
            }
            .let { modifier ->
                if (isAnimating && animationStep == 2 && cardIndex == 0) {
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