package ru.yandexpraktikum.cardsanimation.compose

import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ru.yandexpraktikum.cardsanimation.model.CardData
import kotlin.math.abs
import kotlin.math.absoluteValue
import kotlin.math.cos
import kotlin.math.sin


const val DURATION_TRANSLATION = 300
const val DURATION_ROTATION = 1000

/**
 * Метод для вычисления поворота карты в конкретной позиции
 */
fun calculateCardRotation(
    cardIndex: Int, cardCount: Int, isRotated: Boolean
): Float {
    if (cardCount <= 1) return 0f

    return if (isRotated) {
        val angleStep = 180f / (cardCount - 1)
        90f - (cardIndex * angleStep)
    } else {
        val angleStep = 45f / (cardCount - 1)
        22.5f - (cardIndex * angleStep)
    }
}


@Composable
fun AnimatedCardStack(inputCards: List<CardData>) {
    var cards: List<CardData> by remember { mutableStateOf(inputCards.toList()) }
    val cardCount = inputCards.size
    var isRotated by remember { mutableStateOf(false) }
    var dragOffset by remember { mutableStateOf(Offset(0f, 0f)) }
    var animationState by remember { mutableStateOf(CardSwapAnimationState()) }

    // TODO: [Задание 2] Добавьте обработку жестов
    // Подсказка: Используйте Modifier.pointerInput() с методом detectDragGestures()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectDragGestures(onDragEnd = {
                    Log.d("AnimatedCardStack", "animationState = ${animationState.isAnimating}")
                    if (!animationState.isAnimating) {
                        val threshold = 100f
                        val isVerticalDominant =
                            dragOffset.y.absoluteValue > dragOffset.x.absoluteValue
                        val isHorizontalDominant =
                            dragOffset.y.absoluteValue < dragOffset.x.absoluteValue

                        when {
                            isVerticalDominant && dragOffset.y.absoluteValue > threshold -> {
                                handleVerticalSwipe(verticalDragDistance = dragOffset.y) {
                                    isRotated = it
                                    animationState = animationState.copy(
                                        isAnimating = true,
                                        animationStep = AnimationStep.ALL_ROTATE_TO_TARGET
                                    )
                                }
                            }

                            isHorizontalDominant && dragOffset.x.absoluteValue > threshold ->
                                //horizontal swipe
                                handleHorizontalSwipe(dragOffset.x) {
                                    animationState = animationState.copy(
                                        isAnimating = true,
                                        animationStep = AnimationStep.FIRST_MOVE_TO_RIGHT
                                    )
                                }

                        }
                    }
                    dragOffset = Offset(0f, 0f)
                }, onDrag = { change, dragAmount ->
//                    Log.d("AnimatedCardStack", "change = ${change.position}")
                    // Log.d("AnimatedCardStack", "dragAmount = $dragAmount")

                    dragOffset = dragOffset.copy(
                        x = dragOffset.x + dragAmount.x, y = dragOffset.y + dragAmount.y
                    )

                })

            }, contentAlignment = Alignment.Center
    ) {

        cards.forEachIndexed { i, cardData ->
            key(cardData.imageResId) {
                val targetRotation = calculateCardRotation(i, cardCount, isRotated)

                handleCardAnimation(
                    cardIndex = i,
                    cardData = cardData,
                    targetRotation = targetRotation,
                    isAnimating = animationState.isAnimating,
                    animationStep = animationState.animationStep
                ) { finishedAnimationStep ->

                    Log.d("AnimatedCardStack", "finishedAnimationStep = $finishedAnimationStep")
                    animationState = when (finishedAnimationStep) {
                        AnimationStep.FIRST_MOVE_TO_RIGHT ->
                            animationState.copy(animationStep = AnimationStep.SECOND_MOVE_TO_CENTER)

                        AnimationStep.SECOND_MOVE_TO_CENTER -> {
                            cards = reorderCards(cards)
                            animationState.copy(animationStep = AnimationStep.LAST_ROTATE_TO_TARGET)
                        }

//                        AnimationStep.LAST_ROTATE_TO_TARGET -> {
//
//                        }

                        else -> animationState.copy(isAnimating = false, animationStep = null)
                    }
                }

            }
        }
    }
}

