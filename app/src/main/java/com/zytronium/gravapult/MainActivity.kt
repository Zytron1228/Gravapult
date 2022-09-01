package com.zytronium.gravapult

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.forEach
import kotlin.math.pow

class MainActivity : AppCompatActivity() {
    private lateinit var player: ImageView
    private lateinit var planet: ImageView
    private lateinit var space: ConstraintLayout

    private var maxObstDist = 25
    private var grav: Force = Force(0f, 0f)
    private var velc = Force(20f, 0f, rightRot = 6f)
    private var totalForce: MutableList<Force> = mutableListOf(velc, grav)
    private var simulating = false
    private var startPoint: Coordinate = Coordinate(0f, 0f)
    private var found = false
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        player = findViewById(R.id.player)
        planet = findViewById(R.id.planet)
        space = findViewById(R.id.space)
        var plr = Thing(player, 0f, Velocity(0f, 0f), totalForce)
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
            velc = Force(0f, 0f, rightRot = 6f)
                player.x = startPoint.x
                player.y = startPoint.y
                applyForces(plr)
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
                    asteroid.x = x + player.width/2
                    asteroid.y = y - player.height/2
                    asteroid.background = getColor(R.color.purple_500).toDrawable()
                    asteroid.tag = "physical"

            space.addView(asteroid)
                    var ast = Thing(asteroid, 0f, Velocity(0f, 0f), mutableListOf(Force(5f, 0f, 0f, 5f), Force(0f, 0f), Force(235f, 61f, -5f)))
                    applyForces(ast)
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
//        do {
//        Timer().scheduleAtFixedRate(1500, 500) {
            Handler().postDelayed({
        if(simulating) {
            var x1 = i.x//+(i.width/2)
            var y1 = i.y//+(i.height/2)
            var p1 = Coordinate(x1, y1)
                println(i.x.toString() + ", " + i.y.toString())
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
                }
            val acclrtn = acceleration(Force(totalX, totalY, 0f, rot), i)
            obj.velocity.speed = acclrtn
                i.x += (totalX*acclrtn)
                i.y -= (totalY*acclrtn)
                i.rotation += rot/2
                println(i.x.toString() + ", " + i.y.toString())
            val x2 = i.x
            val y2 = i.y
            val p2 = Coordinate(x2, y2)
            val accl2 = kotlin.math.sqrt(
                            kotlin.math.abs(p1.x - p2.x).toDouble().pow(2) + kotlin.math.abs(p1.y - p2.y)
                                .toDouble().pow(2)
                        )
            println("estimated acclrtn: $acclrtn; actual change in position: $accl2")
            val oldVlcty = vlty
            vlty = Force(totalX-vlty.x, totalY-vlty.y)
                println("velocity: $vlty")
                obj.forces.component1().x = vlty.x
                obj.forces.component1().y = vlty.y
                obj.forces.component1().leftRot = velc.leftRot
                obj.forces.component1().rightRot = velc.rightRot
            obj.forces = mutableListOf(obj.forces.component1(), obj.forces.component2(), Force((oldVlcty.x + velc.x)/1.0525f, (oldVlcty.y + velc.y)/1.0525f))
                applyForces(obj)
            }
        }, 25)
//        } while(true)
//    }
        }

    private fun gravity(obj1: View, obj2: View): Force {
        val mass1 = obj1.width * obj1.height
        val mass2 = obj2.width * obj2.height
        val cent1 = Coordinate(obj1.x + (obj1.width/2), obj1.y + (obj1.height/2))
        val cent2 = Coordinate(obj2.x + (obj2.width/2), obj2.y + (obj2.height/2))
        val dist = kotlin.math.sqrt(
                            kotlin.math.abs(cent1.x - cent2.x).toDouble().pow(2) + kotlin.math.abs(cent1.y - cent2.y)
                                .toDouble().pow(2)
                        )
        val reltvPos2 =  Coordinate(cent2.x-cent1.x, cent1.y-cent2.y)
        val gc = 6.2742f * 10.0.pow(-11).toFloat()
        val gForce = (gc*((mass1*mass2) / (dist/1300).pow(2)).toFloat())/2f
        println("gforce: $gForce")
        return Force(((/*reltvPos2.x / */ (reltvPos2.x)) * (if(gForce > 1f) 1F else gForce) /*if(reltvPos2.x < 0) (gForce * -1) else gForce*/ ), ((/*reltvPos2.x / */(reltvPos2.y)) * (if (gForce > 1) 1f else gForce))/*(if(reltvPos2.y < 0) (gForce) else (gForce * -1f))*/)


    }

    private fun acceleration(force: Force, obj: View): Float {
        val m = (obj.width * obj.height).toFloat()
        val f = kotlin.math.sqrt(
            kotlin.math.abs(force.x).toDouble().pow(2) + kotlin.math.abs(force.y)
                .toDouble().pow(2)
        ).toFloat()
        return f / m
    }

}

class Thing(
    var view: View,
    var speed: Float = 0f,
    var velocity: Velocity = Velocity(0f, 0f),
    var forces: MutableList<Force> = mutableListOf<Force>(Force(0f, 0f, 0f, 1f), Force(0f, 0f))
)

class Velocity(
    var speed: Float,
    var direction: Float,
)

class Force(var x: Float, var y: Float, var leftRot: Float = 0f, var rightRot: Float = 0f)

class Coordinate(val x: Float, val y: Float) // same as class Point() but with Floats instead of Ints