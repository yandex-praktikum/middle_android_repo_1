package ru.yandexpraktikum.cardsanimation.views

import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.util.Log
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
) : FrameLayout(context, attrs, defStyleAttr){

    val cardView: CardView
    private val cardImageView: ImageView

    private var rotationAnimation : ObjectAnimator? = null

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

    fun setElevationStackPosition(index: Int) {
        cardView.cardElevation = (4 + index * 1).toFloat() * resources.displayMetrics.density
    }

    fun animateToTargetRotation(targetRotation : Float, onComplete: (() -> Unit)? = null){


        Log.d("AnimatedCardStack ", "rotation = $rotation targetRotation = $targetRotation")
        rotationAnimation = ObjectAnimator.ofFloat(this,"rotation",targetRotation)

        rotationAnimation?.apply {
            duration = ROTATION_DURATION
            //addListener(animatorListener)
            addListener(object : android.animation.AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: android.animation.Animator) {
                    onComplete?.invoke()
                }
            })

         }?.start()

    }

    fun moveCardRight(onComplete: (() -> Unit)? = null) {
        val moveDistance = 50f * resources.displayMetrics.density
        val currentRotationRad = Math.toRadians(rotation.toDouble())

        val deltaX = moveDistance * cos(currentRotationRad).toFloat()
        val deltaY = moveDistance * sin(currentRotationRad).toFloat()

        val currentX = x
        val currentY = y

        val animatorX = ObjectAnimator.ofFloat(this, "x", currentX, currentX + deltaX)
        val animatorY = ObjectAnimator.ofFloat(this, "y", currentY, currentY + deltaY)

        val animatorSet = android.animation.AnimatorSet().apply {
            playTogether(animatorX, animatorY)
            duration = TRANSLATION_DURATION
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
        val cardWidth = CARD_WIDTH * resources.displayMetrics.density
        val cardHeight = CARD_HEIGHT * resources.displayMetrics.density
        val centerX = parent.width / 2f - cardWidth / 2f
        val centerY = parent.height / 2f - cardHeight / 2f

        val animatorX = ObjectAnimator.ofFloat(this, "x", x, centerX)
        val animatorY = ObjectAnimator.ofFloat(this, "y", y, centerY)

        val animatorSet = android.animation.AnimatorSet().apply {
            playTogether(animatorX, animatorY)
            duration = TRANSLATION_DURATION
            addListener(object : android.animation.AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: android.animation.Animator) {
                    onComplete?.invoke()
                }
            })
        }

        animatorSet.start()
    }






    // TODO: [Задание 1] Добавьте методы для анимации
    // Подсказка: используйте ObjectAnimator для плавной анимации

    // TODO: [Задание 1] Добавьте метод для анимации поворота карты (чтобы был плавный эффект раскрытия/закрытия колоды)
    // fun animateToRotation(targetRotation: Float, duration: Long = 300) { ... }

    // TODO: [Задание 5, шаг 1] Добавьте метод для анимации перетасовки карт (первым шагом нижняя карта двигается вправо)
    // fun moveCardRight(onComplete: (() -> Unit)? = null) { ... }

    // TODO: [Задание 5, шаг 2] Добавьте метод для анимации выдвижения нижней карты наверх
    // fun moveCardToTop(onComplete: (() -> Unit)? = null) { ... }

    // TODO: [Задание 5, шаг 3] Добавьте анимацию перемещения всей колоды карты в желаемую позицию
    // fun adjustToFinalPosition(finalRotation: Float, finalZOrder: Int, onComplete: (() -> Unit)? = null) { ... }

    companion object{
        const val CARD_HEIGHT = 160f
        const val CARD_WIDTH = 100f
        const val TRANSLATION_DURATION = 300L
        const val ROTATION_DURATION = 800L
    }
} 