package ru.yandexpraktikum.cardsanimation.views

import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.FrameLayout
import kotlin.math.abs

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
    private val gestureDetector = GestureDetector(context, CardGestureListener())
    private var flingMinVelocity = 0f
    private var lastAction = 0

    companion object {
        private const val FLING_MIN_VELOCITY_DP = 100f
        private const val CARD_WIDTH_DP = 100f
        private const val CARD_HEIGHT_DP = 160f
        private const val MAX_ELEVATION_EXTRA = 20f
        private const val BASE_ELEVATION = 4f
        private const val COLLAPSED_MAX_ANGLE = 22.5f
        private const val EXPANDED_MAX_ANGLE = 90f
    }

    init {
        flingMinVelocity = FLING_MIN_VELOCITY_DP * resources.displayMetrics.density

        isClickable = true
        isFocusable = true
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
            val cardWidth = CARD_WIDTH_DP * resources.displayMetrics.density
            val cardHeight = CARD_HEIGHT_DP * resources.displayMetrics.density

            cardView.x =  width / 2f - cardWidth / 2f
            cardView.y = height / 2f - cardHeight / 2f

            cardView.pivotX = cardWidth / 2f
            cardView.pivotY = cardHeight

            cardView.animateToRotation(calculateFinalRotation(index))
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        if (changed) {
            updateCardPositions()
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val result = if (!isAnimating) {
            gestureDetector.onTouchEvent(event) || super.onTouchEvent(event)
        } else {
            true
        }

        lastAction = event.action

        if (event.action == MotionEvent.ACTION_UP && !isAnimating) {
            performClick()
        }

        return result
    }

    override fun performClick(): Boolean {
        super.performClick()

        return false
    }
    private inner class CardGestureListener : GestureDetector.SimpleOnGestureListener() {
        override fun onFling(
            e1: MotionEvent?,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            if (isAnimating) return false

            return when {
                isVerticalFling(e1, e2, velocityY) -> {
                    toggleDeckMode()
                    true
                }
                isHorizontalFling(e1, e2, velocityX) -> {
                    handleHorizontalSwipe()
                    true
                }
                else -> false
            }
        }

        private fun isVerticalFling(e1: MotionEvent?, e2: MotionEvent, velocityY: Float): Boolean {
            if (abs(velocityY) <= flingMinVelocity) return false
            val diffY = e2.y - (e1?.y ?: 0f)
            val diffX = e2.x - (e1?.x ?: 0f)

            return abs(diffY) > abs(diffX)
        }

        private fun isHorizontalFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float): Boolean {
            if (abs(velocityX) <= flingMinVelocity) return false

            val diffY = e2.y - (e1?.y ?: 0f)
            val diffX = e2.x - (e1?.x ?: 0f)

            return abs(diffX) > abs(diffY)
        }
    }

    private fun toggleDeckMode() {
        isRotated = !isRotated
        updateCardPositions()
    }

    private fun handleHorizontalSwipe() {
        val bottomCard = cards.firstOrNull() ?: return
        startCardSwapAnimation(bottomCard)
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

    private fun bringCardToFront(card: AnimatedCardView) {
        card.bringToFront()
        card.cardView.cardElevation = (BASE_ELEVATION + cards.size + MAX_ELEVATION_EXTRA) * resources.displayMetrics.density
    }

    private fun reorderCardsData() {
        cardDataList = cardDataList.drop(1) + cardDataList.first()

        cards.add(cards.removeAt(0))

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

    private fun finalizeCardPositions() {
        cards.forEachIndexed { index, card ->
            card.setStackPosition(index)
            card.rotation = calculateFinalRotation(index)
        }

        isAnimating = false
        animationStep = 0
    }

    private fun calculateFinalRotation(cardIndex: Int): Float {
        val cardCount = cards.size
        if (cardCount <= 1) return 0f

        return if (isRotated) {
            val angleStep = 180f / (cardCount - 1)
            EXPANDED_MAX_ANGLE - (cardIndex * angleStep)
        } else {
            val angleStep = 45f / (cardCount - 1)
            COLLAPSED_MAX_ANGLE - (cardIndex * angleStep)
        }
    }
}