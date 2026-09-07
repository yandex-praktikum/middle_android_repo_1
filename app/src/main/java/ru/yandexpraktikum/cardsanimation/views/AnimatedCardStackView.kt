package ru.yandexpraktikum.cardsanimation.views

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.FrameLayout
import androidx.compose.ui.geometry.Offset
import ru.yandexpraktikum.cardsanimation.model.CardData
import kotlin.math.absoluteValue

class AnimatedCardStackView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var cardDataList: List<CardData> = emptyList()
    private val cards = mutableListOf<AnimatedCardView>()
    private var isRotated = false

    private var distanceOffset = Offset(0f, 0f)

    private var isAnimating = false

    private val gestureDetector: GestureDetector =
        GestureDetector(context, object : GestureDetector.OnGestureListener {
            override fun onDown(e: MotionEvent): Boolean {
                return true
            }

            override fun onShowPress(e: MotionEvent) {

            }

            override fun onSingleTapUp(e: MotionEvent): Boolean {
                return false
            }

            override fun onScroll(
                e1: MotionEvent?,
                e2: MotionEvent,
                distanceX: Float,
                distanceY: Float
            ): Boolean {
//            Log.d("AnimatedCardStackView", "onScroll e1 = $e1 ")
//            Log.d("AnimatedCardStackView", "onScroll e2 = $e2 ")
//            Log.d("AnimatedCardStackView", "onScroll distanceX = $distanceX ")
//            Log.d("AnimatedCardStackView", "onScroll distanceY = $distanceY")

                distanceOffset = Offset(distanceOffset.x + distanceX, distanceOffset.y + distanceY)

                return true
            }

            override fun onLongPress(e: MotionEvent) {

            }

            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                Log.d("AnimatedCardStackView", "onFling $e1 $e2 $velocityX $velocityY")
                handleDragOffset(distanceOffset)
                return true
            }

        })

    private fun handleDragOffset(offset: Offset) {
        if(isAnimating){
            return
        }
        if(offset.x == 0f && offset.y == 0f){
            return
        }
        if (offset.y.absoluteValue > offset.x.absoluteValue) {
            //verticalScroll
            Log.d("AnimatedCardStackView", "verticalSwipe")
             isRotated = offset.y < 0
            isAnimating = true
            animateAllCardsRotation{
                Log.d("AnimatedCardStackView", "animateAllCardsRotation complete")
                isAnimating = false
            }
        } else {
            //horizontalScroll
            Log.d("AnimatedCardStackView", "horizontalSwipe")
            isAnimating = true
            startCardSwapAnimation(cards.first()){
                Log.d("AnimatedCardStackView", "startCardSwapAnimation complete")
                isAnimating = false
            }

        }
        distanceOffset = Offset(0f, 0f)
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
                setElevationStackPosition(index)
            }
            cards.add(cardView)
            addView(cardView)
        }
        // Возврат в исходное положение
        isRotated = false
        updateCardPositions()
    }

    private fun updateCardOrder(){
        cardDataList = reorderCards(cardDataList)

        val bottomCardView = cards.removeAt(0)
        cards.add(bottomCardView)

        cardDataList.forEachIndexed { index, cardData ->

            cards[index].setCardData(cardData)
        }
    }

    private fun clearCards() {
        cards.clear()
        removeAllViews()
    }

    // Расчёт расположения карт в исходной позиции
    private fun getBaseRotation(cardCount: Int, index: Int): Float {
        return if (cardCount > 1) {
            val angleStep = 45f / (cardCount - 1)
            22.5f - (index * angleStep)
        } else {
            0f
        }
    }

    // Расчёт финальной позиции (для эффекта раскрытой колоды карт)
    private fun getTargetRotation(cardCount: Int, index: Int): Float {
        return if (isRotated) {
            val angleStep = if (cardCount > 1) 180f / (cardCount - 1) else 0f
            90f - (index * angleStep)
        } else {
            getBaseRotation(cardCount, index)
        }
    }

    private fun updateCardPositions() {
        val cardCount = cards.size

        cards.forEachIndexed { index, cardView ->

            // Расчёт финальной позиции (для эффекта раскрытой колоды карт)
            val targetRotation = getTargetRotation(cardCount, index)

            val cardWidth = AnimatedCardView.CARD_WIDTH * resources.displayMetrics.density
            val cardHeight = AnimatedCardView.CARD_HEIGHT * resources.displayMetrics.density
            val sharedX = width / 2f - cardWidth / 2f
            val sharedY = height / 2f - cardHeight / 2f

            cardView.x = sharedX
            cardView.y = sharedY

            cardView.pivotX = cardWidth / 2f
            cardView.pivotY = cardHeight

            // TODO: [Задание 1] Замените на метод, который анимирует движение карты
            cardView.rotation = targetRotation
        }
    }

    private fun animateAllCardsRotation(onComplete: (() -> Unit)? = null) {
        val cardCount = cards.size

        var completedCount = 0
        cards.forEachIndexed { index, cardView ->

            // Расчёт финальной позиции (для эффекта раскрытой колоды карт)
            val targetRotation = getTargetRotation(cardCount, index)

            cardView.animateToTargetRotation(targetRotation = targetRotation){
                completedCount++
                if(completedCount == cardCount){
                    onComplete?.invoke()
                }
            }

        }


    }

    private fun startCardSwapAnimation(bottomCard: AnimatedCardView, onComplete: (() -> Unit)? = null) {

        //animationStep = 1
        bottomCard.moveCardRight {
            //animationStep = 2
            bringCardToFront(bottomCard)
            bottomCard.moveCardToTop {
                //animationStep = 3
                updateCardOrder()
                animateAllCardsRotation{
                    Log.d("AnimatedCardStackView", "animateAllCardsRotation complete")
                    cards.forEachIndexed { index, cardData ->
                        cards[index].setElevationStackPosition(index)
                    }
                    onComplete?.invoke()
                }

            }
        }
    }

    private fun bringCardToFront(card: AnimatedCardView) {
        card.bringToFront()
        val maxElevation = (4 + cards.size + 20).toFloat() * resources.displayMetrics.density
        card.cardView.cardElevation = maxElevation
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (changed) {
            updateCardPositions()
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        //return super.onInterceptTouchEvent(ev)
        return true
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        // Log.d("AnimatedCardStackView", "event = $event")
        if (event == null) {
            return super.onTouchEvent(event)
        }
//        when(event.action){
//            MotionEvent.ACTION_DOWN -> Log.d("AnimatedCardStackView", "action = ACTION_DOWN")
//            MotionEvent.ACTION_UP -> Log.d("AnimatedCardStackView", "action = ACTION_UP")
//            MotionEvent.ACTION_MOVE -> Log.d("AnimatedCardStackView", "action = ACTION_MOVE")
//            MotionEvent.ACTION_CANCEL -> Log.d("AnimatedCardStackView", "action = ACTION_CANCEL")
//            else -> Log.d("AnimatedCardStackView", "action = ${event.action}")
//        }
//        Log.d("AnimatedCardStackView", "event = ${event.x}")
        if (event.action == MotionEvent.ACTION_UP) {
            Log.d("AnimatedCardStackView", "action = ACTION_UP")
            handleDragOffset(distanceOffset)
        }
        return gestureDetector.onTouchEvent(event) || super.onTouchEvent(event)
    }

    // Простая функция перестановки карт
    fun reorderCards(cards: List<CardData>): List<CardData> {
        return cards.drop(1) + cards.first()
    }
    // TODO: [Задание 2] Добавьте обработку жестов
    // Подсказка: Используйте GestureDetector с методом onFling для обработки свайпов

    // TODO: [Задание 3] Добавьте обработку вертикальных свайпов (вверх/вниз)

    // TODO: [Задание 4] Добавьте обработку горизонтальных свайпов (влево/вправо)
}