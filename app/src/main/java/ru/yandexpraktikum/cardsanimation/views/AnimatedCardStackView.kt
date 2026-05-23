package ru.yandexpraktikum.cardsanimation.views

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.FrameLayout
import ru.yandexpraktikum.cardsanimation.model.CardData
import kotlin.math.abs

class AnimatedCardStackView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var cardDataList: List<CardData> = emptyList()
    private val cards = mutableListOf<AnimatedCardView>()
    private var isRotated = false

    fun setCards(newCardDataList: List<CardData>) {
        cardDataList = newCardDataList
        setupCards()
    }

    private fun setupCards() {
        clearCards()
        cardDataList.forEachIndexed { index, cardData ->
            val cardView = AnimatedCardView(context).apply {
                setCardData(cardData)
                setStackPosition(index)
            }
            cards.add(cardView)
            addView(cardView)
        }
        // Возврат в исходное положение
        isRotated = false
        updateCardPositions()
    }

    private fun clearCards() {
        cards.clear()
        removeAllViews()
    }

    private fun updateCardPositions() {
        val cardCount = cards.size

        cards.forEachIndexed { index, cardView ->
            // Расчёт расположения карт в исходной позиции
            val baseRotation = if (cardCount > 1) {
                val angleStep = 45f / (cardCount - 1)
                22.5f - (index * angleStep)
            } else {
                0f
            }

            // Расчёт финальной позиции (для эффекта раскрытой колоды карт)
            val targetRotation = if (isRotated) {
                val angleStep = if (cardCount > 1) 180f / (cardCount - 1) else 0f
                90f - (index * angleStep)
            } else {
                baseRotation
            }

            val cardWidth = 100f * resources.displayMetrics.density
            val cardHeight = 160f * resources.displayMetrics.density
            val sharedX = width / 2f - cardWidth / 2f
            val sharedY = height / 2f - cardHeight / 2f

            cardView.x = sharedX
            cardView.y = sharedY

            cardView.pivotX = cardWidth / 2f
            cardView.pivotY = cardHeight

            // TODO: [Задание 1] Замените на метод, который анимирует движение карты
            cardView.animateToRotation(targetRotation)
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (changed) {
            updateCardPositions()
        }
    }

    private fun startCardSwapAnimation(bottomCard: AnimatedCardView) {
        // TODO: [Задание 5] Добавьте анимацию перетасовки карт
        // На данном этапе просто быстро двигаем нижнюю карту наверх
        cardDataList = reorderCards(cardDataList)
        setupCards()
    }

    // Простая функция перестановки карт
    fun reorderCards(cards: List<CardData>): List<CardData> {
        return cards.drop(1) + cards.first()
    }
    // TODO: [Задание 2] Добавьте обработку жестов
    // Подсказка: Используйте GestureDetector с методом onFling для обработки свайпов
    private val gestureDetector =
        GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            var verticalDragOffset = 0f
            var horizontalDragOffset = 0f

            override fun onScroll(
                e1: MotionEvent?,
                e2: MotionEvent,
                distanceX: Float,
                distanceY: Float
            ): Boolean {
                verticalDragOffset = distanceY
                horizontalDragOffset = distanceX
                return super.onScroll(e1, e2, distanceX, distanceY)
            }

            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                Log.d("ComposeGestures", when {
                    abs(verticalDragOffset) > abs(horizontalDragOffset) && verticalDragOffset < 0 -> {
                        isRotated = false
                        updateCardPositions()
                        "Вертикальный свайп вниз"
                    }
                    abs(verticalDragOffset) > abs(horizontalDragOffset) && verticalDragOffset > 0 -> {
                        isRotated = true
                        updateCardPositions()
                        "Вертикальный свайп вверх"
                    }
                    abs(horizontalDragOffset) > abs(verticalDragOffset) && horizontalDragOffset < 0 -> "Горизонтальный свайп вправо"
                    abs(horizontalDragOffset) > abs(verticalDragOffset) && horizontalDragOffset > 0 -> "Горизонтальный свайп влево"
                    else -> "Непонятный жест"
                })
                return super.onFling(e1, e2, velocityX, velocityY)
            }
        })

    // Нужно также переопределить onTouchEvent
    override fun onTouchEvent(event: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(event)
        return true
    }

    // TODO: [Задание 3] Добавьте обработку вертикальных свайпов (вверх/вниз)

    // TODO: [Задание 4] Добавьте обработку горизонтальных свайпов (влево/вправо)
}