package ru.yandexpraktikum.cardsanimation.compose

import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.unit.dp
import ru.yandexpraktikum.cardsanimation.model.CardData
import kotlin.math.absoluteValue

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
    var dragOffset by remember { mutableStateOf(Offset(0f, 0f)) }

    // TODO: [Задание 2] Добавьте обработку жестов
    // Подсказка: Используйте Modifier.pointerInput() с методом detectDragGestures()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        Log.d("AnimatedCardStack", "dragAmountOffset = $dragOffset")
                        if (dragOffset.y.absoluteValue > dragOffset.x.absoluteValue) {
                            //vertical swipe
                            isRotated = dragOffset.y > 0
                            dragOffset = Offset(0f, 0f)
                        } else {
                            //horizontal swipe
                        }
                    },
                    onDrag = { change, dragAmount ->
//                    Log.d("AnimatedCardStack", "change = ${change.position}")
                        Log.d("AnimatedCardStack", "dragAmount = $dragAmount")

                        dragOffset =
                            dragOffset.copy(
                                x = dragOffset.x + dragAmount.x,
                                y = dragOffset.y + dragAmount.y
                            )

                    }
                )

            },
        contentAlignment = Alignment.Center
    ) {
        cards.forEachIndexed { i, cardData ->
            key(cardData.imageResId) {
                val targetRotation = calculateCardRotation(i, cardCount, isRotated)

                val animatedRotation by animateFloatAsState(
                    targetRotation,
                    animationSpec = tween(durationMillis = 2000)
                )


                AnimatedCard(
                    cardIndex = i,
                    targetRotation = animatedRotation,
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