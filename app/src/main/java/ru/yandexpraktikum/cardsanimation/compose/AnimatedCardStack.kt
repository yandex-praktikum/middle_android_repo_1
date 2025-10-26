package ru.yandexpraktikum.cardsanimation.compose

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import ru.yandexpraktikum.cardsanimation.model.AnimationStep
import ru.yandexpraktikum.cardsanimation.model.CardData
import ru.yandexpraktikum.cardsanimation.model.CardSwapAnimationState
import kotlin.math.abs

private const val DRAG_THRESHOLD = 100f

@Composable
fun AnimatedCardStack(cards: List<CardData>) {
    val cardCount = cards.size
    var isRotated by remember { mutableStateOf(false) }
    var animationState by remember { mutableStateOf(CardSwapAnimationState()) }
    var currentCards by remember { mutableStateOf(cards) }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .addCardPointerInput(
                isAnimating = animationState.isAnimating,
                onVerticalSwipe = { isUpSwipe -> isRotated = isUpSwipe },
                onHorizontalSwipe = {
                    animationState =
                        animationState.copy(isAnimating = true, animationStep = AnimationStep.MOVE_TO_RIGHT)
                }
            )
    ) {
        currentCards.forEachIndexed { cardIndex, cardData ->
            key(cardData.imageResId) {
                val targetRotation = calculateCardRotation(cardIndex, cardCount, isRotated)
                val finalRotation = if (cardIndex == 0) {
                    calculateCardRotation(currentCards.size - 1, cardCount, isRotated)
                } else {
                    calculateCardRotation(cardIndex - 1, cardCount, isRotated)
                }
                AnimatedCard(
                    cardIndex = cardIndex,
                    targetRotation = targetRotation,
                    finalRotation = finalRotation,
                    cardData = cardData,
                    animationState = animationState,
                    onAnimationStepComplete = { step ->
                        handleAnimationStepComplete(
                            step = step,
                            cardIndex = cardIndex,
                            onStepChange = { animationState = animationState.copy(animationStep = it) },
                            onAnimationComplete = {
                                animationState = animationState.copy(
                                    isAnimating = false,
                                    animationStep = AnimationStep.MOVE_TO_RIGHT
                                )
                                currentCards = reorderCards(currentCards)
                            }
                        )
                    }
                )
            }
        }
    }
}

private fun Modifier.addCardPointerInput(
    isAnimating: Boolean,
    onVerticalSwipe: (isUpSwipe: Boolean) -> Unit,
    onHorizontalSwipe: () -> Unit
) = pointerInput(Unit) {
    var verticalDragOffset = 0f
    var horizontalDragOffset = 0f
    detectDragGestures(
        onDragEnd = {
            if (!isAnimating) {
                val isVerticalDominant = abs(verticalDragOffset) > abs(horizontalDragOffset)
                val isHorizontalDominant = abs(horizontalDragOffset) > abs(verticalDragOffset)
                when {
                    isHorizontalDominant && abs(horizontalDragOffset) > DRAG_THRESHOLD -> onHorizontalSwipe()
                    isVerticalDominant && abs(verticalDragOffset) > DRAG_THRESHOLD ->
                        onVerticalSwipe(verticalDragOffset < 0)
                }
            }
            verticalDragOffset = 0f
            horizontalDragOffset = 0f
        },
        onDrag = { _, dragAmount ->
            verticalDragOffset += dragAmount.y
            horizontalDragOffset += dragAmount.x
        }
    )
}

private fun handleAnimationStepComplete(
    step: AnimationStep,
    cardIndex: Int,
    onStepChange: (AnimationStep) -> Unit,
    onAnimationComplete: () -> Unit
) {
    if (cardIndex == 0) {
        when (step) {
            AnimationStep.MOVE_TO_RIGHT -> onStepChange(AnimationStep.MOVE_TO_CENTER)
            AnimationStep.MOVE_TO_CENTER -> onStepChange(AnimationStep.MOVE_TO_START_POSITION)
            AnimationStep.MOVE_TO_START_POSITION -> onAnimationComplete()
        }
    }
}

// Простая функция перестановки карт
private fun reorderCards(cards: List<CardData>): List<CardData> {
    return cards.drop(1) + cards.first()
}

/**
 * Метод для вычисления поворота карты в конкретной позиции
 */
private fun calculateCardRotation(
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