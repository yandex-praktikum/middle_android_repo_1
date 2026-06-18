package ru.yandexpraktikum.cardsanimation.compose

import android.util.Log
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
import ru.yandexpraktikum.cardsanimation.Helper
import ru.yandexpraktikum.cardsanimation.model.AnimationState
import ru.yandexpraktikum.cardsanimation.model.CardData
import ru.yandexpraktikum.cardsanimation.model.OffsetState


@Composable
fun AnimatedCardStack(cards: List<CardData>) {
    val cardCount = cards.size
    var cardsState by remember { mutableStateOf(cards) }

    var isRotated by remember { mutableStateOf(false) }
    var offsetState by remember { mutableStateOf(OffsetState()) }

    // [Задание 5] стейты для анимации свапа
    var animationState by remember { mutableStateOf(AnimationState()) }


    fun changeState(currStep: Int) {
        animationState = if (currStep >= cardCount - 1)
            AnimationState()
        else
            animationState.copy(isAnimating = true, step = currStep+1)
    }

    fun changeState(currStep: Int, newAnimatedCardId: Int?) {
        animationState = if (currStep >= cardCount - 1)
            AnimationState()
        else
            AnimationState(isAnimating = true, step = currStep+1, currentAnimatingCard = newAnimatedCardId)
    }

    // Простая функция перестановки карт
    fun reorderCards(cards: List<CardData>): List<CardData> {
        return cards.drop(1) + cards.first()
    }

    fun startFanOutCards() {
        Log.d("AnimatedCardStack", "vertical swipe ${offsetState.vDragOffset}")
        isRotated = offsetState.vDragOffset < 0f
    }

    fun startCardSwapAnimation() {
        if (animationState.isAnimating) return
        if(cardCount < 2) return

        Log.d("AnimatedCardStack", "horizontal swipe ${offsetState.hDragOffset}")
        changeState(0, 0)
    }


    Box(
        modifier = Modifier
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        if (!animationState.isAnimating) {
                            offsetState.handleDragEnd().let { mode ->
                                Log.i("AnimatedCardStack", "End of dragging $mode")
                                if (mode < 0) {
                                    // [Задание 3] раскрываем/закрываем веер
                                    startFanOutCards()
                                } else if (mode > 0) {
                                    // [Задание 4] смена первой карты
                                    startCardSwapAnimation()
                                }
                                offsetState.clearState()
                            }
                        }
                    }
                ) { change, offset ->
                    // [Задание 2] распознавание направления
                    offsetState.changeOffset(offset.x, offset.y)
                    change.consume()
                }
            },
        contentAlignment = Alignment.Center
    ) {
        cardsState.forEachIndexed { i, cardData ->
            key(cardData.imageResId) {
                AnimatedCard(
                    cardIndex = i,
                    cardData = cardData,
                    // [Задание 5] параметры для анимации карты
                    targetRotation = Helper.calculateCardRotation(i, cardCount, isRotated),
                    animationState = animationState,
                    onStepFinished = { currStep ->
                        when(currStep) {
                            1 -> changeState(currStep)
                            2 -> {
                                cardsState = reorderCards(cardsState)
                                changeState(currStep, cards.size-1)
                            }
                            3 -> {
                                changeState(currStep, 0)
                            }
                        }
                    }
                )
            }
        }
    }
}
