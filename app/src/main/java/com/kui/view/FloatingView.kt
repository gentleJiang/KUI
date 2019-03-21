package com.kui.view

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View


class FloatingView : View {
    companion object {
        private const val DEFAULT_WIDTH = 100
        private const val DEFAULT_HEIGHT = 100
        private const val DEFAULT_TEXT_SIZE = 20F
        private const val DEFAULT_TEXT_COLOR = 0xFFFFFF
    }

    private var attrText: String? = ""
    private var attrTextSize: Float = 0f
    private var attrTextColor: Int = 0


    private lateinit var paintText: Paint
    private lateinit var paintBackground: Paint
    private var startX: Float = 0f
    private var startY: Float = 0f
    private var touchX: Float = 0f
    private var touchY: Float = 0f
    private var screenWidth: Float = 0f
    private var screenHeight: Float = 0f
    private var statusBarHeight: Float = 0f


    constructor(context: Context) : super(context) {
        initDefaultParam()
    }

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet) {
        initialView(context, attributeSet)
        initDefaultParam()
    }

    constructor(context: Context, attributeSet: AttributeSet, defStyleAttr: Int) : super(
        context,
        attributeSet,
        defStyleAttr
    ) {
        initialView(context, attributeSet)
        initDefaultParam()
    }

    private fun initialView(context: Context, attributeSet: AttributeSet) {
        val typeArray = context.obtainStyledAttributes(attributeSet, R.styleable.FloatingView)
        attrText = typeArray.getString(R.styleable.FloatingView_text)
        attrTextSize = typeArray.getDimension(R.styleable.FloatingView_textSize, DEFAULT_TEXT_SIZE)
        attrTextColor = typeArray.getColor(R.styleable.FloatingView_textColor, DEFAULT_TEXT_COLOR)
        typeArray.recycle()
    }

    private fun initDefaultParam() {
        paintBackground = Paint()
        paintBackground.style = Paint.Style.FILL
        paintBackground.color = Color.BLUE
        paintBackground.isAntiAlias = true

        paintText = Paint()
        paintText.textSize = attrTextSize
        paintText.color = attrTextColor

        statusBarHeight = getStateBarHeight()

        val dm = resources.displayMetrics
        screenHeight = dm.heightPixels.toFloat() - statusBarHeight
        screenWidth = dm.widthPixels.toFloat()
    }

    private fun getMeasuredSize(defaultSize: Int, measureSpec: Int): Int {
        val specMode = MeasureSpec.getMode(measureSpec)
        val specSize = MeasureSpec.getSize(measureSpec)
        val size: Int
        size = when (specMode) {
            MeasureSpec.UNSPECIFIED -> defaultSize
            MeasureSpec.EXACTLY -> specSize
            MeasureSpec.AT_MOST -> defaultSize
            else -> defaultSize
        }
        return size
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(
            getMeasuredSize(DEFAULT_WIDTH, widthMeasureSpec),
            getMeasuredSize(DEFAULT_HEIGHT, heightMeasureSpec)
        )
    }

    override fun onDraw(canvas: Canvas) {
        val contentWidth = width - paddingLeft - paddingRight
        val contentHeight = height - paddingTop - paddingBottom
        val circleRadius = (Math.min(contentWidth, contentHeight)) / 2f
        val centerX: Float = left + paddingLeft + contentWidth / 2f
        val centerY: Float = top + paddingTop + contentHeight / 2f
        canvas.drawCircle(centerX, centerY, circleRadius, paintBackground)

        attrText?.let {
            var needShowText = attrText!!
            val originalText = attrText!!
            var textOccupyWidth = paintText.measureText(needShowText)
            if (textOccupyWidth > contentWidth) {
                for (i in needShowText.indices) {
                    needShowText = originalText.substring(0, originalText.length - i)
                    textOccupyWidth = paintText.measureText(needShowText)
                    if (textOccupyWidth <= contentWidth) {
                        break
                    }
                }
            }
            val baselineX = paintText.measureText(needShowText) / 2
            val baselineY =
                centerY + (paintText.fontMetrics.descent - paintText.fontMetrics.ascent) / 2 - paintText.fontMetrics.descent
            canvas.drawText(needShowText, centerX - baselineX, baselineY, paintText)
        }
    }

    override fun performClick(): Boolean {
        return super.performClick()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                startX = event.rawX
                startY = event.rawY
                touchX = event.x
                touchY = event.y
            }
            MotionEvent.ACTION_MOVE -> {
                if (Math.abs(event.rawX - startX) > 8 || Math.abs(event.rawY - startY) > 8) {
                    var locationX = event.rawX - touchX
                    var locationY = event.rawY - touchY - statusBarHeight

                    if (locationX < 0) {
                        locationX = 0f
                    } else if (locationX + width > screenWidth) {
                        locationX = screenWidth - width
                    }

                    if (locationY < 0) {
                        locationY = 0f
                    } else if (locationY + height > screenHeight) {
                        locationY = screenHeight - height
                    }

                    x = locationX
                    y = locationY
                    invalidate()
                }
            }
            MotionEvent.ACTION_UP -> {
                if (Math.abs(event.rawX - startX) < 8 && Math.abs(event.rawY - startY) < 8) {
                    performClick()
                } else {
                    if ((x + width / 2) <= (screenWidth / 2f)) {
                        val translateXAnimator = ObjectAnimator.ofFloat(this, "translationX", x, 0f)
                        translateXAnimator.duration = 500
                        translateXAnimator.start()
                    } else {
                        val translateXAnimator = ObjectAnimator.ofFloat(this, "translationX", x, screenWidth - width)
                        translateXAnimator.duration = 500
                        translateXAnimator.start()
                    }
                }
            }
        }
        return true
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        clearAnimation()
    }

    private fun getStateBarHeight(): Float {
        var result = 0
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = resources.getDimensionPixelSize(resourceId)
        }
        return result.toFloat()
    }
}