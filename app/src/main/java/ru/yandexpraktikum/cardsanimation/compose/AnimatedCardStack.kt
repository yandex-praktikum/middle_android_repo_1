package ru.yandexpraktikum.cardsanimation.compose

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import ru.yandexpraktikum.cardsanimation.model.CardData
import ru.yandexpraktikum.cardsanimation.model.CardSwapAnimationState
import ru.yandexpraktikum.cardsanimation.utils.handleHorizontalSwipe
import ru.yandexpraktikum.cardsanimation.utils.handleVerticalSwipe
import kotlin.math.abs

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

private fun calculateFinalRotation(
    cardIndex: Int,
    cardCount: Int,
    isRotated: Boolean
): Float {

    return if (isRotated) {
        val angleStep = if (cardCount > 1) 180f / (cardCount - 1) else 0f
        90f - (cardIndex * angleStep)
    } else {
        val angleStep = if (cardCount > 1) 45f / (cardCount - 1) else 0f
        22.5f - (cardIndex * angleStep)
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

@Composable
fun AnimatedCardStack(cards: List<CardData>) {
    val cardCount = cards.size
    var isRotated by remember { mutableStateOf(false) }
    var currentCards by remember {
        mutableStateOf(cards)
    }
    var animationStep by remember { mutableStateOf(0) }
    var isAnimating by remember { mutableStateOf(false) }

    var animationState by remember { mutableStateOf(CardSwapAnimationState()) }
    var verticalDragOffset = 0f
    var horizontalDragOffset = 0f


    Box(
        modifier = Modifier
            .pointerInput(Unit) {
                detectDragGestures(
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
                                    handleHorizontalSwipe(
                                        horizontalDragDistance = horizontalDragOffset,
                                        onCardsReorder = {
                                            currentCards = reorderCards(currentCards)
                                        }
                                    )
                                }
                            }
                        }
                        verticalDragOffset = 0f
                        horizontalDragOffset = 0f
                    }
                ) { _, dragAmount ->
                    verticalDragOffset += dragAmount.y
                    horizontalDragOffset += dragAmount.x
                }
            }
    ) {
        currentCards.forEachIndexed { i, cardData ->
            key(cardData.imageResId) {
                val targetRotation = calculateCardRotation(i, cardCount, isRotated)
                AnimatedCard(
                    cardIndex = i,
                    targetRotation = targetRotation,
                    cardData = cardData,
                    finalRotation = calculateFinalRotation(i, cardCount, isRotated),
                    animationStep = animationStep,
                    isAnimating = isAnimating,
                    onAnimationStepComplete = {
                        handleAnimationStepComplete(
                            step = it,
                            cardIndex = i,
                            onStepChange = {

                            },
                            onAnimationComplete = {

                            }
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