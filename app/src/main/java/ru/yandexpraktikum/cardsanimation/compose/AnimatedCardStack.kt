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
    var endOffset by remember { mutableStateOf(Offset.Zero) }
    var startOffset by remember { mutableStateOf(Offset.Zero) }
    var cards by remember { mutableStateOf(cards) }

    // TODO: [Задание 2] Добавьте обработку жестов
    // Подсказка: Используйте Modifier.pointerInput() с методом detectDragGestures()

    Box(
        modifier = Modifier
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        startOffset = offset
                    },
                    onDragEnd = {
                        // TODO: Обработка жестов в заданиях 3 и 4
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
                            SwipeDirection.SWIPE_RIGHT -> {
                                cards = reorderCards(cards)
                            }
                            SwipeDirection.UNRECOGNIZED -> Unit
                        }

                        endOffset = Offset.Zero
                        startOffset = Offset.Zero
                    }
                ) { change, dragAmount ->
                    endOffset += dragAmount
                    // Определяем направление свайпа
                    change.consume()
                }
            },
        contentAlignment = Alignment.Center
    ) {
        cards.forEachIndexed { i, cardData ->
            key(cardData.imageResId) {
                val targetRotation = calculateCardRotation(i, cardCount, isRotated)

                AnimatedCard(
                    cardIndex = i,
                    targetRotation = targetRotation,
                    cardData = cardData
                    // TODO: [Задание 5] Здесь добавьте параметры анимации карты
                )
            }
        }
    }
}

// Простая функция перестановки карт
fun reorderCards(cards: List<CardData>): List<CardData> {
    return cards.drop(1) + cards.first()
}