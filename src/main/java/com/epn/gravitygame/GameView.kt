package com.epn.gravitygame

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.MotionEvent
import android.view.View
import kotlin.math.roundToInt
import kotlin.random.Random

class GameView(context: Context) : View(context) {

    private val ball = Ball()
    private val target = Target()
    private val obstacles = mutableListOf<Obstacle>()

    private var sensorX = 0f
    private var sensorY = 0f
    private var score = 0
    private var lives = 3
    private var started = false
    private var gameOver = false

    // --- Personalización de la bolita ---
    private val ballColors = listOf(
        Color.rgb(249, 115, 22), // naranja
        Color.rgb(59, 130, 246), // azul
        Color.rgb(168, 85, 247), // morado
        Color.rgb(236, 72, 153)  // rosa
    )
    private val ballColorNames = listOf("Naranja", "Azul", "Morado", "Rosa")
    private val ballSizes = listOf(26f, 42f, 58f)
    private val ballSizeLabels = listOf("Chica", "Media", "Grande")

    private var selectedColorIndex = 0
    private var selectedSizeIndex = 1

    private var colorButtonRects = listOf<RectF>()
    private var sizeButtonRects = listOf<RectF>()
    private var startButtonRect: RectF? = null

    // --- Feedback visual al chocar con un borde ---
    private var borderFlashFrames = 0

    // --- Cuenta regresiva ---
    private var countdownValue = 0
    private var countdownActive = false
    private val countdownHandler = Handler(Looper.getMainLooper())
    private val countdownSteps = listOf("3", "2", "1", "\u00a1YA!")

    private val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

