package ru.yandexpraktikum.cardsanimation.views

import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.FrameLayout
import androidx.compose.ui.geometry.Offset
import ru.yandexpraktikum.cardsanimation.model.CardData
import ru.yandexpraktikum.cardsanimation.model.CardSwapAnimationState
import kotlin.math.abs

class AnimatedCardStackView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var cardDataList: List<CardData> = emptyList()
    private val cards = mutableListOf<AnimatedCardView>()
    private var isRotated = false
        set(value) {
            if (field != value) {
                field = value
                updateCardPositions()
            }
        }

    private var dragOffset = Offset.Zero
    private var flingVelocity = Offset.Zero
    private var swapAnimationState = CardSwapAnimationState()

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
        if (swapAnimationState.isAnimating) return

        swapAnimationState = swapAnimationState.copy(isAnimating = true, animationStep = 1)

        bottomCard.moveCardRight {
            swapAnimationState = swapAnimationState.copy(animationStep = 2)
            bringCardToFront(bottomCard)
            bottomCard.moveCardToTop {
                swapAnimationState = swapAnimationState.copy(animationStep = 3)
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
        cards.forEachIndexed { index, cardView ->
            cardView.setStackPosition(index)
            val correctRotation = calculateFinalRotation(index)
            cardView.rotation = correctRotation
        }

        swapAnimationState = swapAnimationState.copy(isAnimating = false, animationStep = 0)
    }

    private fun bringCardToFront(card: AnimatedCardView) {
        card.bringToFront()
        val maxElevation = (4 + cards.size + 20).toFloat() * resources.displayMetrics.density
        card.cardView.cardElevation = maxElevation
    }

    private val gestureDetector =
        GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onScroll(
                e1: MotionEvent?,
                e2: MotionEvent,
                distanceX: Float,
                distanceY: Float
            ): Boolean {
                dragOffset += Offset(distanceX, distanceY)

                return true
            }

            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                flingVelocity = Offset(velocityX, velocityY)
                handleDragEnd()

                return true
            }
        })

    override fun onTouchEvent(event: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(event)

        if (event.action == MotionEvent.ACTION_UP) {
            handleDragEnd()
        }

        return true
    }

    private fun handleVerticalSwipe(offset: Float = 0f, velocity: Float = 0f) {
        isRotated = when {
            offset > 0 -> true
            offset < 0 -> false
            else -> when {
                velocity > 0 -> false
                velocity < 0 -> true
                else -> isRotated
            }
        }
    }

    private fun handleHorizontalSwipe() {
        val bottomCard = cards.firstOrNull() ?: return
        startCardSwapAnimation(bottomCard)
    }

    private fun handleDragEnd() {
        if (swapAnimationState.isAnimating) return

        val threshold = 100f
        val isVerticalOffsetDominant = abs(dragOffset.y) > abs(dragOffset.x)
        val isHorizontalOffsetDominant = abs(dragOffset.x) > abs(dragOffset.y)
        val isVerticalVelocityDominant = abs(flingVelocity.y) > abs(flingVelocity.x)
        val isHorizontalVelocityDominant = abs(flingVelocity.x) > abs(flingVelocity.y)

        when {
            isVerticalOffsetDominant && abs(dragOffset.y) > threshold -> {
                handleVerticalSwipe(offset = dragOffset.y)
            }

            isHorizontalOffsetDominant && abs(dragOffset.x) > threshold -> {
                handleHorizontalSwipe()
            }

            isVerticalVelocityDominant -> {
                handleVerticalSwipe(velocity = flingVelocity.y)
            }

            isHorizontalVelocityDominant -> {
                handleHorizontalSwipe()
            }
        }

        dragOffset = Offset.Zero
        flingVelocity = Offset.Zero
    }
}