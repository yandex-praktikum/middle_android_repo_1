package ru.yandexpraktikum.cardsanimation.model

enum class AnimationStep(val step: Int) {
    INITIAL(0),
    ROTATION(1),
    TRANSLATION(2),
    FINAL(3)
}