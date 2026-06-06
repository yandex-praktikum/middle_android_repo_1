package ru.yandexpraktikum.cardsanimation.views

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.FrameLayout
import ru.yandexpraktikum.cardsanimation.model.CardData
import java.lang.Math.abs


val TAG = "test"
class AnimatedCardStackView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var cardDataList: List<CardData> = emptyList()
    private val cards = mutableListOf<AnimatedCardView>()
    private var isRotated = false
    private var isAnimating = false
    private var animationStep = 0

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
//            cardView.rotation = targetRotation
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
        if (isAnimating) return

        isAnimating = true
        animationStep = 1

        bottomCard.moveCardRight {
            animationStep = 2
            bringCardToFront(bottomCard)
            bottomCard.moveCardToTop {
                animationStep = 3
                reorderCardsData()
                animateAllCardsToFinalPositions()
            }
        }
    }

    private fun reorderCardsData() {
        val reorderedCards = cardDataList.drop(1) + cardDataList.first()
        cardDataList = reorderedCards

        val bottomCardView = cards.removeAt(0)
        cards.add(bottomCardView)

        cards.forEachIndexed { index, cardView ->
            cardView.setCardData(cardDataList[index])
        }
    }

    private fun animateAllCardsToFinalPositions() {
        var completedAnimations = 0
        val totalAnimations = cards.size

        cards.forEachIndexed { index, cardView ->
            val finalRotation = calculateFinalRotation(index)

            cardView.adjustToFinalPosition(finalRotation, index) {
                completedAnimations++
                if (completedAnimations == totalAnimations) {
                    finalizeCardPositions()
                }
            }
        }
    }

    private fun calculateFinalRotation(cardIndex: Int): Float {
        val cardCount = cards.size
        return if (isRotated) {
            val angleStep = if (cardCount > 1) 180f / (cardCount - 1) else 0f
            90f - (cardIndex * angleStep)
        } else {
            val angleStep = if (cardCount > 1) 45f / (cardCount - 1) else 0f
            22.5f - (cardIndex * angleStep)
        }
    }

    private fun finalizeCardPositions() {
        cards.forEachIndexed { index, card ->
            card.setStackPosition(index)
            val correctRotation = calculateFinalRotation(index)
            card.rotation = correctRotation
        }

        isAnimating = false
        animationStep = 0
    }

    private fun bringCardToFront(card: AnimatedCardView) {
        card.bringToFront()
        val maxElevation = (4 + cards.size + 20).toFloat() * resources.displayMetrics.density
        card.cardView.cardElevation = maxElevation
    }

    // Простая функция перестановки карт
    fun reorderCards(cards: List<CardData>): List<CardData> {
        return cards.drop(1) + cards.first()
    }
    // TODO: [Задание 2] Добавьте обработку жестов
    // Подсказка: Используйте GestureDetector с методом onFling для обработки свайпов
    private val gestureDetector =
        GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {

            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {

                // TODO: Обработка в заданиях 3 и 4

                handleFlingGesture(
                    velocityX = velocityX,
                    velocityY = velocityY
                )

                return true
            }
        })

    private fun updateFanState(isRotated: Boolean) {
        this.isRotated = isRotated
    }

    private fun handleFlingGesture(
        velocityX: Float,
        velocityY: Float
    ) {
        when {
            isHorizontalSwipe(velocityX, velocityY) -> {
                handleHorizontalSwipe()
            }

            isVerticalSwipe(velocityX, velocityY) -> {
                handleVerticalSwipe(
                    verticalDragDistance = velocityY,
                    onFanStateChange = ::updateFanState
                )
            }
        }

        updateCardPositions()
    }

    private fun isHorizontalSwipe(
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        return kotlin.math.abs(velocityX) > kotlin.math.abs(velocityY)
    }

    private fun isVerticalSwipe(
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        return kotlin.math.abs(velocityY) > kotlin.math.abs(velocityX)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(event)
        return true
    }

    // TODO: [Задание 3] Добавьте обработку вертикальных свайпов (вверх/вниз)
    fun handleVerticalSwipe(
        verticalDragDistance: Float,
        onFanStateChange: (Boolean) -> Unit
    ) {
        when {
            verticalDragDistance < 0 -> {
                onFanStateChange(true)
            }
            verticalDragDistance > 0 -> {
                onFanStateChange(false)
            }
        }
    }
    // TODO: [Задание 4] Добавьте обработку горизонтальных свайпов (влево/вправо)

    fun handleHorizontalSwipe() {
        val bottomCard = cards.firstOrNull() ?: return
        startCardSwapAnimation(bottomCard)
    }
}
