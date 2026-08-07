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
    private var verticalDragOffset = 0f
    private var horizontalDragOffset = 0f
    private val threshold = 100f
    private var isAnimating = false
    private var animationStep = 0

    init {
        setOnClickListener {
            if (!isAnimating) {
                setRotatedState(!isRotated)
            }
        }
    }

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
        isRotated = false
        updateCardPositions()
    }

    private fun clearCards() {
        cards.clear()
        removeAllViews()
    }

    private fun updateCardPositions() {
        cards.forEachIndexed { index, cardView ->
            val targetRotation = calculateFinalRotation(index)

            val cardWidth = 100f * resources.displayMetrics.density
            val cardHeight = 160f * resources.displayMetrics.density
            val sharedX = width / 2f - cardWidth / 2f
            val sharedY = height / 2f - cardHeight / 2f

            cardView.x = sharedX
            cardView.y = sharedY

            cardView.pivotX = cardWidth / 2f
            cardView.pivotY = cardHeight
            cardView.animateToRotation(targetRotation)
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (changed) {
            updateCardPositions()
        }
    }

    private fun startCardSwapAnimation(bottomCard: AnimatedCardView, isRight: Boolean) {
        if (isAnimating) return

        isAnimating = true
        animationStep = 1

        bottomCard.moveCardSide(isRight) {
            animationStep = 2
            bringCardToFront(bottomCard)
            bottomCard.moveCardToTop {
                animationStep = 3
                val movingCard = cards.removeAt(0)
                cards.add(movingCard)
                cardDataList = cardDataList.drop(1) + cardDataList.first()

                animateAllCardsToFinalPositions()
            }
        }
    }

    private fun bringCardToFront(card: AnimatedCardView) {
        card.bringToFront()
        val maxElevation = (10 + cards.size + 20).toFloat() * resources.displayMetrics.density
        card.cardView.cardElevation = maxElevation
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

    private val gestureDetector =
        GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onScroll(
                e1: MotionEvent?,
                e2: MotionEvent,
                distanceX: Float,
                distanceY: Float
            ): Boolean {
                if (isAnimating) return false
                val totalX = abs(distanceX)
                val totalY = abs(distanceY)

                if (totalY > totalX) {
                    verticalDragOffset -= distanceY
                } else {
                    horizontalDragOffset -= distanceX
                }
                return true
            }

            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                if (isAnimating) return false
                if (abs(velocityY) > abs(velocityX)) {
                    if (velocityY < -500) setRotatedState(true)
                    else if (velocityY > 500) setRotatedState(false)
                } else {
                    if (abs(velocityX) > 500) handleHorizontalSwipe(velocityX > 0)
                }
                return true
            }
        })

    private fun handleHorizontalSwipe(isRight: Boolean) {
        val bottomCard = cards.firstOrNull() ?: return
        startCardSwapAnimation(bottomCard, isRight)
    }

    private fun setRotatedState(rotated: Boolean) {
        if (isRotated != rotated) {
            isRotated = rotated
            updateCardPositions()
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val result = gestureDetector.onTouchEvent(event)
        if (event.action == MotionEvent.ACTION_UP) {
            handleDragEnd()
        }
        return result || super.onTouchEvent(event)
    }

    private fun handleDragEnd() {
        if (isAnimating) return
        if (abs(verticalDragOffset) > abs(horizontalDragOffset)) {
            if (verticalDragOffset < -threshold) setRotatedState(true)
            else if (verticalDragOffset > threshold) setRotatedState(false)
        } else {
            if (abs(horizontalDragOffset) > threshold)
                handleHorizontalSwipe(horizontalDragOffset > 0)
        }
        verticalDragOffset = 0f
        horizontalDragOffset = 0f
    }
}
