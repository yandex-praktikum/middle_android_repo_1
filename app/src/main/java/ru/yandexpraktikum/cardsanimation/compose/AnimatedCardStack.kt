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
import kotlin.math.absoluteValue

fun calculateCardRotation(cardIndex: Int, cardCount: Int, isRotated: Boolean): Float {
    if (cardCount <= 1) return 0f
    if (isRotated) {
        val angleStep = 180f / (cardCount - 1)
        return 90f - (cardIndex * angleStep)
    } else {
        val angleStep = 45f / (cardCount - 1)
        return 22.5f - (cardIndex * angleStep)
    }
}

data class CardSwapAnimationState(
    val isAnimating: Boolean = false,
    val animationStep: Int = 0
)

@Composable
fun AnimatedCardStack(initCards: List<CardData>) {
    var isRotated by remember { mutableStateOf(false) }
    var offsetX by remember { mutableFloatStateOf(0.0f) }
    var offsetY by remember { mutableFloatStateOf(0.0f) }
    var cards by remember { mutableStateOf(initCards) }
    var animationState by remember { mutableStateOf(CardSwapAnimationState()) }
    Box(
        modifier = Modifier
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        if (animationState.isAnimating) return@detectDragGestures
                        if (offsetX.absoluteValue < offsetY.absoluteValue) {
                            isRotated = offsetY > 0
                        } else if (offsetX.absoluteValue != offsetY.absoluteValue) {
                            animationState = animationState.copy(isAnimating = true, animationStep = 1)
                        }
                        offsetX = 0.0f
                        offsetY = 0.0f
                    },
                    onDrag = { _, offset ->
                        offsetX -= offset.x
                        offsetY -= offset.y
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        cards.forEachIndexed { i, cardData ->
            key(cardData.imageResId) {
                AnimatedCard(
                    cardIndex = i,
                    targetRotation = calculateCardRotation(i, cards.size, isRotated),
                    finalRotation = calculateCardRotation(i, cards.size, isRotated),
                    cardData = cardData,
                    isAnimating = animationState.isAnimating,
                    animationStep = animationState.animationStep,
                    onAnimationStepComplete = { step ->
                        when (step) {
                            1 -> {
                                animationState = animationState.copy(animationStep = 2)
                            }
                            2 -> {
                                animationState = animationState.copy(animationStep = 3)
                                cards = reorderCards(cards)
                            }
                            3 -> {
                                animationState = animationState.copy(animationStep = 0, isAnimating = false)
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