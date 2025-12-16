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

    private var items: List<CardData> = emptyList()
    private val cardViews = mutableListOf<AnimatedCardView>()

    private var isExpanded = false
    private var isBusy = false

    private var dragX = 0f
    private var dragY = 0f

    private val swipeThreshold = 100f

    init {
        setOnClickListener {
            isExpanded = !isExpanded
            updateStack()
        }
    }

    fun setCards(data: List<CardData>) {
        items = data
        rebuildStack()
    }

    private fun rebuildStack() {
        cardViews.clear()
        removeAllViews()

        items.forEachIndexed { index, item ->
            val v = AnimatedCardView(context).apply {
                setImage(item)
                setStackLevel(index)
            }
            cardViews.add(v)
            addView(v)
        }

        isExpanded = false
        updateStack()
    }

    private fun updateStack() {
        val count = cardViews.size
        val cardW = 100f * resources.displayMetrics.density
        val cardH = 160f * resources.displayMetrics.density

        val centerX = width / 2f - cardW / 2f
        val centerY = height / 2f - cardH / 2f

        cardViews.forEachIndexed { index, view ->
            view.x = centerX
            view.y = centerY
            view.pivotX = cardW / 2f
            view.pivotY = cardH

            val angle = computeAngle(index, count, isExpanded)
            view.rotateTo(angle)
        }
    }

    private fun computeAngle(i: Int, total: Int, expanded: Boolean): Float {
        if (total <= 1) return 0f

        return if (expanded) {
            val step = 180f / (total - 1)
            90f - (i * step)
        } else {
            val step = 45f / (total - 1)
            22.5f - (i * step)
        }
    }

    override fun onLayout(c: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(c, l, t, r, b)
        if (c) updateStack()
    }

    private fun animateCardSwap(bottom: AnimatedCardView) {
        if (isBusy) return
        isBusy = true

        bottom.shiftRight {
            bringToFrontAndRaise(bottom)
            bottom.centerInParent {
                rotateData()
                animateStackReset()
            }
        }
    }

    private fun rotateData() {
        items = items.drop(1) + items.first()

        val first = cardViews.removeAt(0)
        cardViews.add(first)

        cardViews.forEachIndexed { idx, v ->
            v.setImage(items[idx])
        }
    }

    private fun animateStackReset() {
        var finished = 0
        val total = cardViews.size

        cardViews.forEachIndexed { index, view ->
            val angle = computeAngle(index, total, isExpanded)

            view.snapToPosition(angle, index) {
                finished++
                if (finished == total) {
                    finalizePositions()
                }
            }
        }
    }

    private fun finalizePositions() {
        cardViews.forEachIndexed { i, v ->
            v.setStackLevel(i)
            v.rotation = computeAngle(i, cardViews.size, isExpanded)
        }
        isBusy = false
    }

    private fun bringToFrontAndRaise(v: AnimatedCardView) {
        v.bringToFront()
        val elev = (4 + cardViews.size + 20).toFloat() * resources.displayMetrics.density
        v.cardView.cardElevation = elev
    }

    private val detector =
        GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {

            override fun onScroll(e1: MotionEvent?, e2: MotionEvent, dx: Float, dy: Float): Boolean {
                if (isBusy) return false
                dragX -= dx
                dragY -= dy
                return true
            }

            override fun onFling(e1: MotionEvent?, e2: MotionEvent, vx: Float, vy: Float): Boolean {
                if (isBusy) return false

                if (abs(vy) > abs(vx)) {
                    isExpanded = vy < 0
                    updateStack()
                } else {
                    cardViews.firstOrNull()?.let { animateCardSwap(it) }
                }
                return true
            }
        })

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                dragX = 0f
                dragY = 0f
            }

            MotionEvent.ACTION_UP -> {
                if (abs(dragY) > abs(dragX)) {
                    if (dragY < -swipeThreshold) {
                        isExpanded = true
                        updateStack()
                    } else if (dragY > swipeThreshold) {
                        isExpanded = false
                        updateStack()
                    }
                } else {
                    if (abs(dragX) > swipeThreshold) {
                        cardViews.firstOrNull()?.let { animateCardSwap(it) }
                    }
                }
            }
        }

        detector.onTouchEvent(event)
        return true
    }
}
