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
import ru.yandexpraktikum.cardsanimation.model.CardData
import kotlin.math.abs

data class CardSwapAnimationState(
    val isAnimating: Boolean = false,
    val animationStep: Int = 0,
    val isSwipeRight: Boolean = true,
    val animatedCardId: Int? = null
)

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
    var currentCards by remember(cards) { mutableStateOf(cards) }
    val cardCount = currentCards.size
    var isRotated by remember { mutableStateOf(false) }
    var verticalDragOffset by remember { mutableFloatStateOf(0f) }
    var horizontalDragOffset by remember { mutableFloatStateOf(0f) }
    val threshold = 100f
    var animationState by remember { mutableStateOf(CardSwapAnimationState()) }

    Box(
        modifier = Modifier
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        if (!animationState.isAnimating) {
                            val isVerticalDominant =
                                abs(verticalDragOffset) > abs(horizontalDragOffset)
                            val isHorizontalDominant =
                                abs(horizontalDragOffset) > abs(verticalDragOffset)

                            when {
                                isVerticalDominant && abs(verticalDragOffset) > threshold -> {
                                    isRotated = verticalDragOffset < -threshold
                                }

                                isHorizontalDominant && abs(horizontalDragOffset) > threshold -> {
                                    animationState = CardSwapAnimationState(
                                        isAnimating = true,
                                        animationStep = 1,
                                        isSwipeRight = horizontalDragOffset > 0,
                                        animatedCardId = currentCards.firstOrNull()?.imageResId
                                    )
                                }
                            }
                        }
                        verticalDragOffset = 0f
                        horizontalDragOffset = 0f
                    }
                ) { change, dragAmount ->
                    if (!animationState.isAnimating) {
                        change.consume()
                        val (x, y) = dragAmount
                        if (abs(y) > abs(x)) {
                            verticalDragOffset += y
                        } else {
                            horizontalDragOffset += x
                        }
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        currentCards.forEachIndexed { i, cardData ->
            key(cardData.imageResId) {
                val isMovingCard = animationState.isAnimating &&
                        cardData.imageResId == animationState.animatedCardId
                val targetFanRotation = calculateCardRotation(i, cardCount, isRotated)

                AnimatedCard(
                    cardIndex = i,
                    targetRotation = targetFanRotation,
                    cardData = cardData,
                    isAnimating = isMovingCard,
                    animationStep = animationState.animationStep,
                    isSwipeRight = animationState.isSwipeRight,
                    onAnimationStepComplete = { step ->
                        if (isMovingCard) {
                            when (step) {
                                1 -> animationState = animationState.copy(animationStep = 2)
                                2 -> {
                                    currentCards = reorderCards(currentCards)
                                    animationState = animationState.copy(animationStep = 3)
                                }

                                3 -> {
                                    animationState = CardSwapAnimationState(
                                        isAnimating = false,
                                        animationStep = 0
                                    )
                                }
                            }
                        }
                    }
                )
            }
        }
    }
}

fun reorderCards(cards: List<CardData>): List<CardData> {
    return cards.drop(1) + cards.first()
}
