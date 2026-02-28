package ru.yandexpraktikum.cardsanimation.compose

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
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
    var cardList by remember(cards) { mutableStateOf(cards) }
    val cardCount = cardList.size
    var isRotated by remember { mutableStateOf(false) }

    var horizontalDragOffset by remember { mutableStateOf(0f) }
    var verticalDragOffset by remember { mutableStateOf(0f) }
    val threshold = 150f

    var isAnimating by remember { mutableStateOf(false) }
    var animationStep by remember { mutableStateOf(0) }

    Box(
        modifier = Modifier
            .pointerInput(isAnimating) {
                if (!isAnimating) {
                    detectDragGestures(
                        onDrag = { _, dragAmount ->
                            horizontalDragOffset += dragAmount.x
                            verticalDragOffset += dragAmount.y
                        },
                        onDragEnd = {
                            val absHorizontal = abs(horizontalDragOffset)
                            val absVertical = abs(verticalDragOffset)

                            if (absHorizontal > absVertical) {
                                if (absHorizontal > threshold) {
                                    isAnimating = true
                                    animationStep = 1
                                }
                            } else {
                                if (verticalDragOffset < -threshold) {
                                    isRotated = true
                                } else if (verticalDragOffset > threshold) {
                                    isRotated = false
                                }
                            }

                            horizontalDragOffset = 0f
                            verticalDragOffset = 0f
                        }
                    )
                }
            },
        contentAlignment = Alignment.Center
    ) {
        cardList.forEachIndexed { i, cardData ->
            key(cardData.imageResId) {
                val targetRotation = calculateCardRotation(i, cardCount, isRotated)

                AnimatedCard(
                    cardIndex = i,
                    targetRotation = targetRotation,
                    cardData = cardData,
                    isAnimating = isAnimating,
                    animationStep = animationStep,
                    onAnimationStepComplete = { step ->
                        if (step == 1) {
                            cardList = reorderCards(cardList)
                            isAnimating = false
                            animationStep = 0
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
