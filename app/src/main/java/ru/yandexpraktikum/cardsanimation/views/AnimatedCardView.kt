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

class AnimatedCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr){

    val cardView: CardView
    private val cardImageView: ImageView

    private var rotationAnimation : ObjectAnimator? = null
    private var animationInProgress : Boolean = false

    private val animatorListener = object : Animator.AnimatorListener{
        override fun onAnimationStart(animation: Animator) {
            animationInProgress = true
        }

        override fun onAnimationEnd(animation: Animator) {
            Log.d("AnimatedCardStackView","onAnimationEnd" )
            rotationAnimation?.removeListener(this)
            animationInProgress = false

        }

        override fun onAnimationCancel(animation: Animator) {
            Log.d("AnimatedCardStackView","onAnimationCancel" )
            rotationAnimation?.removeListener(this)
            animationInProgress = false
        }

        override fun onAnimationRepeat(animation: Animator) {

        }

    }


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

    fun animateToTargetRotation(targetRotation : Float){
        if(animationInProgress){
            return
        }

        //rotationAnimation?.cancel()
        rotationAnimation = ObjectAnimator.ofFloat(this,"rotation",targetRotation)

        rotationAnimation?.apply {
            duration = 1000
            addListener(animatorListener)

         }?.start()

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
} 