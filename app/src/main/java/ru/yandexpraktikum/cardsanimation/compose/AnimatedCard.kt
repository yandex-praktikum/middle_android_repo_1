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
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import ru.yandexpraktikum.cardsanimation.model.AnimationStep
import ru.yandexpraktikum.cardsanimation.model.CardData
import kotlin.math.cos
import kotlin.math.sin

private const val DEFAULT_ANIMATION_DURATION = 800
private const val FINAL_ANIMATION_DURATION = 300


@Composable
fun AnimatedCard(
    cardIndex: Int,
    cardData: CardData,
    targetRotation: Float,
    finalRotation: Float,
    animationStep: Int,
    isAnimating: Boolean,
    onAnimationStepComplete: ((Int) -> Unit)? = null
) {
    val density: Density = LocalDensity.current
    // TODO: [Задание 1] Добавьте анимацию поворота карты
    // Подсказка: используйте animateFloatAsState для плавной анимации
    val animatedRotation by animateFloatAsState(
        targetValue = when {
            animationStep == AnimationStep.FINAL.step -> finalRotation
            isAnimating -> targetRotation
            else -> targetRotation
        },
        animationSpec = tween(
            durationMillis = if (animationStep == AnimationStep.FINAL.step) {
                FINAL_ANIMATION_DURATION
            } else {
                DEFAULT_ANIMATION_DURATION
            }
        ),
        finishedListener = {
            if (
                animationStep == AnimationStep.FINAL.step &&
                isAnimating
            ) {
                onAnimationStepComplete?.invoke(AnimationStep.FINAL.step)
            }
        },
        label = "rotation"
    )

    val animatedTranslationX by animateFloatAsState(
        targetValue = when {
            isAnimating && animationStep == 1 -> {
                val moveDistance = with(density){ 50.dp.toPx() }
                val rotationRad = Math.toRadians(targetRotation.toDouble())
                moveDistance * cos(rotationRad).toFloat()
            }
            isAnimating && animationStep == 2 -> 0f // Движение в центр
            else -> 0f
        },
        animationSpec = tween(durationMillis = 300),
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
                val moveDistance = with(density) { 50.dp.toPx() }
                val rotationRad = Math.toRadians(targetRotation.toDouble())
                moveDistance * sin(rotationRad).toFloat()
            }
            isAnimating && animationStep == 2 -> 0f // Движение в центр
            else -> 0f
        },
        animationSpec = tween(durationMillis = 300),
        label = "translationY"
    )

// Добавьте zIndex для поднятия карты наверх
    val shouldBringToFront = isAnimating && animationStep >= 2


    Card(
        modifier = Modifier
            .size(width = 100.dp, height = 160.dp)
            // TODO: [Задание 5] Добавьте анимацию карты при свайпе вправо или влево
            .graphicsLayer {
                translationX = if (isAnimating) animatedTranslationX else 0f
                translationY = if (isAnimating) animatedTranslationY else 0f
                rotationZ = animatedRotation
                transformOrigin = TransformOrigin(0.5f, 1.0f)
            }.let { modifier ->
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

