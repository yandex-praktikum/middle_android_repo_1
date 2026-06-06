package ru.yandexpraktikum.cardsanimation.utils

fun handleVerticalSwipe(
    verticalDragDistance: Float,
    onFanStateChange: (Boolean) -> Unit
) {
    when {
        verticalDragDistance < 0 -> onFanStateChange(true)
        verticalDragDistance > 0 -> onFanStateChange(false)
    }
}

fun handleHorizontalSwipe(
    horizontalDragDistance: Float,
    threshold: Float = 120f,
    onCardsReorder: () -> Unit
) {

    when {
        horizontalDragDistance > threshold -> {
            onCardsReorder()
        }
        horizontalDragDistance < -threshold -> {
            onCardsReorder()
        }
    }
}