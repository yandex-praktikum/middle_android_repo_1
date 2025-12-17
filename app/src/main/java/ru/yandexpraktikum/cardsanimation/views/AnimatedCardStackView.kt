package ru.yandexpraktikum.cardsanimation.views

import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.FrameLayout
import ru.yandexpraktikum.cardsanimation.dp
import ru.yandexpraktikum.cardsanimation.model.CardData
import kotlin.math.absoluteValue

class AnimatedCardStackView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var cardsData = emptyList<CardData>()
    private val cards = mutableListOf<AnimatedCardView>()
    private var isRotated = false

    fun setCardsData(newCardDataList: List<CardData>) {
        cardsData = newCardDataList
        cards.clear()
        removeAllViews()
        cardsData.forEachIndexed { index, cardData ->
            val cardView = AnimatedCardView(context).apply {
                setCardData(cardData)
                setStackPosition(index)
            }
            cards.add(cardView)
            addView(cardView)
        }
        isRotated = false
    }

    private fun updateCardPositions() = cards.forEachIndexed { index, cardView ->
        val cardWidth = 100f.dp(context)
        val cardHeight = 160f.dp(context)
        cardView.x = (width / 2f) - (cardWidth / 2f)
        cardView.y = (height / 2f) - (cardHeight / 2f)
        cardView.pivotX = cardWidth / 2f
        cardView.pivotY = cardHeight
        cardView.animateToRotation(
            calculateRotation(index)
        )
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (changed) {
            updateCardPositions()
        }
    }

    private var offsetX = 0f
    private var offsetY = 0f
    private val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
        override fun onScroll(e1: MotionEvent?, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
            offsetX -= distanceX
            offsetY -= distanceY
            return true
        }
    })

    override fun onTouchEvent(event: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(event)
        if (event.action == MotionEvent.ACTION_UP) {
            if (offsetX.absoluteValue < offsetY.absoluteValue) {
                isRotated = offsetY < 0
                updateCardPositions()
            } else if (offsetX.absoluteValue != offsetY.absoluteValue) {
                startCardSwapAnimation(cards.first())
            }
            offsetX = 0f
            offsetY = 0f
        }
        return true
    }

    private var isAnimating = false
    private var animationStep = 0
    private fun startCardSwapAnimation(bottomCard: AnimatedCardView) {
        if (isAnimating) return
        isAnimating = true
        animationStep = 1
        bottomCard.moveCardRight {
            animationStep = 2
            bottomCard.bringCardToFront()
            bottomCard.moveCardToTop {
                animationStep = 3
                reorderCardsData()
                animateAllCardsToFinalPositions()
            }
        }
    }

    private fun reorderCardsData() {
        cardsData = cardsData.drop(1) + cardsData.first()
        cards.add(cards.removeAt(0))
        cards.forEachIndexed { index, card ->
            card.setCardData(cardsData[index])
        }
    }

    private fun animateAllCardsToFinalPositions() {
        var completedAnimations = 0
        cards.forEachIndexed { index, cardView ->
            val finalRotation = calculateRotation(index)
            cardView.setStackPosition(index)
            cardView.moveToFinalPosition(finalRotation, index) {
                completedAnimations++
                if (completedAnimations == cards.size) {
                    isAnimating = false
                    animationStep = 0
                }
            }
        }
    }

    private fun calculateRotation(cardIndex: Int) =
        if (isRotated) {
            val angleStep = if (cards.size > 1) 180f / (cards.size - 1) else 0f
            90f - (cardIndex * angleStep)
        } else {
            val angleStep = if (cards.size > 1) 45f / (cards.size - 1) else 0f
            22.5f - (cardIndex * angleStep)
        }

    private fun AnimatedCardView.bringCardToFront() {
        this.bringToFront()
        val maxElevation = (4 + cards.size + 20).toFloat().dp(context)
        this.cardView.cardElevation = maxElevation
    }
}