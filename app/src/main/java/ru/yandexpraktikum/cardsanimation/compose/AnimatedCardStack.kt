package ru.yandexpraktikum.cardsanimation.compose

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
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
import ru.yandexpraktikum.cardsanimation.model.CardSwapAnimationState
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
    val cardCount = cards.size
    var dragOffset = Offset.Zero
    var currentCards by remember { mutableStateOf(cards) }
    var isRotated by remember { mutableStateOf(false) }
    var animationState by remember { mutableStateOf(CardSwapAnimationState()) }
    // id карты, чтобы корректно анимировать после изменения порядка карт
    var animatingCardId by remember { mutableStateOf(0) }

    fun reorderCardsData() {
        val reorderedCards = currentCards.drop(1) + currentCards.first()
        currentCards = reorderedCards
    }

    fun handleDragEnd() {
        if (animationState.isAnimating) return

        val threshold = 100f
        val isVerticalDominant = abs(dragOffset.y) > abs(dragOffset.x)
        val isHorizontalDominant = abs(dragOffset.x) > abs(dragOffset.y)

        when {
            isVerticalDominant && abs(dragOffset.y) > threshold -> {
                handleVerticalSwipe(
                    verticalDragDistance = dragOffset.y,
                    onFanStateChange = { newState ->
                        isRotated = newState
                    },
                )
            }

            isHorizontalDominant && abs(dragOffset.x) > threshold -> {
                handleHorizontalSwipe(
                    horizontalDragDistance = dragOffset.x,
                    onCardsReorder = {
                        animatingCardId = currentCards.first().imageResId
                        animationState = animationState.copy(isAnimating = true, animationStep = 1)
                    },
                )
            }
        }

        dragOffset = Offset.Zero
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        handleDragEnd()
                    },
                ) { _, dragAmount ->
                    dragOffset += dragAmount
                }
            },
        contentAlignment = Alignment.Center
    ) {
        currentCards.forEachIndexed { i, cardData ->
            key(cardData.imageResId) {
                val targetRotation = calculateCardRotation(i, cardCount, isRotated)

                AnimatedCard(
                    cardIndex = i,
                    targetRotation = targetRotation,
                    cardData = cardData,
                    isAnimating = if (cardData.imageResId == animatingCardId) {
                        animationState.isAnimating
                    } else {
                        false
                    },
                    animationStep = if (cardData.imageResId == animatingCardId) {
                        animationState.animationStep
                    } else {
                        0
                    },
                    onAnimationStepComplete = { step ->
                        if (cardData.imageResId == animatingCardId) {
                            handleAnimationStepComplete(
                                step = step,
                                onStepChange = { newStep ->
                                    animationState = animationState.copy(animationStep = newStep)
                                    if (newStep == 3) {
                                        reorderCardsData()
                                    }
                                },
                                onAnimationComplete = {
                                    animationState = animationState.copy(
                                        isAnimating = false,
                                        animationStep = 0
                                    )
                                    animatingCardId = 0
                                },
                            )
                        }
                    }
                )
            }
        }
    }
}

private fun handleVerticalSwipe(
    verticalDragDistance: Float,
    onFanStateChange: (Boolean) -> Unit,
) {
    if (verticalDragDistance == 0f) return

    if (verticalDragDistance < 0f) {
        onFanStateChange(true)
    } else {
        onFanStateChange(false)
    }
}

private fun handleHorizontalSwipe(
    horizontalDragDistance: Float,
    onCardsReorder: () -> Unit,
) {
    if (horizontalDragDistance == 0f) return

    onCardsReorder()
}

fun handleAnimationStepComplete(
    step: Int,
    onStepChange: (Int) -> Unit,
    onAnimationComplete: () -> Unit,
) {
    when (step) {
        1 -> onStepChange(2)
        2 -> onStepChange(3)
        3 -> onAnimationComplete()
    }
}