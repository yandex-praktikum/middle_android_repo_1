package ru.yandexpraktikum.cardsanimation.views

import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.cardview.widget.CardView
import java.lang.Math.toRadians
import kotlin.math.cos
import kotlin.math.sin

class AnimatedCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    companion object {
        private const val DEFAULT_ANIMATION_DURATION = 300L
        private const val CARD_WIDTH_DP = 100f
        private const val CARD_HEIGHT_DP = 160f
        private const val MOVE_DISTANCE_DP = 50f
        private const val BASE_ELEVATION = 4f
        private const val ELEVATION_STEP = 1f
    }

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
        cardView.cardElevation = (BASE_ELEVATION + index * ELEVATION_STEP) * resources.displayMetrics.density
    }

    fun animateToRotation(targetRotation: Float, duration: Long = DEFAULT_ANIMATION_DURATION) {
        ObjectAnimator.ofFloat(this, "rotation", rotation, targetRotation).apply {
            this.duration = duration
            start()
        }
    }

    fun moveCardRight(onComplete: (() -> Unit)? = null) {
        val moveDistance = MOVE_DISTANCE_DP * resources.displayMetrics.density
        val currentRotationRad = toRadians(rotation.toDouble())

        val deltaX = moveDistance * cos(currentRotationRad).toFloat()
        val deltaY = moveDistance * sin(currentRotationRad).toFloat()

        val currentX = x
        val currentY = y

        val animatorX = ObjectAnimator.ofFloat(this, "x", currentX, currentX + deltaX)
        val animatorY = ObjectAnimator.ofFloat(this, "y", currentY, currentY + deltaY)

        val animatorSet = android.animation.AnimatorSet().apply {
            playTogether(animatorX, animatorY)
            duration = DEFAULT_ANIMATION_DURATION
            addListener(object : android.animation.AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: android.animation.Animator) {
                    onComplete?.invoke()
                }
            })
        }

        animatorSet.start()
    }

    fun moveCardToTop(onComplete: (() -> Unit)? = null) {
        val parent = parent as? FrameLayout ?: return
        val cardWidth = CARD_WIDTH_DP * resources.displayMetrics.density
        val cardHeight = CARD_HEIGHT_DP * resources.displayMetrics.density
        val centerX = parent.width / 2f - cardWidth / 2f
        val centerY = parent.height / 2f - cardHeight / 2f

        val animatorX = ObjectAnimator.ofFloat(this, "x", x, centerX)
        val animatorY = ObjectAnimator.ofFloat(this, "y", y, centerY)

        val animatorSet = android.animation.AnimatorSet().apply {
            playTogether(animatorX, animatorY)
            duration = DEFAULT_ANIMATION_DURATION
            addListener(object : android.animation.AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: android.animation.Animator) {
                    onComplete?.invoke()
                }
            })
        }

        animatorSet.start()
    }

    fun adjustToFinalPosition(finalRotation: Float, finalZOrder: Int, onComplete: (() -> Unit)? = null) {
        ObjectAnimator.ofFloat(this, "rotation", rotation, finalRotation).apply {
            duration = DEFAULT_ANIMATION_DURATION
            addListener(object : android.animation.AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: android.animation.Animator) {
                    setStackPosition(finalZOrder)
                    onComplete?.invoke()
                }
            })
            start()
        }
    }
}