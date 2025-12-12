package ru.yandexpraktikum.cardsanimation.views

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.cardview.widget.CardView
import ru.yandexpraktikum.cardsanimation.R
import ru.yandexpraktikum.cardsanimation.model.CardData

class AnimatedCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    val cardView: CardView
    private val imageView: ImageView

    init {
        LayoutInflater.from(context).inflate(R.layout.card_view, this, true)

        cardView = getChildAt(0) as CardView
        imageView = findViewById(R.id.cardImage)

        updatePivot()
    }

    override fun onSizeChanged(newWidth: Int, newHeight: Int, oldWidth: Int, oldHeight: Int) {
        super.onSizeChanged(newWidth, newHeight, oldWidth, oldHeight)
        updatePivot()
    }

    private fun updatePivot() {
        pivotX = width / 2f
        pivotY = height.toFloat()
    }

    fun setImage(data: CardData) {
        imageView.setImageResource(data.imageResId)
    }

    fun setStackLevel(level: Int) {
        val elevation = (4 + level) * resources.displayMetrics.density
        cardView.cardElevation = elevation
    }

    fun rotateTo(angle: Float, duration: Long = 300L) {
        ObjectAnimator.ofFloat(this, "rotation", angle)
            .apply {
                this.duration = duration
                start()
            }
    }

    fun shiftRight(onComplete: (() -> Unit)? = null) {
        val distance = 50f * resources.displayMetrics.density
        val rotationRad = Math.toRadians(rotation.toDouble())

        val deltaX = distance * Math.cos(rotationRad).toFloat()
        val deltaY = distance * Math.sin(rotationRad).toFloat()

        val animatorX = ObjectAnimator.ofFloat(this, "x", x, x + deltaX)
        val animatorY = ObjectAnimator.ofFloat(this, "y", y, y + deltaY)

        AnimatorSet().apply {
            playTogether(animatorX, animatorY)
            this.duration = 300
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    onComplete?.invoke()
                }
            })
            start()
        }
    }

    fun centerInParent(onComplete: (() -> Unit)? = null) {
        val parentContainer = parent as? FrameLayout ?: return

        val cardWidth = 100f * resources.displayMetrics.density
        val cardHeight = 160f * resources.displayMetrics.density
        val targetX = parentContainer.width / 2f - cardWidth / 2f
        val targetY = parentContainer.height / 2f - cardHeight / 2f

        val animatorX = ObjectAnimator.ofFloat(this, "x", x, targetX)
        val animatorY = ObjectAnimator.ofFloat(this, "y", y, targetY)

        AnimatorSet().apply {
            playTogether(animatorX, animatorY)
            this.duration = 300
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    onComplete?.invoke()
                }
            })
            start()
        }
    }

    fun snapToPosition(finalAngle: Float, stackLevel: Int, onComplete: (() -> Unit)? = null) {
        ObjectAnimator.ofFloat(this, "rotation", rotation, finalAngle).apply {
            duration = 300
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    setStackLevel(stackLevel)
                    onComplete?.invoke()
                }
            })
            start()
        }
    }
}