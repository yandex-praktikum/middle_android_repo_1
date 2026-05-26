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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import ru.yandexpraktikum.cardsanimation.model.CardData
import ru.yandexpraktikum.cardsanimation.utils.SwipeDirection
import ru.yandexpraktikum.cardsanimation.utils.handleDragEnd

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

private fun reorderCards(cards: List<CardData>): List<CardData> {
    return cards.drop(1) + cards.first()
}

private data class CardSwapAnimationState(
    val isAnimating: Boolean = false,
    val animationStep: Int = 0,
)

@Composable
fun AnimatedCardStack(cards: List<CardData>) {
    val cardCount = cards.size
    var endOffset = Offset.Zero
    var startOffset = Offset.Zero
    var isRotated by remember { mutableStateOf(false) }
    var cards by remember { mutableStateOf(cards) }
    var animationState by remember { mutableStateOf(CardSwapAnimationState()) }

    fun nextStepAnimate(step: Int) {
        animationState = CardSwapAnimationState(
            isAnimating = true,
            animationStep = step
        )
    }

    fun finishAnimate() {
        cards = reorderCards(cards)
        animationState = CardSwapAnimationState(
            isAnimating = false,
            animationStep = 0
        )
    }

    Box(
        modifier = Modifier
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        startOffset = offset
                    },
                    onDragEnd = {
                        if (!animationState.isAnimating) {
                            val verticalDragOffset = startOffset.y - endOffset.y
                            val horizontalDragOffset = startOffset.x - endOffset.x

                            val swipeDirection = handleDragEnd(
                                verticalDragOffset = verticalDragOffset,
                                horizontalDragOffset = horizontalDragOffset,
                            )
                            when (swipeDirection) {
                                SwipeDirection.SWIPE_UP -> isRotated = true
                                SwipeDirection.SWIPE_DOWN -> isRotated = false
                                SwipeDirection.SWIPE_LEFT,
                                SwipeDirection.SWIPE_RIGHT -> { nextStepAnimate(1) }
                                SwipeDirection.UNRECOGNIZED -> Unit
                            }
                        }

                        endOffset = Offset.Zero
                        startOffset = Offset.Zero
                    }
                ) { change, dragAmount ->
                    endOffset += dragAmount
                    change.consume()
                }
            },
        contentAlignment = Alignment.Center
    ) {
        cards.forEachIndexed { i, cardData ->
            key(cardData.imageResId) {
                val targetRotation = calculateCardRotation(i, cardCount, isRotated)

                val shiftIndex = (i + cards.lastIndex) % cardCount
                val finalRotation = calculateCardRotation(shiftIndex, cardCount, isRotated)

                AnimatedCard(
                    cardIndex = i,
                    targetRotation = targetRotation,
                    finalRotation = finalRotation,
                    cardData = cardData,
                    isAnimating = animationState.isAnimating,
                    animationStep = animationState.animationStep,
                    onAnimationStepComplete = if (i == 0) { step ->
                        when (step) {
                            1 -> nextStepAnimate(2)
                            2 -> nextStepAnimate(3)
                            3 -> finishAnimate()
                        }
                    } else null
                )
            }
        }
    }
}