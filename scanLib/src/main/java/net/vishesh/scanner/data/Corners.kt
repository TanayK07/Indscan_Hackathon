package net.vishesh.scanner.data

import android.util.Log
import org.opencv.core.Point
import org.opencv.core.Size

data class Corners(val points: List<Point>, var size: Size) {
    fun log() {

        Log.d(
            javaClass.simpleName,
            "ERROR A size: ${size.width}x${size.height} - tl: ${tl.x}, ${tl.y} - tr: ${tr.x}, ${tr.y} - br: ${br.x}, ${br.y} - bl: ${bl.x}, ${bl.y}"
        )
    }
    fun pointsToPair() : List<Pair<Double, Double>> {
        return listOf(Pair(points[0].x, points[0].y),Pair(points[1].x, points[1].y),Pair(points[2].x, points[2].y),Pair(points[3].x, points[3].y))
    }

    val tl: Point = points[0]
    val tr: Point = points[1]
    val br: Point = points[2]
    val bl: Point = points[3]
}
