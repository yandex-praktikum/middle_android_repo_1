package ru.yandexpraktikum.cardsanimation.compose

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
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
    onAnimationStepComplete: ((Int) -> Unit)? = null
) {
    val animatedRotation by animateFloatAsState(
        targetValue = targetRotation,
        animationSpec = tween(durationMillis = 800)
    )

    val translationX = remember { Animatable(0f) }
    val translationY = remember { Animatable(0f) }

    LaunchedEffect(isAnimating, animationStep) {
        if (isAnimating && animationStep == 1 && cardIndex == 0) {
            coroutineScope {
                launch {
                    translationX.animateTo(
                        targetValue = 150f * cos(Math.toRadians(targetRotation.toDouble())).toFloat(),
                        animationSpec = tween(300, easing = LinearOutSlowInEasing)
                    )
                }
                launch {
                    translationY.animateTo(
                        targetValue = 150f * sin(Math.toRadians(targetRotation.toDouble())).toFloat(),
                        animationSpec = tween(300, easing = LinearOutSlowInEasing)
                    )
                }
            }
            onAnimationStepComplete?.invoke(1)
        } else if (!isAnimating) {
            launch { translationX.snapTo(0f) }
            launch { translationY.snapTo(0f) }
        }
    }


    Card(
        modifier = Modifier
            .size(100.dp, 160.dp)
            .zIndex(cardIndex.toFloat())
            .graphicsLayer {
                this.translationX = translationX.value
                this.translationY = translationY.value
                rotationZ = animatedRotation
                transformOrigin = TransformOrigin(0.5f, 1.0f)
            },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation((4 + cardIndex).dp)
    ) {
        Image(
            modifier = Modifier.fillMaxSize(),
            painter = painterResource(cardData.imageResId),
            contentDescription = null,
            contentScale = ContentScale.Crop
        )
    }
}
