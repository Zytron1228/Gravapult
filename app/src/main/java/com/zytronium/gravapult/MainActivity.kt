package com.zytronium.gravapult

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.util.toRange
import androidx.core.view.forEach
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import java.util.*
import kotlin.math.*

class MainActivity : AppCompatActivity() {
    private lateinit var player: ImageView
    private lateinit var moon: ImageView
    private lateinit var planet: ImageView
    private lateinit var planet5: ImageView
    private lateinit var autoOrbiter: ImageView
    private lateinit var space: ConstraintLayout
    private lateinit var TVangle: TextView
    private lateinit var velocityIndicatorLine: View
    private var newAstSpawn: Coordinate = Coordinate(0f, 0f)

//    private var maxObstDist = 25
//    private var grav: Force = Force(0f, 0f)
//    private var velc = Force(0f, 0f, rightRot = 6f)
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
        planet5 = findViewById(R.id.planet5)
        autoOrbiter = findViewById(R.id.autoOrbiter)
        space = findViewById(R.id.space)
        velocityIndicatorLine = findViewById(R.id.vil)
        TVangle = findViewById(R.id.angleText)
        val windowInsetsController =
            ViewCompat.getWindowInsetsController(window.decorView) ?: return
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        windowInsetsController.hide(WindowInsetsCompat.Type.displayCutout()) //remove this if you still want the cutout
        window.setFlags( // needed because else it will show the cutout as white
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )

//        planet.tag = "physical"
        var earthPlanet = Thing(planet5, true, Force(-50f, 13f))
        var mn = Thing(moon, false, Force(0f, 0f)/*, mutableListOf(grav, Force(0f, 0f*//*, angle = 0f, strength = 0f*//*))*/)

        var plr = Thing(player, false/*, velc, mutableListOf(grav)*/)
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
//            plr.velocity = Force(0f, 0f, rightRot = 6f)
//                player.x = startPoint.x
//                player.y = startPoint.y
//                applyForces(plr)

//                mn.velocity = Force(0f, 0f, angle = calcOrbitAngle(moon, planet)+45f, strength = calcOrbitVelocity(moon, planet)*13.15F)
//                val velgrav = gravity(mn.view, planet)
//                mn.velocity.x += velgrav.x
//                mn.velocity.y += velgrav.y

                applyForces(earthPlanet)
//                applyForces(mn)
                simulateAutoOrbit(autoOrbiter, planet)
                rainbow(autoOrbiter, 10f, 30)
            } else recreate()
        }
        space.setOnTouchListener(onTouchListener())
        findStartPoint()

    }

    private fun simulateAutoOrbit(orbiter: View, parent: View) {
//        do {
            val newCoords = angleToCoords(
                Force(
                    0f,
                    0f,
                    angle = calcOrbitAngle(orbiter, parent, false),
                    strength = calcOrbitVelocity2(orbiter, parent) /7f
                )
            )
            orbiter.x += newCoords.x
            orbiter.y += newCoords.y
        Handler().postDelayed({
            simulateAutoOrbit(orbiter, parent)
        }, 2)
//        } while(simulating)

    }

    @SuppressLint("ClickableViewAccessibility")
    private fun onTouchListener(): View.OnTouchListener {
        return View.OnTouchListener { _, event ->
            val x = event.rawX
            val y = event.rawY

            when (event.action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_DOWN -> {
                    velocityIndicatorLine.visibility = View.VISIBLE
                    newAstSpawn = Coordinate(x, y)
                    velocityIndicatorLine.x = x
                    velocityIndicatorLine.y = y + velocityIndicatorLine.height/2

                }
                MotionEvent.ACTION_MOVE -> {
                    TVangle.text = "Angle: ${angleBetween(Coordinate(x, y), planet)}"
                    velocityIndicatorLine.visibility = View.VISIBLE
                    velocityIndicatorLine.layoutParams = ConstraintLayout.LayoutParams(calcDistanceBetween(newAstSpawn, Coordinate(x, y)).toInt(), velocityIndicatorLine.height)
                    velocityIndicatorLine.rotation = angleBetween(Coordinate(x, y), newAstSpawn )
                       //if(newAstSpawn.x > x) x else newAstSpawn.x// - velocityIndicatorLine.measuredWidth
                          //if(newAstSpawn.y > y) newAstSpawn.y + y/2  else y - newAstSpawn.y/2//- velocityIndicatorLine.width// - velocityIndicatorLine.measuredHeight/2
//                    velocityIndicatorLine.pivotX = 0f
                }
                MotionEvent.ACTION_UP -> {
                    velocityIndicatorLine.visibility = View.INVISIBLE
//                    velocityIndicatorLine.layoutParams = ConstraintLayout.LayoutParams(0, velocityIndicatorLine.height)
                    createAsteroid(newAstSpawn.x, newAstSpawn.y, Velocity((calcDistanceBetween(newAstSpawn, Coordinate(x, y))/17f), (angleBetweenFixed(newAstSpawn, Coordinate(x, y)))))
                }


            }

            true
        }
    }

    private fun createAsteroid(x: Float, y: Float, velocity: Velocity) {
        val asteroid = ImageView(this)
        val size = (player.measuredWidth*1.4).toInt() //((player.measuredWidth / 1.5).toInt()..(player.measuredWidth * 2.75).toInt()).random()
        asteroid.layoutParams = ConstraintLayout.LayoutParams(size, size)
        asteroid.x = x - size / 2f
        asteroid.y = y - size / 2f
//        asteroid.background = getColor(R.color.purple_500).toDrawable()
        asteroid.tag = "physical"
        asteroid.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.earth2))

        space.addView(asteroid)
        var ast = Thing(asteroid, true, Force(0f, 0f, 0f, 0f, velocity.direction, velocity.speed), mutableListOf(Force(0f, 0f), Force(0f, 0f)))
