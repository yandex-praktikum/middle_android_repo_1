package ru.yandexpraktikum.cardsanimation.views

import android.content.Context
import android.util.AttributeSet
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

            //  [Задание 1] Замените на метод, который анимирует движение карты

            cardView.animateToRotation(targetRotation)


        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (changed) {
            updateCardPositions()
        }
    }

    private var isAnimating = false
    private var animationStep = 0
    private fun startCardSwapAnimation(bottomCard: AnimatedCardView) {
        // TODO: [Задание 5] Добавьте анимацию перетасовки карт
        // На данном этапе просто быстро двигаем нижнюю карту наверх
        if (isAnimating) return

        isAnimating = true
        animationStep = 1

        // TODO: Реализовать 3-шаговую анимацию в следующих этапах
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

    private fun handleHorizontalSwipe() {
        val bottomCard = cards.firstOrNull() ?: return
        startCardSwapAnimation(bottomCard)
    }

    private fun handleVerticalSwipe() {
        if (verticalDragOffset > 0) {

            isRotated = false
            updateCardPositions()
        } else {

            isRotated = true
            updateCardPositions()
        }
    }

    private fun bringCardToFront(card: AnimatedCardView) {
        card.bringToFront()
        val maxElevation = (4 + cards.size + 20).toFloat() * resources.displayMetrics.density
        card.cardView.cardElevation = maxElevation
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


    private val swipeThreshold = 100
    private val swipeVelocityThreshold = 100
    private var verticalDragOffset = 0f
    private var horizontalDragOffset = 0f

    //  [Задание 2] Добавьте обработку жестов
    // Подсказка: Используйте GestureDetector с методом onFling для обработки свайпов
    private val gestureDetector: GestureDetector =
        GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onDown(event: MotionEvent): Boolean {

                return true
            }

            override fun onScroll(
                e1: MotionEvent?,
                e2: MotionEvent,
                distanceX: Float,
                distanceY: Float
            ): Boolean {
                //Для накопления offset'ов
                // Отслеживаем движение
                verticalDragOffset = e2.y - e1!!.y
                horizontalDragOffset = e2.x - e1.x
                return super.onScroll(e1, e2, distanceX, distanceY)
            }

            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {

                if (abs(horizontalDragOffset) > abs(verticalDragOffset)) {

                    // Horizontal swipe
                    if (abs(horizontalDragOffset) > swipeThreshold && abs(velocityX) > swipeVelocityThreshold) {

                        handleHorizontalSwipe()

                        return true
                    }
                } else {

                    // Vertical swipe
                    if (abs(verticalDragOffset) > swipeThreshold && abs(velocityY) > swipeVelocityThreshold) {

                        handleVerticalSwipe()

                        return true
                    }
                }
                horizontalDragOffset = 0f
                verticalDragOffset = 0f

                return false
            }

        })

    override fun onTouchEvent(event: MotionEvent): Boolean {

        when (event.action) {
            MotionEvent.ACTION_UP -> {
                if (abs(horizontalDragOffset) > abs(verticalDragOffset)) {
                    // Horizontal swipe
                    if (abs(horizontalDragOffset) > swipeThreshold) {

                        handleHorizontalSwipe()

                    }
                } else {
                    // Vertical swipe
                    if (abs(verticalDragOffset) > swipeThreshold) {

                        handleVerticalSwipe()

                    }
                }
                horizontalDragOffset = 0f
                verticalDragOffset = 0f
            }
        }
        return gestureDetector.onTouchEvent(event) || super.onTouchEvent(event)
    }
}



