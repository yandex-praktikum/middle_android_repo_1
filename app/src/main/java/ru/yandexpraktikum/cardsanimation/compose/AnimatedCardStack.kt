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
    var swipeDirection by remember { mutableStateOf(SwipeDirection.Unknown) }
    var cardsList by remember { mutableStateOf(cards) }
    var animationState by remember { mutableStateOf(CardSwapAnimationState()) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }


    val onStepChange: (Int) -> Unit = { newStep ->
        animationState = animationState.copy(
            animationStep = newStep,
            isAnimating = newStep != 0
        )
    }

    val onAnimationComplete = {
        cardsList = reorderCards(cardsList)
        animationState = CardSwapAnimationState()
    }

    Box(
        modifier = Modifier
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = {
                        offsetX = 0f
                        offsetY = 0f
                    },
                    onDragCancel = {
                        offsetX = 0f
                        offsetY = 0f
                    },
                    onDragEnd = {
                        if (!animationState.isAnimating) {
                            when (swipeDirection) {
                                SwipeDirection.Horizontal -> animationState =
                                    CardSwapAnimationState(
                                        isAnimating = true,
                                        animationStep = 1
                                    )

                                SwipeDirection.Vertical -> isRotated = !isRotated
                                SwipeDirection.Unknown -> Unit
                            }
                        }
                    }
                ) { change, dragAmount ->
                    change.consume()

                    offsetX += dragAmount.x
                    offsetY += dragAmount.y


                    swipeDirection = getSwipeDirection(
                        horizontal = offsetX,
                        vertical = offsetY,
                    )
                }
            },
        contentAlignment = Alignment.Center
    ) {
        cardsList.forEachIndexed { i, cardData ->
            key(cardData.imageResId) {
                val targetRotation = calculateCardRotation(i, cardCount, isRotated)
                val animationState = if (i == 0) animationState else CardSwapAnimationState()
                AnimatedCard(
                    cardIndex = i,
                    targetRotation = targetRotation,
                    cardData = cardData,
                    animationState = animationState,
                    onStepChange = onStepChange,
                    onAnimationComplete = onAnimationComplete
                )
            }
        }
    }
}

private fun getSwipeDirection(
    horizontal: Float,
    vertical: Float,
): SwipeDirection = when {
    abs(horizontal) > abs(vertical) && abs(horizontal) > 100f -> SwipeDirection.Horizontal
    abs(vertical) > abs(horizontal) && abs(vertical) > 100f -> SwipeDirection.Vertical
    else -> SwipeDirection.Unknown
}

fun reorderCards(cards: List<CardData>): List<CardData> {
    return cards.drop(1) + cards.first()
}

enum class SwipeDirection {
    Horizontal,
    Vertical,
    Unknown
}

data class CardSwapAnimationState(
    val isAnimating: Boolean = false,
    val animationStep: Int = 0,
)
