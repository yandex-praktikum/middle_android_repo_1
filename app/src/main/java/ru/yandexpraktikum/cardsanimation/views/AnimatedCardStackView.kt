package ru.yandexpraktikum.cardsanimation.views

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.FrameLayout
import ru.yandexpraktikum.cardsanimation.Helper
import ru.yandexpraktikum.cardsanimation.model.AnimationState
import ru.yandexpraktikum.cardsanimation.model.CardData
import ru.yandexpraktikum.cardsanimation.model.OffsetState

class AnimatedCardStackView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var cardDataList: List<CardData> = emptyList()
    private val cards = mutableListOf<AnimatedCardView>()
    private var isRotated = false
    private val offsetState = OffsetState()
    private var animationState = AnimationState()


    // [Задание 2] обработчик жестов
    private val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent): Boolean {
            return true
        }

        override fun onScroll(e1: MotionEvent?, e2: MotionEvent, offsetX: Float, offsetY: Float): Boolean {
            // отслеживаем движение пальца, пока он касается экрана
            offsetState.changeOffset(offsetX, offsetY)
            return super.onScroll(e1, e2, offsetX, offsetY)
        }

        override fun onFling(
            e1: MotionEvent?,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            Log.i("AnimatedCardStackView", "onFling")
            // обрабатываем свайпы
            offsetState.handleDragEnd().let { mode ->
                if (mode < 0) {
                    // [Задание 3] раскрываем/закрываем веер
                    startFanOutCards()
                    invalidate()
                } else if (mode > 0) {
                    // [Задание 4] смена первой карты
                    // если у нас нет элементов в списке - нечего перерисовывать
                    cards.firstOrNull()?.let {
                        startCardSwapAnimation(it)
                        invalidate()
                    }
                }
                offsetState.clearState()
            }
            return super.onFling(e1, e2, velocityX, velocityY)
        }
    })

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
            // Расчёт финальной позиции
            val targetRotation = Helper.calculateCardRotation(index, cardCount, isRotated)

            val (cardWidth, cardHeight) = this.getCardSize()
            val sharedX = width / 2f - cardWidth / 2f
            val sharedY = height / 2f - cardHeight / 2f

            cardView.x = sharedX
            cardView.y = sharedY

            cardView.pivotX = cardWidth / 2f
            cardView.pivotY = cardHeight

            // [Задание 1] движение карты
            cardView.animateToRotation(targetRotation, 1_000)
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        updateCardPositions()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        Log.i("AnimatedCardStackView", "onTouchEvent $event")
        parent?.requestDisallowInterceptTouchEvent(true)
        if (gestureDetector.onTouchEvent(event)) {
            return true
        }
        return super.onTouchEvent(event)
    }

    fun changeState(currStep: Int) {
        animationState = if (currStep >= cards.size - 1)
            AnimationState()
        else
            animationState.copy(isAnimating = true, step = currStep+1)
    }

    fun changeState(currStep: Int, newAnimatedCardId: Int?) {
        animationState = if (currStep >= cards.size - 1)
            AnimationState()
        else
            AnimationState(isAnimating = true, step = currStep+1, currentAnimatingCard = newAnimatedCardId)
    }

    private fun startFanOutCards() {
        Log.d("AnimatedCardStack", "vertical swipe ${offsetState.vDragOffset}")
        isRotated = offsetState.vDragOffset > 0f
        updateCardPositions()
    }

    private fun startCardSwapAnimation(bottomCard: AnimatedCardView) {
        if (animationState.isAnimating) return
        if (cardDataList.size < 2) return

        Log.d("AnimatedCardStack", "horizontal swipe ${offsetState.hDragOffset}")
        changeState(0, 0)

        // [Задание 5] Добавьте анимацию перетасовки карт
        bottomCard.moveCardRight {
            changeState(1)

            bottomCard.moveCardToTop(countAnimCardElevation()) {
                changeState(2, cards.size-1)

                // меняем порядок
                cardDataList = reorderCards(cardDataList)
                cards.apply {
                    add(removeAt(0))
                }

                // анимируем
                animateAllCardsToFinalPositions()
            }
        }
    }

    private fun animateAllCardsToFinalPositions() {
        var completedAnimations = 0
        cards.forEachIndexed { index, cardView ->
            cardView.adjustToFinalPosition(
                Helper.calculateCardRotation(index, cardDataList.size, isRotated),
                index
            ) {
                completedAnimations++
                if (completedAnimations == cards.size) {
                    stopSwapAnimation()
                }
            }
        }
    }

    private fun stopSwapAnimation() {
        // финальный штрих - выставляем elevation
        cards.forEachIndexed { index, cardView ->
            cardView.translationZ = index.toFloat()
            cardView.setStackPosition(index)
        }
        // меняем шаг, по сути, заканчиваем анимацию
        changeState(3)
    }

    private fun countAnimCardElevation() =
        (4 + cards.size + 20).toFloat() * resources.displayMetrics.density

    // Простая функция перестановки карт
    private fun reorderCards(cards: List<CardData>): List<CardData> {
        return cards.drop(1) + cards.first()
    }

}