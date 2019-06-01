package com.example.displaydata

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.example.displaydata.R
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.LineData

import com.github.mikephil.charting.charts.LineChart
import android.support.v4.content.ContextCompat
import kotlin.concurrent.fixedRateTimer

val MIN: Int = 0
val MAX: Int = 6
val CHART_X_GIRTH_INCREMENT = 1
val CHART_X_SHIFT_MAX = 15
val TICK_RATE: Long = 300

class HandleCharting {



    constructor(chart: LineChart) {

        chart.setBorderColor(-16777216)
        // chart.setBackgroundColor(Con) // -16777216
        var baseline = 0

        var seriesList = getSeries(baseline)

        var dataSet = LineDataSet(seriesList, "Label")
        dataSet.color = -16777216
        var lineData = LineData(dataSet)
        chart.data = lineData
        chart.invalidate(); // refresh

        fixedRateTimer("default", false, 0L, TICK_RATE){
            baseline += MAX
            seriesList?.let { list1 -> getSeries(baseline)?.let(list1::addAll) }
            if (seriesList.size > CHART_X_SHIFT_MAX){
                seriesList = seriesList.drop(CHART_X_GIRTH_INCREMENT).toMutableList()
            }

            dataSet = LineDataSet(seriesList, "Label")
            lineData = LineData(dataSet)
            dataSet.color = -16777216
            chart.data = lineData
            chart.notifyDataSetChanged()
            chart.invalidate(); // refresh



        }
    }

    private fun getSeries(baseline: Int): MutableList<Entry> {

        val dataObjects: MutableList<Point> = arrayListOf()


        for (x in 0 until CHART_X_GIRTH_INCREMENT) dataObjects.add(Point(x + baseline))


        val entries: MutableList<Entry> = ArrayList()
        for (data: Point in dataObjects) {
            // turn your data into Entry objects
            entries.add(Entry(data.getXValue().toFloat(), data.getYValue().toFloat()));
        }

        return entries


    }

    class Point {

        var x: Int = 0

        var y: Int = 0

        constructor(seriesNo: Int) {

            setup(seriesNo)

        }

        constructor() {

            setup(0)

        }

        fun setup(seriesNo: Int) {


            this.y = MIN + (Math.random() * ((MAX - MIN) + 1)).toInt()
            this.x = seriesNo

        }

        fun getXValue(): Int {return x}
        fun getYValue(): Int {return y}

    }

}