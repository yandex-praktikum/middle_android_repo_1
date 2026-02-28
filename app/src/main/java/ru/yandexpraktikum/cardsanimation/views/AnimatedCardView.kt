package ru.yandexpraktikum.cardsanimation.views

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.animation.DecelerateInterpolator
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

    // TODO: [Задание 1] Добавьте методы для анимации
    // Подсказка: используйте ObjectAnimator для плавной анимации

    // TODO: [Задание 1] Добавьте метод для анимации поворота карты (чтобы был плавный эффект раскрытия/закрытия колоды)
     fun animateToRotation(targetRotation: Float, duration: Long = 600) {
        val animator = ObjectAnimator.ofFloat(
            this,
            View.ROTATION,
            targetRotation
        )

        animator.duration = duration
        animator.interpolator = DecelerateInterpolator()
        animator.start()
     }

    // TODO: [Задание 5, шаг 1] Добавьте метод для анимации перетасовки карт (первым шагом нижняя карта двигается вправо)
    fun moveCardRight(onComplete: (() -> Unit)? = null) {
        val moveDistance = 50f * resources.displayMetrics.density
        val currentRotationRad = Math.toRadians(rotation.toDouble())

        val deltaX = moveDistance * cos(currentRotationRad).toFloat()
        val deltaY = moveDistance * sin(currentRotationRad).toFloat()

        val animatorX = ObjectAnimator.ofFloat(this, "x", x, x + deltaX)
        val animatorY = ObjectAnimator.ofFloat(this, "y", y, y + deltaY)

        AnimatorSet().apply {
            playTogether(animatorX, animatorY)
            duration = 300
            interpolator = DecelerateInterpolator()
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    onComplete?.invoke()
                }
            })
            start()
        }
    }

    fun moveCardToTop(onComplete: (() -> Unit)? = null) {
        val parent = parent as? FrameLayout ?: return
        val cardWidth = 100f * resources.displayMetrics.density
        val cardHeight = 160f * resources.displayMetrics.density
        val centerX = parent.width / 2f - cardWidth / 2f
        val centerY = parent.height / 2f - cardHeight / 2f

        AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(this@AnimatedCardView, "x", x, centerX),
                ObjectAnimator.ofFloat(this@AnimatedCardView, "y", y, centerY)
            )
            duration = 300
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    onComplete?.invoke()
                }
            })
            start()
        }
    }

    fun adjustToFinalPosition(finalRotation: Float, finalZOrder: Int, onComplete: (() -> Unit)? = null) {
        ObjectAnimator.ofFloat(this, "rotation", rotation, finalRotation).apply {
            duration = 300
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    setStackPosition(finalZOrder)
                    onComplete?.invoke()
                }
            })
            start()
        }
    }}