package ru.yandexpraktikum.cardsanimation.views

import android.animation.ObjectAnimator
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
                detectDistanceOffset(distanceOffset)
                return true
            }

        })

    private fun detectDistanceOffset(offset: Offset) {
        if(offset.x == 0f && offset.y == 0f){
            return
        }
        if (offset.y.absoluteValue > offset.x.absoluteValue) {
            //verticalScroll
            if (offset.y > 0) {
                //up
                //Log.d("AnimatedCardStackView", "UP")
                isRotated = false
                animateCards()
            } else {
                //down
                //Log.d("AnimatedCardStackView", "DOWN")
                isRotated = true
                animateCards()
            }
        } else {
            //horizontalScroll
            if (offset.x > 0) {
                //left
                Log.d("AnimatedCardStackView", "LEFT")
            } else {
                //right
                Log.d("AnimatedCardStackView", "RIGHT")
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

            val cardWidth = 100f * resources.displayMetrics.density
            val cardHeight = 160f * resources.displayMetrics.density
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

    private fun animateCards() {
        val cardCount = cards.size

        cards.forEachIndexed { index, cardView ->

            // Расчёт финальной позиции (для эффекта раскрытой колоды карт)
            val targetRotation = getTargetRotation(cardCount, index)

            cardView.animateToTargetRotation(targetRotation = targetRotation)
        }
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
            detectDistanceOffset(distanceOffset)
        }
        return gestureDetector.onTouchEvent(event) || super.onTouchEvent(event)
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

    // TODO: [Задание 3] Добавьте обработку вертикальных свайпов (вверх/вниз)

    // TODO: [Задание 4] Добавьте обработку горизонтальных свайпов (влево/вправо)
}