package ru.yandexpraktikum.cardsanimation.compose

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import kotlin.math.abs

data class CardSwapAnimationState(
    val isAnimating: Boolean = false,
    val animationStep: Int = 0
)

/**
 * Метод для вычисления поворота карты в конкретной позиции
 */
fun calculateCardRotation(
    cardIndex: Int,
    cardCount: Int,
    isRotated: Boolean
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
fun AnimatedCardStack(cards: List<CardData>) {
    val cardCount = cards.size
    var isRotated by remember { mutableStateOf(false) }

    var currentCards by remember { mutableStateOf(cards) }

    var horizontalDragOffset by remember { mutableFloatStateOf(0f) }
    var verticalDragOffset by remember { mutableFloatStateOf(0f) }

    var animationState by remember { mutableStateOf(CardSwapAnimationState()) }

    val onStepChange: (Int) -> Unit = { newStep ->
        animationState = animationState.copy(animationStep = newStep)
    }

    val onAnimationComplete: () -> Unit = {
        animationState = animationState.copy(isAnimating = false, animationStep = 0)

        currentCards = reorderCards(currentCards)
    }

    Box(
        modifier = Modifier.pointerInput(Unit) {
            detectDragGestures(
                onDragStart = {
                    horizontalDragOffset = 0f
                    verticalDragOffset = 0f
                },
                onDrag = { change, dragAmount ->
                    change.consume()

                    horizontalDragOffset += dragAmount.x
                    verticalDragOffset += dragAmount.y
                },
                onDragEnd = {
                    if (!animationState.isAnimating) {

                        val threshold = 100f
                        val isVerticalDominant = abs(verticalDragOffset) > abs(horizontalDragOffset)
                        val isHorizontalDominant = abs(horizontalDragOffset) > abs(verticalDragOffset)

                        when {
                            isVerticalDominant && abs(verticalDragOffset) > threshold -> {
                                handleVerticalSwipe(
                                    verticalDragDistance = verticalDragOffset,
                                    onFanStateChange = { newFanState -> isRotated = newFanState }
                                )
                            }
                            isHorizontalDominant && abs(horizontalDragOffset) > threshold -> {
                                animationState = animationState.copy(
                                    isAnimating = true,
                                    animationStep = 1
                                )
                                handleHorizontalSwipe(
                                    horizontalDragDistance = horizontalDragOffset,
                                    onCardsReorder = {
                                        //
                                    }
                                )
                            }
                        }
                    }
                    verticalDragOffset = 0f
                    horizontalDragOffset = 0f
                }
            )
        },
        contentAlignment = Alignment.Center,
    ) {
        currentCards.forEachIndexed { cardIndex, cardData ->
            key(cardData.imageResId) {
                val targetRotation = calculateCardRotation(cardIndex, cardCount, isRotated)

                AnimatedCard(
                    cardIndex = cardIndex,
                    targetRotation = targetRotation,
                    cardData = cardData,
                    isAnimating = animationState.isAnimating,
                    animationStep = animationState.animationStep,
                    onAnimationStepComplete = { step ->
                        handleAnimationStepComplete(
                            step = step,
                            cardIndex = cardIndex,
                            onStepChange = onStepChange,
                            onAnimationComplete = onAnimationComplete
                        )
                    }
                )
            }
        }
    }
}

// Простая функция перестановки карт
fun reorderCards(cards: List<CardData>): List<CardData> {
    return cards.drop(1) + cards.first()
}

fun handleVerticalSwipe(
    verticalDragDistance: Float,
    onFanStateChange: (Boolean) -> Unit,
    threshold: Float = 100f
) {
    when {
        verticalDragDistance < -threshold -> {
            onFanStateChange(false)
        }
        verticalDragDistance > threshold -> {
            onFanStateChange(true)
        }
    }
}

fun handleHorizontalSwipe(
    horizontalDragDistance: Float,
    onCardsReorder: () -> Unit,
    threshold: Float = 100f
) {
    val absoluteDistance = abs(horizontalDragDistance)

    if (absoluteDistance > threshold) {
        onCardsReorder()
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