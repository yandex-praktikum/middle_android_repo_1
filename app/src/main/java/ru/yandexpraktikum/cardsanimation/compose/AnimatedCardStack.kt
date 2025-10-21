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

fun calculateCardFinalRotation(
    cardIndex: Int,
    cardCount: Int,
    isRotated: Boolean
): Float {
    if (cardCount <= 1) return 0f

    return if (isRotated) {
        val angleStep = 180f / (cardCount - 1)
        45f - (cardIndex * angleStep)
    } else {
        val angleStep = 45f / (cardCount - 1)
        22.4f - (cardIndex * angleStep)
    }
}

@Composable
fun AnimatedCardStack(cards: List<CardData>) {
    val cardCount = cards.size
    var isRotated by remember { mutableStateOf(false) }
    var verticalDragOffset = 0f
    var horizontalDragOffset = 0f
    var currentCards by remember { mutableStateOf(cards) }
    var animationState by remember { mutableStateOf(CardSwapAnimationState()) }
    var isAnimating by remember { mutableStateOf(animationState.isAnimating) }

    var animationStep by remember { mutableStateOf(animationState.animationStep) }
    // TODO: [Задание 2] Добавьте обработку жестов
    // Подсказка: Используйте Modifier.pointerInput() с методом detectDragGestures()


    Box(
        modifier = Modifier.pointerInput(Unit) {
            detectDragGestures(

                onDragEnd = {
                    if (!animationState.isAnimating) {

                        val threshold = 100f
                        val isVerticalDominant = abs(verticalDragOffset) > abs(horizontalDragOffset)
                        val isHorizontalDominant =
                            abs(horizontalDragOffset) > abs(verticalDragOffset)

                        when {
                            isVerticalDominant && abs(verticalDragOffset) > threshold -> {

                                handleVerticalSwipe(
                                    verticalDragOffset,
                                    { newFanState ->
                                        isRotated = newFanState
                                    }
                                )
                            }

                            isHorizontalDominant && abs(horizontalDragOffset) > threshold -> {

                                handleHorizontalSwipe(onCardsReorder = {
                                    isAnimating = true
                                    animationStep = 1
                                })
                            }
                        }
                    }
                    verticalDragOffset = 0f
                    horizontalDragOffset = 0f
                }
            ) { _, dragAmount ->
                // Определяем направление свайпа
                verticalDragOffset += dragAmount.y
                horizontalDragOffset += dragAmount.x

            }
        },
        contentAlignment = Alignment.Center
    ) {
        currentCards.forEachIndexed { i, cardData ->
            key(cardData.imageResId) {
                val targetRotation = calculateCardRotation(i, cardCount, isRotated)

                val finalRotation = calculateCardFinalRotation(i, cardCount, isRotated)

                AnimatedCard(
                    cardIndex = i,
                    targetRotation = targetRotation,
                    cardData = cardData,
                    // TODO: [Задание 5] Здесь добавьте параметры анимации карты
                    isAnimating = isAnimating,
                    animationStep = animationStep,
                    onAnimationStepComplete = {
                        handleAnimationStepComplete(
                            step = animationStep,
                            cardIndex = i,
                            onStepChange = { newStep -> animationStep = newStep },
                            onAnimationComplete = {
                                currentCards = reorderCards(currentCards)
                                animationStep = 0
                                isAnimating = false
                            }
                        )
                    }, finalRotation = finalRotation

                )


            }


        }
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

// Простая функция перестановки карт
fun reorderCards(cards: List<CardData>): List<CardData> {
    return cards.drop(1) + cards.first()
}

fun handleVerticalSwipe(verticalDragDistance: Float, onFanStateChange: (Boolean) -> Unit) {
    val threshold = 5f
    var newFanState: Boolean = false
    when {
        verticalDragDistance < -threshold -> {
            newFanState = true //"Свайп снизу вверх!"
        }

        verticalDragDistance > threshold -> {
            newFanState = false //"Свайп сверху вниз!"
        }
    }
    onFanStateChange(newFanState)
}

fun handleHorizontalSwipe(onCardsReorder: () -> Unit) {
    onCardsReorder()
}








