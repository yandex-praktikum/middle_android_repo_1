package ru.yandexpraktikum.cardsanimation.compose


enum class AnimationStep {
    ALL_ROTATE_TO_TARGET,
    FIRST_MOVE_TO_RIGHT,
    SECOND_MOVE_TO_CENTER,
    LAST_ROTATE_TO_TARGET
}
data class CardSwapAnimationState(
    val isAnimating: Boolean = false,
    val animationStep: AnimationStep? = null
)
