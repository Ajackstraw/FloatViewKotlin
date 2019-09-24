package com.jackstraw.floatwindow.view

import android.content.Context
import android.graphics.Point
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.ViewConfiguration
import android.view.WindowManager
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Interpolator
import android.widget.FrameLayout

abstract class BaseFloatView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : FrameLayout(context, attrs, defStyleAttr) {

    /**
     * 记录手指按下时在小悬浮窗的View上的横坐标的值
     */
    private var xInView: Float = 0.toFloat()

    /**
     * 记录手指按下时在小悬浮窗的View上的纵坐标的值
     */
    private var yInView: Float = 0.toFloat()
    /**
     * 记录当前手指位置在屏幕上的横坐标值
     */
    private var xInScreen: Float = 0.toFloat()

    /**
     * 记录当前手指位置在屏幕上的纵坐标值
     */
    private var yInScreen: Float = 0.toFloat()

    /**
     * 记录手指按下时在屏幕上的横坐标的值
     */
    private var xDownInScreen: Float = 0.toFloat()

    /**
     * 记录手指按下时在屏幕上的纵坐标的值
     */
    private var yDownInScreen: Float = 0.toFloat()

    private var isAnchoring = false
    private var isShowing = false
    private var windowManager: WindowManager? = null
    private var mParams: WindowManager.LayoutParams? = null
    private var onFloatClickListener: OnFloatClickListener? = null

    abstract val baseFloatView: BaseFloatView?

    init {
        initWindowManager()
        this.createFloatView()
    }

    private fun initWindowManager() {
        windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    }

    fun setOnFloatClickListener(onFloatClickListener: OnFloatClickListener) {
        this.onFloatClickListener = onFloatClickListener
    }

    abstract fun createFloatView()

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (isAnchoring) {
            return true
        }
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                xInView = event.x
                yInView = event.y
                xDownInScreen = event.rawX
                yDownInScreen = event.rawY
                xInScreen = event.rawX
                yInScreen = event.rawY
            }
            MotionEvent.ACTION_MOVE -> {
                xInScreen = event.rawX
                yInScreen = event.rawY
                // 手指移动的时候更新小悬浮窗的位置
                updateViewPosition()
            }
            MotionEvent.ACTION_UP -> {
                if (Math.abs(xDownInScreen - xInScreen) <= ViewConfiguration.get(context).scaledTouchSlop && Math.abs(yDownInScreen - yInScreen) <= ViewConfiguration.get(context).scaledTouchSlop) {
                    // 点击效果
                    if (onFloatClickListener != null) {
                        onFloatClickListener!!.onClick()
                    }
                } else {
                    //吸附效果
                    anchorToSide()
                }
            }
            else -> {

            }
        }
        return true
    }

    fun setParams(params: WindowManager.LayoutParams?) {
        mParams = params
    }

    fun setIsShowing(isShowing: Boolean) {
        this.isShowing = isShowing
    }

    private fun anchorToSide() {
        isAnchoring = true
        val size = Point()
        windowManager!!.defaultDisplay.getSize(size)
        val screenWidth = size.x
        val screenHeight = size.y
        val middleX = mParams!!.x + width / 2

        var animTime = 0
        var xDistance = 0
        var yDistance = 0

        val dp_25 = dp2px(15f)

        if (middleX <= dp_25 + width / 2) {
            xDistance = dp_25 - mParams!!.x
        } else if (middleX <= screenWidth / 2) {
            xDistance = dp_25 - mParams!!.x
        } else if (middleX >= screenWidth - width / 2 - dp_25) {
            xDistance = screenWidth - mParams!!.x - width - dp_25
        } else {
            xDistance = screenWidth - mParams!!.x - width - dp_25
        }

        if (mParams!!.y < dp_25) {
            yDistance = dp_25 - mParams!!.y
        } else if (mParams!!.y + height + dp_25 >= screenHeight) {
            yDistance = screenHeight - dp_25 - mParams!!.y - height
        }
        Log.e(TAG, "xDistance-${xDistance}-yDistance-${yDistance}")

        animTime = if (Math.abs(xDistance) > Math.abs(yDistance))
            (xDistance.toFloat() / screenWidth.toFloat() * 600f).toInt()
        else
            (yDistance.toFloat() / screenHeight.toFloat() * 900f).toInt()
        this.post(AnchorAnimRunnable(Math.abs(animTime), xDistance, yDistance, System.currentTimeMillis()))
    }

    private inner class AnchorAnimRunnable(private val animTime: Int, private val xDistance: Int, private val yDistance: Int, private val currentStartTime: Long) : Runnable {
        private val interpolator: Interpolator
        private val startX: Int
        private val startY: Int

        init {
            interpolator = AccelerateDecelerateInterpolator()
            startX = mParams!!.x
            startY = mParams!!.y
        }

        override fun run() {
            if (System.currentTimeMillis() >= currentStartTime + animTime) {
                if (mParams!!.x != startX + xDistance || mParams!!.y != startY + yDistance) {
                    mParams!!.x = startX + xDistance
                    mParams!!.y = startY + yDistance
                    if (baseFloatView != null) {
                        windowManager!!.updateViewLayout(baseFloatView, mParams)
                    }
                }
                isAnchoring = false
                return
            }
            val delta = interpolator.getInterpolation((System.currentTimeMillis() - currentStartTime) / animTime.toFloat())
            val xMoveDistance = (xDistance * delta).toInt()
            val yMoveDistance = (yDistance * delta).toInt()
            Log.e(TAG, "delta:  $delta  xMoveDistance  $xMoveDistance   yMoveDistance  $yMoveDistance")
            mParams!!.x = startX + xMoveDistance
            mParams!!.y = startY + yMoveDistance
            if (!isShowing) {
                return
            }
            windowManager!!.updateViewLayout(baseFloatView, mParams)
            if (baseFloatView != null) {
                baseFloatView!!.postDelayed(this, 16)
            }
        }
    }

    fun dp2px(dp: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dp * scale + 0.5f).toInt()
    }

    private fun updateViewPosition() {
        //增加移动误差
        mParams!!.x = (xInScreen - xInView).toInt()
        mParams!!.y = (yInScreen - yInView).toInt()
        windowManager!!.updateViewLayout(this, mParams)
    }

    /**
     * 点击事件
     */
    interface OnFloatClickListener {
        /**
         * 单击
         */
        fun onClick()
    }

    companion object {
        private const val TAG = "BaseFloatView"
    }

}
