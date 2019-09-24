package com.jackstraw.floatview

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.Toast

import com.jackstraw.floatwindow.view.BaseFloatView

class FloatView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : BaseFloatView(context, attrs, defStyleAttr), BaseFloatView.OnFloatClickListener {

    override val baseFloatView: BaseFloatView?
        get() = this

    override fun createFloatView() {
        val inflater = LayoutInflater.from(context)
        val floatView = inflater.inflate(R.layout.float_window_layout, null)
        addView(floatView)
        setOnFloatClickListener(this)
    }

    override fun onClick() {
        val toast = Toast.makeText(context, null, Toast.LENGTH_SHORT)
        toast.setText("FloatView-click")
        toast.show()
    }
}