    // --- Paint objects ---
    private val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(15, 23, 42)
        textSize = 48f
        isFakeBoldText = true
    }

    private val titleAccentPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(124, 58, 237)
        textSize = 52f
        isFakeBoldText = true
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(51, 65, 85)
        textSize = 32f
    }

    private val smallPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(100, 116, 139)
        textSize = 24f
    }

    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(51, 65, 85)
        textSize = 22f
        textAlign = Paint.Align.CENTER
    }

    private val panelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(235, 255, 255, 255)
    }

    private val accentPanelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(124, 58, 237)
    }

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(226, 232, 240)
        strokeWidth = 3f
    }

    private val startButtonPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(34, 197, 94)
    }

    private val startButtonTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 30f
        isFakeBoldText = true
        textAlign = Paint.Align.CENTER
    }

    private val selectionRingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(15, 23, 42)
        style = Paint.Style.STROKE
        strokeWidth = 5f
    }

    private val sizeButtonPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(226, 232, 240)
    }

    private val sizeButtonSelectedPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(203, 213, 225)
    }

    private val borderFlashPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(239, 68, 68)
        style = Paint.Style.STROKE
        strokeWidth = 14f
    }

    private val countdownOverlayPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(180, 15, 23, 42)
    }

    private val countdownTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 140f
        isFakeBoldText = true
        textAlign = Paint.Align.CENTER
    }

    private val countdownSubTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(200, 255, 255, 255)
        textSize = 36f
        textAlign = Paint.Align.CENTER
    }

    private val gameOverPanelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(220, 15, 23, 42)
    }

    private val gameOverTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 46f
        isFakeBoldText = true
        textAlign = Paint.Align.CENTER
    }

    private val gameOverScorePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(250, 204, 21)
        textSize = 56f
        isFakeBoldText = true
        textAlign = Paint.Align.CENTER
    }

    private val gameOverTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(180, 255, 255, 255)
        textSize = 28f
        textAlign = Paint.Align.CENTER
    }

    private val restartButtonPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(124, 58, 237)
    }

    private val ballPreviewPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 4f
        color = Color.rgb(203, 213, 225)
    }

    private val sectionTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(124, 58, 237)
        textSize = 24f
        isFakeBoldText = true
    }

    fun updateSensorValues(x: Float, y: Float) {
        if (!started || gameOver || countdownActive) return
        sensorX = x
        sensorY = y
        updateGame()
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        resetGame()
    }

    private fun resetGame() {
        score = 0
        lives = 3
        gameOver = false
        started = false
        countdownActive = false
        countdownValue = 0
        borderFlashFrames = 0
        ball.position.set(width / 2f, height / 2f)
        ball.setColor(ballColors[selectedColorIndex])
        ball.setRadius(ballSizes[selectedSizeIndex])
        target.relocate(width, height)
        createObstacles()
        invalidate()
    }

    private fun createObstacles() {
        obstacles.clear()
        if (width == 0 || height == 0) return

        val margin = 40f
        val minObstacleWidth = 100f
        val maxObstacleWidth = 200f
        val minObstacleHeight = 30f
        val maxObstacleHeight = 60f
        val obstacleCount = Random.nextInt(3, 6)
        val centerZone = RectF(width * 0.35f, height * 0.35f, width * 0.65f, height * 0.65f)
        val placed = mutableListOf<RectF>()

        repeat(obstacleCount) {
            var attempts = 0
            while (attempts < 20) {
                val w = Random.nextFloat() * (maxObstacleWidth - minObstacleWidth) + minObstacleWidth
                val h = Random.nextFloat() * (maxObstacleHeight - minObstacleHeight) + minObstacleHeight
                val left = Random.nextFloat() * (width - w - margin * 2) + margin
                val top = Random.nextFloat() * (height * 0.55f - h - margin * 2) + height * 0.20f + margin
                val rect = RectF(left, top, left + w, top + h)

                if (RectF.intersects(rect, centerZone)) {
                    attempts++
                    continue
                }

                var overlaps = false
                for (existing in placed) {
                    if (RectF.intersects(rect, existing)) {
                        overlaps = true
                        break
                    }
                }

                if (!overlaps) {
                    placed.add(rect)
                    obstacles.add(Obstacle(rect))
                    break
                }
                attempts++
            }
        }

        if (obstacles.isEmpty()) {
            obstacles.add(Obstacle(RectF(width * 0.10f, height * 0.25f, width * 0.40f, height * 0.30f)))
            obstacles.add(Obstacle(RectF(width * 0.60f, height * 0.45f, width * 0.90f, height * 0.50f)))
            obstacles.add(Obstacle(RectF(width * 0.15f, height * 0.70f, width * 0.55f, height * 0.75f)))
        }
    }

    private fun startCountdown() {
        countdownActive = true
        countdownValue = 0
        showNextCountdownStep()
    }

    private fun showNextCountdownStep() {
        if (countdownValue >= countdownSteps.size) {
            countdownActive = false
            started = true
            invalidate()
            return
        }
        invalidate()
        countdownHandler.postDelayed({
            countdownValue++
            showNextCountdownStep()
        }, 800)
    }

    private fun updateGame() {
        ball.update(sensorX, sensorY, width, height)

        if (ball.hitBorder) {
            borderFlashFrames = 8
            vibrate(15)
        } else if (borderFlashFrames > 0) {
            borderFlashFrames--
        }

        if (Collision.circleWithCircle(ball.position, ball.radius(), target.position, target.radius())) {
            score += 10
            target.relocate(width, height)
            vibrate(35)
        }

        obstacles.forEach { obstacle ->
            if (Collision.circleWithRect(ball.position, ball.radius(), obstacle.rect)) {
                lives--
                ball.position.set(width / 2f, height / 2f)
                vibrate(120)
                if (lives <= 0) {
                    gameOver = true
                }
                return@forEach
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawBackground(canvas)
        drawHeader(canvas)
        target.draw(canvas)
        obstacles.forEach { it.draw(canvas) }
        ball.draw(canvas)

        if (borderFlashFrames > 0) {
            canvas.drawRect(6f, 6f, width - 6f, height - 6f, borderFlashPaint)
        }

        if (!started && !countdownActive) drawStartOverlay(canvas)
        if (countdownActive) drawCountdown(canvas)
        if (gameOver) drawGameOver(canvas)
    }

    private fun drawBackground(canvas: Canvas) {
        canvas.drawColor(Color.rgb(248, 250, 252))
        val step = 80
        var x = 0
        while (x < width) {
            canvas.drawLine(x.toFloat(), 0f, x.toFloat(), height.toFloat(), linePaint)
            x += step
        }
        var y = 0
        while (y < height) {
            canvas.drawLine(0f, y.toFloat(), width.toFloat(), y.toFloat(), linePaint)
            y += step
        }
    }

    private fun drawHeader(canvas: Canvas) {
        canvas.drawRoundRect(24f, 24f, width - 24f, 138f, 28f, 28f, panelPaint)
        canvas.drawText("Gravity Ball", 48f, 72f, titleAccentPaint)
        canvas.drawText("Puntaje: $score    Vidas: $lives", 48f, 116f, textPaint)
        canvas.drawText("X: ${sensorX.roundToInt()}  Y: ${sensorY.roundToInt()}", width - 230f, 116f, smallPaint)
    }

    private fun drawStartOverlay(canvas: Canvas) {
        val box = RectF(60f, height * 0.18f, width - 60f, height * 0.84f)
        canvas.drawRoundRect(box, 36f, 36f, panelPaint)

        // --- Barra de título decorativa ---
        canvas.drawRoundRect(box.left, box.top, box.right, box.top + 80f, 36f, 36f, accentPanelPaint)
        canvas.drawRect(box.left, box.top + 40f, box.right, box.top + 80f, accentPanelPaint)
        titlePaint.color = Color.WHITE
        canvas.drawText("Gravity Ball Kotlin", box.centerX(), box.top + 50f, titlePaint.apply { textAlign = Paint.Align.CENTER })
        titlePaint.color = Color.rgb(15, 23, 42)
        titlePaint.textAlign = Paint.Align.LEFT

        // --- Instrucciones ---
        textPaint.color = Color.rgb(100, 116, 139)
        canvas.drawText("Inclina el celular para mover la bolita.", box.centerX(), box.top + 120f, textPaint.apply { textAlign = Paint.Align.CENTER })
        canvas.drawText("Atrapa los objetivos verdes y evita los obst\u00e1culos.", box.centerX(), box.top + 152f, textPaint.apply { textAlign = Paint.Align.CENTER })
        textPaint.color = Color.rgb(51, 65, 85)
        textPaint.textAlign = Paint.Align.LEFT

        val previewY = box.top + 230f
        val previewCenterX = box.centerX()

        // --- Vista previa de la bolita ---
        canvas.drawCircle(previewCenterX, previewY, 52f, ballPreviewPaint)
        val previewBallPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = ballColors[selectedColorIndex]
        }
        canvas.drawCircle(previewCenterX, previewY, 44f, previewBallPaint)
        canvas.drawText("Vista previa", previewCenterX, previewY + 70f, labelPaint)

        // --- Selector de color ---
        val colorSectionY = previewY + 100f
        canvas.drawText("COLOR", box.left + 50f, colorSectionY, sectionTitlePaint)
        val swatchRadius = 36f
        val swatchGap = 100f
        val swatchY = colorSectionY + 50f
        val newColorRects = mutableListOf<RectF>()
        val totalSwatchWidth = ballColors.size * swatchGap
        val swatchStartX = box.centerX() - totalSwatchWidth / 2f + swatchGap / 2f

        ballColors.forEachIndexed { index, c ->
            val cx = swatchStartX + index * swatchGap
            val swatchPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = c }
            canvas.drawCircle(cx, swatchY, swatchRadius, swatchPaint)
            if (index == selectedColorIndex) {
                canvas.drawCircle(cx, swatchY, swatchRadius + 8f, selectionRingPaint)
            }
            val namePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(100, 116, 139)
                textSize = 16f
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText(ballColorNames[index], cx, swatchY + swatchRadius + 25f, namePaint)
            newColorRects.add(RectF(cx - swatchRadius - 10f, swatchY - swatchRadius - 10f, cx + swatchRadius + 10f, swatchY + swatchRadius + 10f))
        }
        colorButtonRects = newColorRects

        // --- Selector de tamaño ---
        val sizeSectionY = swatchY + 80f
        canvas.drawText("TAMA\u00d1O", box.left + 50f, sizeSectionY, sectionTitlePaint)
        val buttonWidth = 140f
        val buttonHeight = 56f
        val buttonGap = 20f
        val buttonsTotalWidth = ballSizeLabels.size * buttonWidth + (ballSizeLabels.size - 1) * buttonGap
        val buttonsStartX = box.centerX() - buttonsTotalWidth / 2f
        val buttonsTop = sizeSectionY + 40f
        val newSizeRects = mutableListOf<RectF>()

        ballSizeLabels.forEachIndexed { index, label ->
            val left = buttonsStartX + index * (buttonWidth + buttonGap)
            val rect = RectF(left, buttonsTop, left + buttonWidth, buttonsTop + buttonHeight)
            val paint = if (index == selectedSizeIndex) sizeButtonSelectedPaint else sizeButtonPaint
            canvas.drawRoundRect(rect, 16f, 16f, paint)
            if (index == selectedSizeIndex) {
                canvas.drawRoundRect(rect, 16f, 16f, selectionRingPaint)
            }
            canvas.drawText(label, rect.centerX(), rect.centerY() + 8f, labelPaint)
            newSizeRects.add(rect)
        }
        sizeButtonRects = newSizeRects

        // --- Botón de inicio ---
        val startRect = RectF(box.left + 60f, box.bottom - 90f, box.right - 60f, box.bottom - 30f)
        canvas.drawRoundRect(startRect, 24f, 24f, startButtonPaint)
        canvas.drawText("Comenzar", startRect.centerX(), startRect.centerY() + 10f, startButtonTextPaint)
        startButtonRect = startRect
    }

    private fun drawCountdown(canvas: Canvas) {
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), countdownOverlayPaint)

        val text = countdownSteps.getOrElse(countdownValue) { "" }

        if (countdownValue < countdownSteps.size - 1) {
            countdownTextPaint.textSize = 160f
            canvas.drawText(text, width / 2f, height / 2f + 50f, countdownTextPaint)
            canvas.drawText("Prep\u00e1rate...", width / 2f, height / 2f - 100f, countdownSubTextPaint)
        } else {
            countdownTextPaint.textSize = 120f
            canvas.drawText(text, width / 2f, height / 2f + 40f, countdownTextPaint)
        }
    }

    private fun drawGameOver(canvas: Canvas) {
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), countdownOverlayPaint)

        val box = RectF(60f, height * 0.28f, width - 60f, height * 0.62f)
        canvas.drawRoundRect(box, 36f, 36f, gameOverPanelPaint)

        canvas.drawText("Juego Terminado", box.centerX(), box.top + 70f, gameOverTitlePaint)
        canvas.drawText("$score", box.centerX(), box.top + 150f, gameOverScorePaint)
        canvas.drawText("puntos", box.centerX(), box.top + 180f, gameOverTextPaint)

        val restartRect = RectF(box.centerX() - 120f, box.bottom - 75f, box.centerX() + 120f, box.bottom - 25f)
        canvas.drawRoundRect(restartRect, 20f, 20f, restartButtonPaint)
        canvas.drawText("Reiniciar", restartRect.centerX(), restartRect.centerY() + 10f, startButtonTextPaint)
        startButtonRect = restartRect
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            if (gameOver) {
                resetGame()
                invalidate()
                return true
            }

            if (!started && !countdownActive) {
                val touchX = event.x
                val touchY = event.y

                val tappedColor = colorButtonRects.indexOfFirst { it.contains(touchX, touchY) }
                if (tappedColor != -1) {
                    selectedColorIndex = tappedColor
                    ball.setColor(ballColors[selectedColorIndex])
                    invalidate()
                    return true
                }

                val tappedSize = sizeButtonRects.indexOfFirst { it.contains(touchX, touchY) }
                if (tappedSize != -1) {
                    selectedSizeIndex = tappedSize
                    ball.setRadius(ballSizes[selectedSizeIndex])
                    invalidate()
                    return true
                }

                if (startButtonRect?.contains(touchX, touchY) == true) {
                    startCountdown()
                    invalidate()
                    return true
                }
                return true
            }
        }
        return true
    }

    private fun vibrate(milliseconds: Long) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(milliseconds, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(milliseconds)
        }
    }
}
