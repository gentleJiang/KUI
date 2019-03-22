package com.kui.view

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import kotlin.math.roundToInt

class LoadingView : View {
    companion object {
        private const val DEFAULT_WIDTH = 100
        private const val DEFAULT_HEIGHT = 100
        private const val DEFAULT_ITEM_WIDTH = 35
    }

    private var revolveTime = 0
    private var circlePaint = Paint()
    private var isSetAnimal = false

    constructor(context: Context) : super(context)

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet) {
        initialView(context, attributeSet)
        initialDefaultParam()
    }

    constructor(context: Context, attributeSet: AttributeSet, defStyleAttr: Int)
            : super(context, attributeSet, defStyleAttr) {
        initialView(context, attributeSet)
        initialDefaultParam()
    }

    private fun initialDefaultParam() {
        circlePaint.isAntiAlias = true
        circlePaint.color = Color.RED
        circlePaint.strokeWidth = 8f
        circlePaint.style = Paint.Style.FILL
    }

    private fun initialView(context: Context, attributeSet: AttributeSet) {
        val typeArray = context.obtainStyledAttributes(attributeSet, R.styleable.LoadingView)
        typeArray.recycle()
    }

    private fun getMeasuredSize(defaultSize: Int, measureSpec: Int): Int {
        val specMode = MeasureSpec.getMode(measureSpec)
        val specSize = MeasureSpec.getSize(measureSpec)
        return when (specMode) {
            MeasureSpec.UNSPECIFIED -> defaultSize
            MeasureSpec.EXACTLY -> specSize
            MeasureSpec.AT_MOST -> defaultSize
            else -> defaultSize
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val width = getMeasuredSize(DEFAULT_WIDTH, widthMeasureSpec)
        val height = getMeasuredSize(DEFAULT_HEIGHT, heightMeasureSpec)
        val minSize = Math.min(width, height)
        setMeasuredDimension(minSize, minSize)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val contentWidth = width - paddingLeft - paddingRight
        val contentHeight = height - paddingTop - paddingBottom
        val radius = Math.min(contentWidth, contentHeight) / 2f
        val roundLength = 2 * Math.PI * radius
        val cx = paddingLeft + radius
        val cy = paddingTop + radius
        val itemNum = (roundLength / DEFAULT_ITEM_WIDTH).toInt()
        val averageDegree = 360f / itemNum
        var itemHeight = radius / 3
        if (itemHeight < 25) {
            itemHeight = 25f
        }

        revolveTime = (roundLength / 350).roundToInt()
        canvas.translate(cx, cy)
        for (i in 1..itemNum) {
            circlePaint.alpha = (i.toFloat() / itemNum * 255).roundToInt()
            canvas.drawLine(radius, 0f, radius - itemHeight, 0f, circlePaint)
            canvas.rotate(averageDegree)
        }
        canvas.restore()

        if (!isSetAnimal) {
            isSetAnimal = true
            startAnimal()
        }
    }

    private fun startAnimal() {
        clearAnimation()

        val animator = ObjectAnimator.ofFloat(this, "rotation", 0f, 360f)
        animator.duration = revolveTime * 1000L
        animator.repeatMode = ValueAnimator.RESTART
        animator.repeatCount = ValueAnimator.INFINITE
        animator.interpolator = LinearInterpolator()
        animator.start()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        clearAnimation()
    }
}