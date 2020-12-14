package com.example.trizigzaglineview

import android.view.View
import android.view.MotionEvent
import android.app.Activity
import android.graphics.Paint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color

val parts : Int = 3
val lineParts : Int = 3
val triParts : Int = 2
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

fun Int.inverse() : Float = 1f / this
fun Float.maxScale(i : Int, n : Int) : Float = Math.max(0f, this - i * n.inverse())
fun Float.divideScale(i : Int, n : Int) : Float = Math.min(n.inverse(), maxScale(i, n)) * n

fun Canvas.drawEndingLine(xSize : Float, ySize : Float, sc1 : Float, sc2 : Float, paint : Paint) {
    drawLine(xSize * sc2, ySize * sc2, xSize * sc1, ySize * sc1, paint)
}

fun Canvas.drawTriZigZagLine(scale : Float, w : Float, h : Float, paint : Paint) {
    val sc1 : Float = scale.divideScale(0, parts)
    val sc2 : Float = scale.divideScale(1, parts)
    val size : Float = Math.min(w, h) / sizeFactor
    val gap : Float = size / (2 * lineParts)
    save()
    translate(w / 2 - 2 * size, h / 2)
    drawEndingLine(size, 0f, sc1.divideScale(0, parts), sc2.divideScale(0, parts), paint)
    for (j in 0..(lineParts - 1)) {
        val sck1 : Float = sc1.divideScale(1, parts).divideScale(j, lineParts)
        val sck2 : Float = sc2.divideScale(1, parts).divideScale(j, lineParts)
        save()
        translate(size + j * 2 * gap, 0f)
        drawEndingLine(gap, -gap, sck1.divideScale(0, triParts), sck2.divideScale(0, triParts), paint)
        save()
        translate(gap, -gap)
        drawEndingLine(gap, gap, sck1.divideScale(1, triParts), sck2.divideScale(1, triParts), paint)
        restore()
        restore()
    }
    save()
    translate(3 * size, 0f)
    drawEndingLine(size, 0f, sc1.divideScale(2, parts), sc2.divideScale(2, parts), paint)
    restore()
    restore()
}

fun Canvas.drawTZZLNode(i : Int, scale : Float, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    val size : Float = Math.min(w, h) / sizeFactor
    paint.color = colors[i]
    paint.strokeCap = Paint.Cap.ROUND
    paint.strokeWidth = Math.min(w, h) / strokeFactor
    drawTriZigZagLine(scale, w, h, paint)
}

class TriZigZagLineView(ctx : Context) : View(ctx) {

    override fun onDraw(canvas : Canvas) {

    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {

            }
        }
        return true
    }
}