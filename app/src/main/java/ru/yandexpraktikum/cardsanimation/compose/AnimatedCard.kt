package ru.yandexpraktikum.cardsanimation.compose

import android.util.Log
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import ru.yandexpraktikum.cardsanimation.Helper
import ru.yandexpraktikum.cardsanimation.Helper.ANIM_DURATION_MS
import ru.yandexpraktikum.cardsanimation.model.AnimationState
import ru.yandexpraktikum.cardsanimation.model.CardData
import ru.yandexpraktikum.cardsanimation.model.isMovingStep
import ru.yandexpraktikum.cardsanimation.model.isToTopStep
import ru.yandexpraktikum.cardsanimation.model.isFinalStep

@Composable
fun AnimatedCard(
    cardIndex: Int,
    cardData: CardData,
    targetRotation: Float,
    animationState: AnimationState,
    onStepFinished: (currStep: Int) -> Unit
) {
    val cardIdAnim by remember { mutableIntStateOf(cardIndex) }
    val shouldAnimated = cardIndex == animationState.currentAnimatingCard

    // [Задание 1] анимация поворота карты
    val rotationAnim by animateFloatAsState(
        targetValue = targetRotation,
        animationSpec = tween(durationMillis = ANIM_DURATION_MS.toInt(), easing = LinearEasing)
    ) {
        if (!shouldAnimated) return@animateFloatAsState
        when {
            animationState.isFinalStep() -> onStepFinished.invoke(3)
        }
    }

    // [Задание 5] движение карты
    val moveXAnim by animateFloatAsState(
        targetValue = if (animationState.isMovingStep()) Helper.calculateMove(rotationAnim).first else 0f,
        animationSpec = tween(durationMillis = ANIM_DURATION_MS.toInt(), easing = LinearEasing)
    ) {
        if (!shouldAnimated) return@animateFloatAsState
        when {
            animationState.isMovingStep() -> onStepFinished.invoke(1)
            animationState.isToTopStep() -> onStepFinished.invoke(2)
        }
    }
    val moveYAnim by animateFloatAsState(
        targetValue = if (animationState.isMovingStep()) Helper.calculateMove(rotationAnim).second else 0f,
        animationSpec = tween(durationMillis = ANIM_DURATION_MS.toInt(), easing = LinearEasing)
    )

    fun bringToFront() = shouldAnimated && animationState.isAnimating && animationState.step >= 2

    SideEffect {
        Log.d("AnimatedCard", "step=${animationState.step} idx=$cardIndex new state=($moveXAnim, $moveYAnim, $targetRotation, $shouldAnimated)")
    }

    Card(
        modifier = Modifier
            .size(width = 100.dp, height = 160.dp)
            // [Задание 5] плюс анимация карты при свайпе вправо или влево
            .graphicsLayer {
                if (shouldAnimated) {
                    translationX = -moveXAnim
                    translationY = moveYAnim
                }
                rotationZ = rotationAnim
                transformOrigin = TransformOrigin(0.5f, 1.0f)
            }.let {
                if (bringToFront()) it.zIndex(1000f) else it
            },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = (4 + cardIdAnim).dp
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