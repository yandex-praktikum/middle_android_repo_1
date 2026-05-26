package ru.yandexpraktikum.cardsanimation.utils

import android.util.Log
import kotlin.math.abs

internal enum class SwipeDirection {
    SWIPE_UP,
    SWIPE_DOWN,
    SWIPE_LEFT,
    SWIPE_RIGHT,
    UNRECOGNIZED;
}

internal fun handleDragEnd(verticalDragOffset: Float, horizontalDragOffset: Float): SwipeDirection {
    return when {
        abs(verticalDragOffset) > abs(horizontalDragOffset) && verticalDragOffset < 0 -> {
            Log.d("ComposeGestures", "Вертикальный свайп вниз")
            SwipeDirection.SWIPE_DOWN
        }
        abs(verticalDragOffset) > abs(horizontalDragOffset) && verticalDragOffset > 0 -> {
            Log.d("ComposeGestures", "Вертикальный свайп вверх")
            SwipeDirection.SWIPE_UP
        }
        abs(horizontalDragOffset) > abs(verticalDragOffset) && horizontalDragOffset < 0 -> {
            Log.d("ComposeGestures", "Горизонтальный свайп вправо")
            SwipeDirection.SWIPE_RIGHT
        }
        abs(horizontalDragOffset) > abs(verticalDragOffset) && horizontalDragOffset > 0 -> {
            Log.d("ComposeGestures", "Горизонтальный свайп влево")
            SwipeDirection.SWIPE_LEFT
        }
        else -> SwipeDirection.UNRECOGNIZED
    }
}