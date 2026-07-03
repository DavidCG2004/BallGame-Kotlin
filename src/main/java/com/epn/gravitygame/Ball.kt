package com.epn.gravitygame

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import kotlin.math.max
import kotlin.math.min

class Ball(
    var position: Vector2 = Vector2(250f, 350f),
    private var radius: Float = DEFAULT_RADIUS
) {
    private var ballColor: Int = Color.rgb(249, 115, 22)

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ballColor
    }

    private val shinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(190, 255, 255, 255)
    }

    /** true cuando la última llamada a update() chocó contra algún borde de la pantalla */
    var hitBorder = false
        private set

    fun radius(): Float = radius

    fun currentColor(): Int = ballColor

    fun setColor(color: Int) {
        ballColor = color
        paint.color = color
    }

    fun setRadius(newRadius: Float) {
        radius = newRadius
    }

    fun update(sensorX: Float, sensorY: Float, width: Int, height: Int) {
        position.x += sensorX * SPEED
        position.y += sensorY * SPEED

        val clampedX = max(radius, min(width - radius, position.x))
        val clampedY = max(radius, min(height - radius, position.y))

        // Si el valor cambió al recortarlo, es porque la bolita tocó un borde
        hitBorder = clampedX != position.x || clampedY != position.y

        position.x = clampedX
        position.y = clampedY
    }

    fun draw(canvas: Canvas) {
        canvas.drawCircle(position.x, position.y, radius, paint)
        canvas.drawCircle(position.x - radius / 3, position.y - radius / 3, radius / 4, shinePaint)
    }

    companion object {
        private const val SPEED = 7.5f
        const val DEFAULT_RADIUS = 42f
    }
}