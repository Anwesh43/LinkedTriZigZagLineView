package com.example.trizigzaglineview

import android.view.View
import android.view.MotionEvent
import android.app.Activity
import android.graphics.Paint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color

val parts : Int = 4
val scGap : Float = 0.02f / parts
val strokeFactor : Float = 90f
val delay : Long = 20
val sizeFactor : Float = 3.8f
val colors : Array<Int> = arrayOf(
    "#F44336",
    "#2196F3",
    "#FF9800",
    "#4CAF50",
    "#FF5722"
).map {
    Color.parseColor(it)
}.toTypedArray()
val backColor : Int = Color.parseColor("#BDBDBD")
