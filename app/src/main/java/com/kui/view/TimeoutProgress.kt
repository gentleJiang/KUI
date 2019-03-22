package com.kui.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

/**
 * The timeout progress bar
 * It will invoke listener when get to your set time!
 */
class TimeoutProgress : View {
    companion object {
        private val DEFAULT_BACKGROUND_COLOR = Color.parseColor("#FFFFFF")
        private val DEFAULT_UNCOMPLETED_COLOR = Color.parseColor("#DDDDDD")
        private val DEFAULT_COMPLETED_COLOR = Color.parseColor("#0000ff")
        private const val DEFAULT_TIMEOUT = 60//SECOND
    }

    private val paintUncompleted = Paint()
    private val paintCompleted = Paint()
    private val rect = Rect()

    private var isPause: Boolean = false
    private var hasInitialed: Boolean = false
    private var shader: LinearGradient? = null
    private var currentValue = 0L
    private var timeout: Int = DEFAULT_TIMEOUT
    private var uncompletedColor = DEFAULT_UNCOMPLETED_COLOR
    private var completedColor = DEFAULT_COMPLETED_COLOR
    private var bgColor = DEFAULT_BACKGROUND_COLOR
    private var listener= {}
    private lateinit var tick: Runnable

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

    private fun initialView(context: Context, attributeSet: AttributeSet) {
        val typeArray = context.obtainStyledAttributes(attributeSet, R.styleable.TimeoutProgress)
        bgColor = typeArray.getColor(R.styleable.TimeoutProgress_backgroundColor, DEFAULT_BACKGROUND_COLOR)
        uncompletedColor = typeArray.getColor(R.styleable.TimeoutProgress_uncompletedColor, DEFAULT_UNCOMPLETED_COLOR)
        typeArray.recycle()
    }

    private fun initialDefaultParam() {
        paintUncompleted.isAntiAlias = true
        paintUncompleted.color = uncompletedColor
        paintUncompleted.style = Paint.Style.FILL

        paintCompleted.isAntiAlias = true
        paintCompleted.color = completedColor
        paintCompleted.shader
        paintCompleted.style = Paint.Style.FILL

        tick = Runnable {
            if(isPause){
                return@Runnable
            }

            currentValue += 500
            if (currentValue > timeout * 1000L) {
                listener()
                return@Runnable
            }
            postDelayed(tick, 500)
            invalidate()
        }
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
        setMeasuredDimension(
            getMeasuredSize(100, widthMeasureSpec),
            getMeasuredSize(30, heightMeasureSpec)
        )
    }

    override fun onDraw(canvas: Canvas) {

        canvas.drawColor(bgColor)

        rect.set(paddingLeft, paddingTop, width - paddingRight, height - paddingBottom)
        canvas.drawRect(rect, paintUncompleted)

        val currentProgress = currentValue / (timeout * 1000f)
        val currentWidth = (width - paddingRight - paddingRight) * currentProgress
        if (!hasInitialed) {
            shader = LinearGradient(
                paddingLeft.toFloat(),
                paddingTop.toFloat(),
                width - paddingRight.toFloat(),
                height - paddingBottom.toFloat(),
                Color.GREEN,
                Color.RED,
                Shader.TileMode.CLAMP
            )
            hasInitialed = true
            paintCompleted.shader = shader
        }
        rect.set(paddingLeft, paddingTop, currentWidth.toInt(), height - paddingBottom)
        canvas.drawRect(rect, paintCompleted)
    }

    public fun start(timeout: Int) {
        this.timeout = timeout
        postDelayed(tick, 500)
    }

    public fun setOnTimeoutListener(timeoutListener: () -> Unit){
        listener = timeoutListener
    }

    public fun reset() {
        currentValue = 0
        postDelayed(tick, 500)
        invalidate()
    }

    public fun pause() {
        isPause = true
    }

    public fun resume() {
        postDelayed(tick, 500)
        invalidate()
    }
}