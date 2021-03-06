package com.example.trizigzaglineview

import android.view.View
import android.view.MotionEvent
import android.app.Activity
import android.graphics.Paint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PointF

val parts : Int = 3
val mainParts : Int = 2
val lineParts : Int = 3
val triParts : Int = 2
val scGap : Float = 0.02f / (parts)
val strokeFactor : Float = 90f
val delay : Long = 20
val sizeFactor : Float = 6.8f
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
fun PointF.distance(p : PointF) : Float = Math.sqrt(
    Math.pow((p.x - x).toDouble(), 2.0) + Math.pow((p.y - y).toDouble(), 2.0)
).toFloat()

fun Canvas.drawEndingLine(xSize : Float, ySize : Float, sc1 : Float, sc2 : Float, paint : Paint) {
    if (PointF(xSize * sc1, ySize * sc1).distance(PointF(xSize * sc2, ySize * sc2)) < 0.1f) {
        return
    }
    drawLine(xSize * sc2, ySize * sc2, xSize * sc1, ySize * sc1, paint)
}

fun Canvas.drawTriZigZagLine(scale : Float, w : Float, h : Float, paint : Paint) {
    val sc1 : Float = scale.divideScale(0, mainParts)
    val sc2 : Float = scale.divideScale(1, mainParts)
    val size : Float = Math.min(w, h) / sizeFactor
    val gap : Float = size / lineParts
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

    private val renderer : Renderer = Renderer(this)

    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var dir : Float = 0f, var prevScale : Float = 0f) {

        fun update(cb : (Float) -> Unit) {
            scale += scGap * dir
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                cb(prevScale)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            if (dir == 0f) {
                dir = 1f - 2 * prevScale
                cb()
            }
        }
    }

    data class Animator(var view : View, var animated : Boolean = false) {

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(delay)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }

    data class TZZLNode(var i : Int, val state : State = State()) {

        private var next : TZZLNode? = null
        private var prev : TZZLNode? = null

        init {
            addNeighbor()
        }

        fun addNeighbor() {
            if (i < colors.size - 1) {
                next = TZZLNode(i + 1)
                next?.prev = this
            }
        }

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawTZZLNode(i, state.scale, paint)
        }

        fun update(cb : (Float) -> Unit) {
            state.update(cb)
        }

        fun startUpdating(cb : () -> Unit) {
            state.startUpdating(cb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : TZZLNode {
            var curr : TZZLNode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }

    data class TriZigZagLine(var i : Int) {

        private var curr : TZZLNode = TZZLNode(0)
        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            curr.draw(canvas, paint)
        }

        fun update(cb : (Float) -> Unit) {
            curr.update {
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                cb(it)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            curr.startUpdating(cb)
        }
    }

    data class Renderer(var view : TriZigZagLineView) {

        private val animator : Animator = Animator(view)
        private val tzzl : TriZigZagLine = TriZigZagLine(0)
        private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

        fun render(canvas : Canvas) {
            canvas.drawColor(backColor)
            tzzl.draw(canvas, paint)
            animator.animate {
                tzzl.update {
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            tzzl.startUpdating {
                animator.start()
            }
        }
    }

    companion object {

        fun create(activity : Activity) : TriZigZagLineView {
            val view : TriZigZagLineView = TriZigZagLineView(activity)
            activity.setContentView(view)
            return view
        }
    }
}