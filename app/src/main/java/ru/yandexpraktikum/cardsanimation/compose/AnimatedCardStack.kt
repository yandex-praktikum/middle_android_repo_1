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
import ru.yandexpraktikum.cardsanimation.model.CardData
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
    var reorderCards by remember { mutableStateOf(cards) }
    var animationState by remember { mutableStateOf(CardSwapAnimationState()) }
    var isRotated by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .pointerInput(Unit) {
                var verticalDragOffset = 0f
                var horizontalDragOffset = 0f
                val verticalThreshold = 50f
                val horizontalThreshold = 50f
                detectDragGestures(
                    onDragEnd = {
                        if (!animationState.isAnimating) {
                            val isVertical =
                                if (abs(horizontalDragOffset) < abs(verticalDragOffset)) true else false
                            if (isVertical) {
                                if (verticalDragOffset < -verticalThreshold) {
                                    isRotated = true
                                } else if (verticalDragOffset > verticalThreshold) {
                                    isRotated = false
                                }
                            } else {
                                if (horizontalDragOffset > horizontalThreshold || horizontalDragOffset < -horizontalThreshold) {
                                    animationState =
                                        animationState.copy(isAnimating = true, animationStep = 1)
                                }
                            }
                        }
                        verticalDragOffset = 0f
                        horizontalDragOffset = 0f
                    }
                ) { _, offset ->
                    verticalDragOffset += offset.y
                    horizontalDragOffset += offset.x
                }
            },
        contentAlignment = Alignment.Center
    ) {
        reorderCards.forEachIndexed { i, cardData ->
            key(cardData.imageResId) {
                val targetRotation = calculateCardRotation(i, cardCount, isRotated)

                AnimatedCard(
                    cardIndex = i,
                    targetRotation = targetRotation,
                    cardData = cardData,
                    isAnimating = animationState.isAnimating,
                    animationStep = animationState.animationStep,
                    onAnimationStepComplete = { step ->
                        when (step) {
                            1 -> {
                                animationState =
                                    animationState.copy(isAnimating = true, animationStep = 2)
                            }

                            2 -> {
                                animationState =
                                    animationState.copy(isAnimating = false, animationStep = 0)
                                if (i == 0) reorderCards = reorderCards(reorderCards)
                            }
                        }
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