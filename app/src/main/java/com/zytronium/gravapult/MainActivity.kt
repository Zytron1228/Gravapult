package com.zytronium.gravapult

import android.annotation.SuppressLint
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.forEach
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import kotlin.math.*

class MainActivity : AppCompatActivity() {
    private lateinit var player: ImageView
    private lateinit var moon: ImageView
    private lateinit var planet: ImageView
    private lateinit var space: ConstraintLayout
    private lateinit var TVangle: TextView

    private var maxObstDist = 25
    private var grav: Force = Force(0f, 0f)
    private var velc = Force(0f, 0f, rightRot = 6f)
//    private var totalForce: MutableList<Force> = mutableListOf(velc, grav)
    private var simulating = false
    private var startPoint: Coordinate = Coordinate(0f, 0f)
    private var found = false
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        player = findViewById(R.id.player)
        moon = findViewById(R.id.moon)
        planet = findViewById(R.id.planet)
        space = findViewById(R.id.space)
        TVangle = findViewById(R.id.angleText)
            val windowInsetsController =
                ViewCompat.getWindowInsetsController(window.decorView) ?: return
            // Configure the behavior of the hidden system bars
            windowInsetsController.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            // Hide both the status bar and the navigation bar
            windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())

        var mn = Thing(moon, 0f, Velocity(0f, 0f), mutableListOf(Force(40f, 11f), grav, Force(0f, 0f/*, angle = 0f, strength = 0f*/)))

        var plr = Thing(player, 0f, Velocity(0f, 0f), mutableListOf(velc, grav))
        println("x: " + player.x.toString() + " y: " +  player.y.toString())
        planet.setOnClickListener {
            simulating = !simulating
            if(simulating) {
                var no = listOf<ImageView>()
                space.forEach { view: View ->
                    if(view is ImageView && view.tag == "physical") {
                        no = no.plus(view)
//                        space.removeView(view)
                        view.visibility = View.GONE
                    }
                }
                no.forEach {
                    space.removeView(it)
                }
                no = listOf<ImageView>()
            velc = Force(0f, 0f, rightRot = 6f)
                player.x = startPoint.x
                player.y = startPoint.y
                applyForces(plr)
                applyForces(mn)
            }
        }
        space.setOnTouchListener(onTouchListener())
        findStartPoint()

    }

        @SuppressLint("ClickableViewAccessibility")
    private fun onTouchListener(): View.OnTouchListener {
        return View.OnTouchListener { _, event ->
            val x = event.rawX
            val y = event.rawY

            when (event.action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_DOWN -> {
                    val asteroid = ImageView(this)
                    asteroid.layoutParams = ConstraintLayout.LayoutParams(
            player.measuredWidth,
            player.measuredHeight
        )
                    asteroid.x = x - player.width*2f
                    asteroid.y = y - player.height/2f
                    asteroid.background = getColor(R.color.purple_500).toDrawable()
                    asteroid.tag = "physical"

            space.addView(asteroid)
                    var ast = Thing(asteroid, 0f, Velocity(0f, 0f), mutableListOf(Force(0f, 0f, -9f, 0f,0f, 0f), Force(0f, 0f), Force(0f, 0f, 0f, 0f, 0f, 66f)))
                    rainbow(ast.view, 10f, 40)
                    applyForces(ast)
                }
                MotionEvent.ACTION_MOVE -> {
                    TVangle.text = "Angle: ${angleBetween(Coordinate(x, y), planet)}"
                }


            }

            true
        }
    }

    private fun findStartPoint() {
        Handler().postDelayed({
            startPoint = Coordinate(player.x, player.y)
            if(startPoint.x != 0f || startPoint.y != 0f)
                found = true
            else {
                println("Not found")
                findStartPoint()
            }
            },15)
    }

    private fun applyForces(obj: Thing) {
        val i = obj.view
        var vlty = obj.forces.component1()
            Handler().postDelayed({
        if(simulating) {
            val x1 = i.x//+(i.width/2)
            val y1 = i.y//+(i.height/2)
            val p1 = Coordinate(x1, y1)
//                println(i.x.toString() + ", " + i.y.toString())
                grav = gravity(i, planet)
//                println(grav.toString())
                obj.forces.component2().x = grav.x
                obj.forces.component2().y = grav.y

                var totalX: Float = 0f
                var totalY: Float = 0f
                var rot = 0f
                obj.forces.forEach { force: Force ->
                    totalX += force.x
                    totalY += force.y
                    rot -= force.leftRot
                    rot += force.rightRot
                    if(force.angle != 0F || force.strength != 0f) {
                        println("Force angle: ${force.angle}; Force strength: ${force.strength}")
                        if(force.angle !in (0f..360f)) println("Warning: Force angle is not between 0 and 360. Force angle = ${force.angle}")
                        println("angleForce: x: ${(force.strength * cos(force.angle))}, y: ${(force.strength * sin(force.angle))}")
                        totalX += (force.strength * cos((force.angle/180f)*PI.toFloat()))
                        totalY += (force.strength * sin((force.angle/180f)*PI.toFloat()))
                    }
                }
            val acclrtn = acceleration(Force(totalX-vlty.x, totalY-vlty.y, 0f, rot), i)
//            obj.velocity.speed = acclrtn
                i.x += vlty.x + ((totalX-vlty.x)*acclrtn)
                i.y -= vlty.y + ((totalY-vlty.y)*acclrtn)
                i.rotation += rot/1.25f
//                println(i.x.toString() + ", " + i.y.toString())
            val x2 = i.x
            val y2 = i.y
            val p2 = Coordinate(x2, y2)
            val accl2 = kotlin.math.sqrt(
                            kotlin.math.abs(p1.x - p2.x).toDouble().pow(2) + kotlin.math.abs(p1.y - p2.y)
                                .toDouble().pow(2)
                        )
//            println("estimated acclrtn: $acclrtn; actual change in position: $accl2")
            val oldVlcty = vlty
            vlty = Force(totalX-vlty.x, totalY-vlty.y)
//                println("velocity: $vlty")
                obj.forces.component1().x = vlty.x
                obj.forces.component1().y = vlty.y
                obj.forces.component1().leftRot = velc.leftRot
                obj.forces.component1().rightRot = velc.rightRot
            if(obj.view == moon) {
                obj.forces = mutableListOf(/*Force(0f, 0f)*/obj.forces.component1(), obj.forces.component2()/*, Force((oldVlcty.x + velc.x)/1.0525f, (oldVlcty.y + velc.y)/1.0525f)*/, Force(0f, 0f, angle = calcOrbitAngle(obj.view, planet)+90f, strength = CalcOrbitVelocity(obj.view, planet)*1.5f))
            } else {
                obj.forces = mutableListOf(obj.forces.component1(), obj.forces.component2(), Force((oldVlcty.x + velc.x)/1.0525f, (oldVlcty.y + velc.y)/1.0525f))
            }
                applyForces(obj)
            }
        }, 21)
        }

    private fun CalcOrbitVelocity(moon: View, planet: View): Float {
        val g = 6.2742f * 10f.pow(-11).toFloat()
        val m = planet.width.toFloat()*planet.height.toFloat()
        val r = kotlin.math.sqrt(
                kotlin.math.abs(moon.x - planet.x).toFloat().pow(2) + kotlin.math.abs(moon.y - planet.y)
                    .toFloat().pow(2)
                ).toFloat()
//        val cent1 = Coordinate(obj1.x + (obj1.width/2), obj1.y + (obj1.height/2))
//        val cent2 = Coordinate(obj2.x + (obj2.width/2), obj2.y + (obj2.height/2))

        return sqrt((g*m) / (r/125f))*1500f /*.pow(2)*/

    }

    private fun calcOrbitAngle(obj: View, plnt: View, clockwise: Boolean = true): Float {
        val a: Float = (angleBetween(obj, plnt) + if(clockwise) 90f else -90f)
        return if(a >= 360f) (a - 360f) else if(a < 0f) (a + 360f) else a
    }

    private fun findAngle(x: Float, y: Float) : Float {
        return (((atan(y/x)/(PI.toFloat()/2))*90f) + if(x >= 0f) 180f
            else if(y > 0f) 360f else 0f)
    }

    private fun angleBetween(from: View, to: View): Float {

        val cent1 = Coordinate(from.x + (from.width/2), from.y + (from.height/2))
        val cent2 = Coordinate(to.x + (to.width/2), to.y + (to.height/2))
        val reltvPos2 =  Coordinate(cent2.x-cent1.x, cent2.y-cent1.y)
        return findAngle(reltvPos2.x, reltvPos2.y)
    }

    private fun angleBetween(from: Coordinate, to: Coordinate): Float {

        val reltvPos2 =  Coordinate(to.x-from.x, to.y-from.y)
        return findAngle(reltvPos2.x, reltvPos2.y)
    }
    private fun angleBetween(from: Coordinate, to: View): Float {

        val cent2 = Coordinate(to.x + (to.width/2), to.y + (to.height/2))
        val reltvPos2 =  Coordinate(cent2.x - from.x, cent2.y - from.y)
        return findAngle(reltvPos2.x, reltvPos2.y)
    }

    private fun angleBetween(from: View, to: Coordinate): Float {

        val cent1 = Coordinate(from.x + (from.width/2), from.y + (from.height/2))
        val reltvPos2 =  Coordinate(to.x - cent1.x, to.y - cent1.y)
        return findAngle(reltvPos2.x, reltvPos2.y)
    }

    private fun gravity(obj1: View, obj2: View): Force {
        val mass1 = obj1.width.toFloat() * obj1.height.toFloat()
        val mass2 = obj2.width.toFloat() * obj2.height.toFloat()
        val cent1 = Coordinate(obj1.x + (obj1.width/2), obj1.y + (obj1.height/2))
        val cent2 = Coordinate(obj2.x + (obj2.width/2), obj2.y + (obj2.height/2))
        val dist = kotlin.math.sqrt(
                            kotlin.math.abs(cent1.x - cent2.x).toFloat().pow(2) + kotlin.math.abs(cent1.y - cent2.y)
                                .toFloat().pow(2)
                        )
        val reltvPos2 =  Coordinate(cent2.x-cent1.x, cent1.y-cent2.y)
        val gc = 6.2742f * 10f.pow(-11).toFloat()
        val gForce = (gc*((mass1*mass2) / (dist/215f).pow(2)).toFloat())//1.33f
//        println("gforce: $gForce")
        return Force(((/*reltvPos2.x / */ (reltvPos2.x)) * (if(gForce > 1f) 1F else gForce) /*if(reltvPos2.x < 0) (gForce * -1) else gForce*/ ), ((/*reltvPos2.x / */(reltvPos2.y)) * (if (gForce > 1f) 1f else gForce))/*(if(reltvPos2.y < 0) (gForce) else (gForce * -1f))*/)


    }

    private fun acceleration(force: Force, obj: View): Float {
        val m = (obj.width * obj.height).toFloat()
        val f = kotlin.math.sqrt(
            kotlin.math.abs(force.x).pow(2) + kotlin.math.abs(force.y).pow(2)
        ).toFloat()
        return (f/6)/m
    }

    private fun rainbow(target: View, hue: Float, speed: Long) {
        var h = hue
        if (h >= 360f) h = 0f
        target.setBackgroundColor(Color.HSVToColor(floatArrayOf(h,100f,100f)))
        Handler(Looper.getMainLooper()).postDelayed({ if(target.visibility == View.VISIBLE) rainbow(target, (h + 2.5f), speed) }, (speed))
    }
}

class Thing(
    var view: View,
    var speed: Float = 0f,
    var velocity: Velocity = Velocity(0f, 0f),
    var forces: MutableList<Force> = mutableListOf<Force>(Force(0f, 0f, 0f, 1f), Force(0f, 0f))
)

class Velocity(
    var speed: Float = 0f,
    var direction: Float = 0f
)

class Force(var x: Float, var y: Float, var leftRot: Float = 0f, var rightRot: Float = 0f, var angle: Float = 0F, var strength: Float = 0F)

class Coordinate(val x: Float, val y: Float) // same as class Point() but with Floats instead of Ints