// Простая функция перестановки карт
fun reorderCards(cards: List<CardData>): List<CardData> {
    return cards.drop(1) + cards.first()
}

fun handleVerticalSwipe(verticalDragDistance: Float, onStateChange: (Boolean) -> Unit) {
    if (verticalDragDistance > 0) onStateChange(true) else onStateChange(false)
}

fun handleHorizontalSwipe(horizontalDragDistance: Float, onAnimationStateChange: () -> Unit) {
    //if (verticalDragDistance > 0) onStateChange(true) else onStateChange(false)
    Log.d("AnimatedCardStack", "handleHorizontalSwipe")
    onAnimationStateChange()

}

@Composable
fun handleCardAnimation(
    cardIndex: Int,
    cardData: CardData,
    targetRotation: Float,
    isAnimating: Boolean,
    animationStep: AnimationStep?,
    onAnimationStepComplete: ((AnimationStep) -> Unit)?
) {

    val animatedRotation by animateFloatAsState(
        targetValue = when {
            isAnimating && animationStep == AnimationStep.LAST_ROTATE_TO_TARGET -> targetRotation
            isAnimating && animationStep == AnimationStep.ALL_ROTATE_TO_TARGET -> targetRotation
            else -> targetRotation
        },
        animationSpec = tween(durationMillis = DURATION_ROTATION),
        finishedListener = {
            if (isAnimating && cardIndex == 0 &&
                (animationStep == AnimationStep.LAST_ROTATE_TO_TARGET ||
                        animationStep == AnimationStep.ALL_ROTATE_TO_TARGET)
            )

                onAnimationStepComplete?.invoke(animationStep)
        }, label = "rotation"
    )


    if (cardIndex == 0) {
        val animatedTranslationX by animateFloatAsState(
            targetValue = when {
                isAnimating && animationStep == AnimationStep.FIRST_MOVE_TO_RIGHT -> {
                    val moveDistance = 50.dp.toPx()
                    val rotationRad = Math.toRadians(targetRotation.toDouble())
                    moveDistance * cos(rotationRad).toFloat()
                }

                isAnimating && animationStep == AnimationStep.SECOND_MOVE_TO_CENTER -> 0f
                else -> 0f
            }, animationSpec = tween(durationMillis = DURATION_TRANSLATION), finishedListener = {
                if (isAnimating && animationStep != null) onAnimationStepComplete?.invoke(
                    animationStep
                )
            }, label = "translationX"
        )


        val animatedTranslationY by animateFloatAsState(
            targetValue = when {
                isAnimating && animationStep == AnimationStep.FIRST_MOVE_TO_RIGHT -> {
                    val moveDistance = 50.dp.toPx()
                    val rotationRad = Math.toRadians(targetRotation.toDouble())
                    moveDistance * sin(rotationRad).toFloat()
                }

                isAnimating && animationStep == AnimationStep.SECOND_MOVE_TO_CENTER -> 0f
                else -> 0f
            },
            animationSpec = tween(durationMillis = DURATION_TRANSLATION), /*finishedListener = {
            if (isAnimating && animationStep != null) onAnimationStepComplete?.invoke(
                animationStep
            )
        },*/ label = "translationY"
        )
        AnimatedCard(
            cardIndex = cardIndex,
            cardData = cardData,
            targetRotation = animatedRotation,
            animatedTranslationX = animatedTranslationX,
            animatedTranslationY = animatedTranslationY,
            shouldBringToFront = isAnimating && animationStep == AnimationStep.SECOND_MOVE_TO_CENTER
            // TODO: [Задание 5] Здесь добавьте параметры анимации карты
        )

    } else {
        AnimatedCard(
            cardIndex = cardIndex,
            cardData = cardData,
            targetRotation = animatedRotation
        )
    }
}

@Composable
fun Dp.toPx(): Float {
    return with(LocalDensity.current) {
        toPx()
    }
}