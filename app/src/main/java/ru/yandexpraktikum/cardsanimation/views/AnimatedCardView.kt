package ru.yandexpraktikum.cardsanimation.views

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.cardview.widget.CardView
import ru.yandexpraktikum.cardsanimation.R
import ru.yandexpraktikum.cardsanimation.model.CardData
import kotlin.math.cos
import kotlin.math.sin

class AnimatedCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    val cardView: CardView
    private val cardImageView: ImageView

    init {
        LayoutInflater.from(context).inflate(R.layout.card_view, this, true)

        cardView = this.getChildAt(0) as CardView
        cardImageView = findViewById(R.id.cardImage)

        pivotX = width / 2f
        pivotY = height.toFloat()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        pivotX = w / 2f
        pivotY = h.toFloat()
    }

    fun setCardData(cardData: CardData) {
        cardImageView.setImageResource(cardData.imageResId)
    }

    fun setStackPosition(index: Int) {
        cardView.cardElevation = (4 + index * 1).toFloat() * resources.displayMetrics.density
    }

    fun animateToRotation(targetRotation: Float) {
        ObjectAnimator.ofFloat(this, "rotation", rotation, targetRotation).apply {
            duration = ANIMATION_DURATION_IN_MILLIS
            start()
        }
    }

    fun moveCardRight(onComplete: () -> Unit) {
        val moveDistance = MOVE_DISTANCE_IN_DP * resources.displayMetrics.density
        val currentRotationRad = Math.toRadians(rotation.toDouble())

        val deltaX = moveDistance * cos(currentRotationRad).toFloat()
        val deltaY = moveDistance * sin(currentRotationRad).toFloat()

        val currentX = x
        val currentY = y

        val animatorX = ObjectAnimator.ofFloat(this, "x", currentX, currentX + deltaX)
        val animatorY = ObjectAnimator.ofFloat(this, "y", currentY, currentY + deltaY)

        val animatorSet = android.animation.AnimatorSet().apply {
            playTogether(animatorX, animatorY)
            duration = ANIMATION_DURATION_IN_MILLIS
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    onComplete.invoke()
                }
            })
        }

        animatorSet.start()
    }

    fun moveCardToTop(onComplete: () -> Unit) {
        val parent = parent as? FrameLayout ?: return
        val cardWidth = CARD_WIDTH_IN_DP * resources.displayMetrics.density
        val cardHeight = CARD_HEIGHT_IN_DP * resources.displayMetrics.density
        val centerX = parent.width / 2f - cardWidth / 2f
        val centerY = parent.height / 2f - cardHeight / 2f

        val animatorX = ObjectAnimator.ofFloat(this, "x", x, centerX)
        val animatorY = ObjectAnimator.ofFloat(this, "y", y, centerY)

        val animatorSet = android.animation.AnimatorSet().apply {
            playTogether(animatorX, animatorY)
            duration = ANIMATION_DURATION_IN_MILLIS
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    onComplete.invoke()
                }
            })
        }

        animatorSet.start()
    }

    fun adjustToFinalPosition(finalRotation: Float, finalZOrder: Int, onComplete: () -> Unit) {
        ObjectAnimator.ofFloat(this, "rotation", rotation, finalRotation).apply {
            duration = ANIMATION_DURATION_IN_MILLIS
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    setStackPosition(finalZOrder)
                    onComplete.invoke()
                }
            })
            start()
        }
    }

    private companion object {
        const val MOVE_DISTANCE_IN_DP = 50
        const val ANIMATION_DURATION_IN_MILLIS = 300L
        const val CARD_WIDTH_IN_DP = 100f
        const val CARD_HEIGHT_IN_DP = 160f
    }
}