//        if (Random().nextBoolean() && !Random().nextBoolean())
//            rainbow(asteroid, 15f, 50) else
        asteroid.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.earth2))
        applyForces(ast)
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
        val oldVelocity = obj.velocity
            Handler().postDelayed({
        if(simulating) {
//            val x1 = i.x//+(i.width/2)
//            val y1 = i.y//+(i.height/2)
//            val p1 = Coordinate(x1, y1)
//                println(i.x.toString() + ", " + i.y.toString())
            val pgravity = gravity(i, planet)
            var totalGrav: MutableList<Force> = /*if(i != moon) mutableListOf() else*/ mutableListOf(pgravity)
            if(i != moon) {
                  space.forEach { view: View -> if(view.tag == "physical" && view != i)
                  totalGrav += gravity(i, view)
                  }
            }
            var gravity: Force = Force(0f, 0f)
            totalGrav.forEach { force: Force ->
                gravity.x += force.x
                gravity.y += force.y
//                rot -= force.leftRot
//                rot += force.rightRot
            }
//                grav = gravity(i, planet)//*
//                println(grav.toString())
                obj.forces.component1().x = gravity.x *1.25f // changed comp2 to 1
                obj.forces.component1().y = gravity.y *1.25f // changed comp2 to 1

                var totalX: Float = 0f
                var totalY: Float = 0f
                var rot = 0f
                obj.forces.forEach { force: Force ->
                    totalX += force.x
                    totalY += force.y
                    rot -= force.leftRot
                    rot += force.rightRot
                    if(/*force.angle != 0F || */force.strength != 0f) {
                        println("Force angle: ${force.angle}; Force strength: ${force.strength}")
                        if(force.angle !in (0f..360f)) println("Warning: Force angle is not between 0 and 360. Force angle = ${force.angle}")
                        println("angleForce: x: ${(force.strength * cos(force.angle))}, y: ${(force.strength * sin(force.angle))}")
                        totalX += angleToCoords(force = force).x
                        totalY += angleToCoords(force = force).y
                    }
                }
            //new velocity = old velocity + acceleration
            // accelleration = f/m
            if(oldVelocity.strength !=0f) {
                oldVelocity.x += (oldVelocity.strength * cos((oldVelocity.angle / 180f) * PI.toFloat()))
                oldVelocity.y += (oldVelocity.strength * sin((oldVelocity.angle / 180f) * PI.toFloat()))
                oldVelocity.strength = 0f
                oldVelocity.angle = 0f
            }
            val acclrtn = acceleration(Force(totalX/*-oldVelocity.x*/, totalY/*-oldVelocity.y*/, 0f, rot), i)
            val  accelForce = /*Coordinate(totalX, totalY)*/ Force(totalX*acclrtn, totalY*acclrtn, 0f, rot)
//            obj.velocity.speed = acclrtn
            val newVelocity = Force(oldVelocity.x+accelForce.x, oldVelocity.y+accelForce.y, oldVelocity.leftRot, oldVelocity.rightRot+rot)
                i.x += newVelocity.x/3 //oldVelocity.x + ((totalX-oldVelocity.x)*acclrtn)
                i.y -= newVelocity.y/3 //oldVelocity.y + ((totalY-oldVelocity.y)*acclrtn)
                i.rotation += newVelocity.rightRot/3
//                println(i.x.toString() + ", " + i.y.toString())
//            val x2 = i.x
//            val y2 = i.y
//            val p2 = Coordinate(x2, y2)
//            val accl2 = kotlin.math.sqrt(
//                            kotlin.math.abs(p1.x - p2.x).toDouble().pow(2) + kotlin.math.abs(p1.y - p2.y)
//                                .toDouble().pow(2)
//                        )
//            println("estimated acclrtn: $acclrtn; actual change in position: $accl2")
//            val oldVlcty = oldVelocity
//            oldVelocity = Force(totalX-oldVelocity.x, totalY-oldVelocity.y)
//                println("velocity: $vlty")
                obj.velocity = newVelocity
//                obj.velocity.y = newVelocity.y
//                obj.velocity.leftRot = velc.leftRot
//                obj.velocity.rightRot = velc.rightRot
            if(obj.view == moon) {
                obj.forces = mutableListOf(gravity(i, planet), /*Force(0f, 0f, angle = calcOrbitAngle(obj.vilanet)+0f, strength = calcOrbitVelocity(obj.view, planet)*16.5f)*//*, Force(gravity(i, planet).x*-1f, gravity(i, planet).y*-1f)*/)
                findViewById<TextView>(R.id.moonAngleText).text = "Moon Angle: ${calcOrbitAngle(obj.view, planet) +45f}"
//                var mai: ImageView = findViewById<ImageView>(R.id.moonAngleIndicator)
//                val angleCoords = angleToCoords(Force(0f, 0f, angle = calcOrbitAngle(obj.view, planet)+225f, strength = calcOrbitVelocity(obj.view, planet)*33.5f))
//                mai.x = (centerOf(moon).x) + angleCoords.x
//                mai.y = (centerOf(moon).y) + angleCoords.y
//                var mai2: ImageView = findViewById<ImageView>(R.id.moonAngleIndicator2)
////                val angleCoords = angleToCoords(Force(0f, 0f, angle = calcOrbitAngle(obj.view, planet)+15f, strength = calcOrbitVelocity(obj.view, planet)*33.5f))
//                mai2.x = (/*centerOf*/(findViewById<ImageView>(R.id.moon2)).x) + angleCoords.x
//                mai2.y = (/*centerOf*/(findViewById<ImageView>(R.id.moon2)).y) + angleCoords.y
//                var pai: ImageView = findViewById<ImageView>(R.id.planet2)
//                var pac: Coordinate = Coordinate(centerOf(planet).x - centerOf(moon).x, centerOf(planet).y - centerOf(moon).y)
//                pai.x = (/*centerOf*/(findViewById<ImageView>(R.id.moon2)).x) + ((pac.x/30f))
//                pai.y = (/*centerOf*/(findViewById<ImageView>(R.id.moon2)).y) + ((pac.y/30f))
//                val orbitForce = calcOrbitForce(obj.view, planet)
//                obj.forces.component2().x = orbitForce.x
//                obj.forces.component2().y = orbitForce.y
            } else {
//                obj.forces = mutableListOf(obj.forces.component1(), obj.forces.component2()/*, Force((oldVlcty.x + velc.x)/1.0525f, (oldVlcty.y + velc.y)/1.0525f)*/)
            }
                applyForces(obj)
            }
        }, 20)
        }

    private fun calcDistanceBetween(to: Coordinate, from: Coordinate): Float {
        return sqrt(abs(to.x - from.x).pow(2f) + abs(to.y - from.y).pow(2f))
    }

    private fun angleToCoords(force: Force): Coordinate {
        return Coordinate((force.strength * cos((force.angle/180f)*PI.toFloat())), (force.strength * sin((force.angle/180f)*PI.toFloat())) )
    }

    private fun calcOrbitForce(mn: View, plnt: View): Force {
        return Force(0f, 0f, angle = calcOrbitAngle(mn, plnt)/*+90f*/, strength = calcOrbitVelocity(mn, plnt)*1.5f)
    }

    private fun calcOrbitVelocity(mn: View, plnt: View): Float {
        val g = 6.2742f * 10f.pow(-11).toFloat()
        val m = plnt.width.toFloat()*plnt.height.toFloat()
        val moonCent: Coordinate = centerOf(mn)//Coordinate((mn.x+(mn.width/2)), (mn.y+(mn.height/2)))
        val planetCent: Coordinate = centerOf(plnt)//Coordinate((plnt.x+(plnt.width/2)), (plnt.y+(plnt.height/2)))
        val r = kotlin.math.sqrt(
                kotlin.math.abs(moonCent.x - planetCent.x).toFloat().pow(2) + kotlin.math.abs(moonCent.y - planetCent.y)
                    .toFloat().pow(2)
                ).toFloat()
//        val cent1 = Coordinate(obj1.x + (obj1.width/2), obj1.y + (obj1.height/2))
//        val cent2 = Coordinate(obj2.x + (obj2.width/2), obj2.y + (obj2.height/2))

        return sqrt((g*m) / (r/205f))*1175f /*.pow(2)*/

    }
    private fun calcOrbitVelocity2(mn: View, plnt: View): Float {
        val g = 6.2742f * 10f.pow(-11).toFloat()
        val m = plnt.width.toFloat()*plnt.height.toFloat()
        val moonCent: Coordinate = centerOf(mn)//Coordinate((mn.x+(mn.width/2)), (mn.y+(mn.height/2)))
        val planetCent: Coordinate = centerOf(plnt)//Coordinate((plnt.x+(plnt.width/2)), (plnt.y+(plnt.height/2)))
        val r = kotlin.math.sqrt(
                kotlin.math.abs(moonCent.x - planetCent.x).toFloat().pow(2) + kotlin.math.abs(moonCent.y - planetCent.y)
                    .toFloat().pow(2)
                ).toFloat()
//        val cent1 = Coordinate(obj1.x + (obj1.width/2), obj1.y + (obj1.height/2))
//        val cent2 = Coordinate(obj2.x + (obj2.width/2), obj2.y + (obj2.height/2))

        return sqrt((plnt.width/2f) / (r/2))*31.75f//PI.toFloat()    /*.pow(2)*/

    }


    private fun centerOf(view: View): Coordinate {
        return Coordinate((view.x+(view.width/2)), (view.y+(view.height/2)))
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
    private fun angleBetweenFixed(from: Coordinate, to: Coordinate): Float {

        val reltvPos2 =  Coordinate(from.x-to.x, to.y-from.y)
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
        val gForce = (gc*((mass1*mass2) / (dist/355f).pow(2.4f)).toFloat())*17.75f //1.33f
//        println("gforce: $gForce")
        return Force(((/*reltvPos2.x / */ (reltvPos2.x)) * (if(gForce > 1f) 1F else gForce) /*if(reltvPos2.x < 0) (gForce * -1) else gForce*/ ), ((/*reltvPos2.x / */(reltvPos2.y)) * (if (gForce > 1f) 1f else gForce))/*(if(reltvPos2.y < 0) (gForce) else (gForce * -1f))*/)


    }

    private fun acceleration(force: Force, obj: View): Float {
        val m = (obj.width * obj.height).toFloat()
        val f = kotlin.math.sqrt(
            kotlin.math.abs(force.x).pow(2) + kotlin.math.abs(force.y).pow(2)
        ).toFloat()
        return (f/(m/1.125f))
    }

    private fun rainbow(target: ImageView, hue: Float, speed: Long) {
        var h = hue
        if (h >= 360f) h = 0f
//        target.setBackgroundColor(Color.HSVToColor(floatArrayOf(h,100f,100f)))
        target.imageTintList = ColorStateList(arrayOf(intArrayOf()), intArrayOf(Color.HSVToColor(floatArrayOf(h,1f, 1f))))
        Handler(Looper.getMainLooper()).postDelayed({ if(target.visibility == View.VISIBLE) rainbow(target, (h + 2.5f), speed) }, (speed))
    }
}

class Thing(
    var view: View,
    var physical: Boolean = view.tag == "physical",
    var velocity: Force = Force(0f, 0f),
    var forces: MutableList<Force> = mutableListOf<Force>(Force(0f, 0f, 0f, 1f), Force(0f, 0f))
)
{
    fun mass() : Int { return (this.view.width * this.view.height) }
}

class Velocity(
    var speed: Float = 0f,
    var direction: Float = 0f
)

class Force(var x: Float, var y: Float, var leftRot: Float = 0f, var rightRot: Float = 0f, var angle: Float = 0F, var strength: Float = 0F)

class Coordinate(val x: Float, val y: Float) // same as class Point() but with Floats instead of Ints