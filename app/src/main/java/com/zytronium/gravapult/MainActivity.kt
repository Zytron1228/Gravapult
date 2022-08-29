package com.zytronium.gravapult

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.ImageView
import kotlin.math.pow

class MainActivity : AppCompatActivity() {
    private lateinit var player: ImageView
    private lateinit var planet: ImageView
    private var maxObstDist = 25
    private var grav: Force = Force(0f, 0f)
    private var totalForce: Array<Force> = arrayOf(Force(40f, -30f, rightRot = 8f), grav)
    private var simulating = false
    private var startPoint: Coordinate = Coordinate(0f, 0f)
    private var found = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        player = findViewById(R.id.player)
        planet = findViewById(R.id.planet)
        println("x: " + player.x.toString() + " y: " +  player.y.toString())
        planet.setOnClickListener {
            simulating = !simulating
            if(simulating) {
                player.x = startPoint.x
                player.y = startPoint.y
                applyForces()
            }
        }
        findStartPoint()

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

    private fun applyForces() {
//        do {
//        Timer().scheduleAtFixedRate(1500, 500) {
            Handler().postDelayed({
        if(simulating) {
                println(player.x.toString() + ", " + player.y.toString())
                grav = gravity(player, planet)
                println(grav.toString())
                totalForce.component2().x = grav.x
                totalForce.component2().y = grav.y

                var totalX: Float = 0f
                var totalY: Float = 0f
                var rot = 0f
                totalForce.forEach { force: Force ->
                    totalX += force.x
                    totalY += force.y
                    rot -= force.leftRot
                    rot += force.rightRot
                }
                player.x += totalX / 3
                player.y += totalY / 3
                player.rotation += rot
                println(player.x.toString() + ", " + player.y.toString())
                applyForces()
            }
        }, 50)
//        } while(true)
//    }
        }

    private fun gravity(obj1: View, obj2: View): Force {
        var mass1 = obj1.width * obj1.height
        var mass2 = obj2.width * obj2.height
        var cent1 = Coordinate(obj1.x + (obj1.width/2), obj1.y -(obj1.height/2))
        var cent2 = Coordinate(obj2.x + (obj2.width/2), obj2.y -(obj2.height/2))
        var dist = kotlin.math.sqrt(
                            kotlin.math.abs(cent1.x - cent2.x).toDouble().pow(2) + kotlin.math.abs(cent1.y - cent2.y)
                                .toDouble().pow(2)
                        )
        var reltvPos2 =  Coordinate(cent2.x-cent1.x, cent2.y+cent1.y)
        var gc = 6.6742f
        var gForce = gc*((mass1*mass2) / (dist*10).pow(2)).toFloat()/1.5f
        println("gforce: $gForce")
        return Force(((reltvPos2.x)/(reltvPos2.x/2)) * if(reltvPos2.x < 0) gForce* -1 else gForce, ((reltvPos2.x)/(reltvPos2.y/2))*if(reltvPos2.y < 0) gForce* -1 else gForce)


    }

    fun acceleration(force: Force, speed: Float) : Float {
        var accl = 0f
        return accl
    }

}

class Force(var x: Float, var y: Float, var leftRot: Float = 0f, var rightRot: Float = 0f)

class Coordinate(val x: Float, val y: Float) // same as class Point() but with Floats instead of Ints