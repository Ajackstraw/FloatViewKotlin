package com.jackstraw.floatview

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.jackstraw.floatwindow.manager.FloatViewManager
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btn_show.setOnClickListener {
            FloatViewManager.getInstance()?.showFloatView(this@MainActivity, FloatView(this@MainActivity))
        }

        btn_dismiss.setOnClickListener{
            FloatViewManager.getInstance()?.dismissFloatView()
        }
    }
}
