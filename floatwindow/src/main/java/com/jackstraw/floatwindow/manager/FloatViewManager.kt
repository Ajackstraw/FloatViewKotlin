package com.jackstraw.floatwindow.manager

import android.content.Context
import android.graphics.PixelFormat
import android.graphics.Point
import android.os.Build
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import com.jackstraw.floatwindow.permission.FloatUtils
import com.jackstraw.floatwindow.view.BaseFloatView


/**
 * @author pink-jackstraw
 * @date 2018/10/19
 * @describe
 */
class FloatViewManager {
    private var isWindowDismiss = true
    private var windowManager: WindowManager? = null
    private var mParams: WindowManager.LayoutParams? = null
    private var floatView: BaseFloatView? = null
    private var floatUtils: FloatUtils? = null

    /**
     * 显示浮窗
     * @param context
     */
    fun showFloatView(context: Context, t: BaseFloatView?) {
        if (t == null) {
            return
        }

        if (floatUtils == null) {
            floatUtils = FloatUtils()
        }

        if (floatUtils!!.checkPermission(context)) {
            showWindow(context, t)
        } else {
            floatUtils!!.applyPermission(context)
        }
    }

    /**
     * 关闭浮窗
     */
    fun dismissFloatView() {
        if (isWindowDismiss) {
            Log.e(TAG, "window can not be dismiss cause it has not been added")
            return
        }

        isWindowDismiss = true
        floatView!!.setIsShowing(false)
        if (windowManager != null && floatView != null) {
            windowManager!!.removeViewImmediate(floatView)
        }
    }

    private fun showWindow(context: Context, t: BaseFloatView) {
        if (!isWindowDismiss) {
            Log.e(TAG, "view is already added here")
            return
        }

        isWindowDismiss = false
        if (windowManager == null) windowManager = context.applicationContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager?
        val size = Point()
        windowManager!!.defaultDisplay.getSize(size)
        val screenWidth = size.x
        val screenHeight = size.y

        mParams = WindowManager.LayoutParams()
        mParams?.packageName = context.packageName
        mParams?.width = WindowManager.LayoutParams.WRAP_CONTENT
        mParams?.height = WindowManager.LayoutParams.WRAP_CONTENT
        mParams?.flags = (WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR
                or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN)
        val mType: Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_SYSTEM_ERROR
        }
        mParams?.type = mType
        mParams?.format = PixelFormat.RGBA_8888
        mParams?.gravity = Gravity.LEFT or Gravity.TOP
        mParams?.x = screenWidth - dp2px(context, 100f)
        mParams?.y = screenHeight - dp2px(context, 171f)

        floatView = t
        floatView?.setParams(mParams)
        floatView?.setIsShowing(true)
        windowManager!!.addView(floatView, mParams)
    }

    private fun dp2px(context: Context, dp: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dp * scale + 0.5f).toInt()
    }

    companion object {
        private const val TAG = "FloatViewManager"
        @Volatile
        private var instance: FloatViewManager? = null

        fun getInstance(): FloatViewManager? {
            if (instance == null) {
                synchronized(FloatViewManager::class.java) {
                    if (instance == null) {
                        instance = FloatViewManager()
                    }
                }
            }
            return instance
        }
    }
}
