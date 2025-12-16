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
import kotlin.math.*

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

    var rotated by remember { mutableStateOf(false) }
    var nowCards by remember { mutableStateOf(cards) }
    var verticalDragOffset by remember { mutableFloatStateOf(0f) }
    var horizontalDragOffset by remember { mutableFloatStateOf(0f) }
    var animateState by remember { mutableStateOf(Animation()) }

    val minSwipeDistance = 100f

    Box(
        modifier = Modifier.pointerInput(Unit) {
            detectDragGestures(
                onDragStart = {
                    verticalDragOffset = 0f
                    horizontalDragOffset = 0f
                },
                onDragEnd = {
                    if (animateState.isAnimating) return@detectDragGestures

                    val absY = abs(verticalDragOffset)
                    val absX = abs(horizontalDragOffset)

                    if (absY > absX) {
                        when {
                            verticalDragOffset < -minSwipeDistance -> rotated = true
                            verticalDragOffset >  minSwipeDistance -> rotated = false
                        }
                    } else {
                        if (absX > minSwipeDistance) {
                            animateState = animateState.copy(
                                isAnimating = true,
                                animationStep = 1
                            )
                        }
                    }
                },
                onDrag = { _, dragAmount ->
                    verticalDragOffset += dragAmount.y
                    horizontalDragOffset += dragAmount.x
                }
            )
        },
        contentAlignment = Alignment.Center
    ) {
        nowCards.forEachIndexed { i, cardData ->
            key(cardData.imageResId) {

                val targetRotation = calculateCardRotation(
                    i, cards.size, rotated
                )

                AnimatedCard(
                    cardIndex = i,
                    targetRotation = targetRotation,
                    cardData = cardData,
                    animationState = animateState,
                    onAnimationStepComplete = { step ->
                        if (step == 3) {
                            nowCards = reorderCards(nowCards)
                            animateState = Animation() // сброс
                        } else {
                            animateState = animateState.copy(
                                isAnimating = true,
                                animationStep = step
                            )
                        }
                    }
                )
            }
        }
    }
}


data class Animation(
    val isAnimating: Boolean = false,
    val animationStep: Int = 0
)

fun reorderCards(cards: List<CardData>): List<CardData> {
    return cards.drop(1) + cards.first()
}