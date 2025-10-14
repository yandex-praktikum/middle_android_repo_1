package ru.yandexpraktikum.cardsanimation.compose.enums

enum class AnimationStep(val value: Int) {
    IDLE(0),
    MOVE_OUT(1),
    MOVE_BACK(2),
    FINAL_ROTATION(3)
}