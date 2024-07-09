package com.example.delaunay

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.constraintlayout.helper.widget.MotionEffect
import androidx.constraintlayout.widget.Constraints
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import org.apache.commons.io.FileUtils
import java.io.BufferedReader
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.util.Arrays
import java.util.Collections
import java.util.function.UnaryOperator
import java.util.stream.IntStream

class Utils {

    private val STORAGE_PERMISSION_CODE = 1
    var new_finallist= ArrayList<Double>()
    var DIRECTORY_NAME = "/LandXmlFile"

    fun readAnyfile(uri: Uri, context: Context): List<String> {
        val csvFile = context.contentResolver.openInputStream(uri)
        val isr = InputStreamReader(csvFile)
        return BufferedReader(isr).readLines()
    }

    fun requestStoragePermission(context : Context) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                context as Activity,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                STORAGE_PERMISSION_CODE
            )
        } else {
            // Permission already granted, perform your file operations here
        }
    }

    fun NewSplitDataLandXml(list: ArrayList<String>): ArrayList<Double> {
        val temp_facepoint = java.util.ArrayList<Int>()
        for (item in list) {
            val splitData = item.split(", ".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()
            //            Log.d(TAG, "readText:splitData ==" + Arrays.toString(splitData));
            for (input in splitData) {
                // Find the start and end indexes of <P> tag content
                val startIdx = input.indexOf(">")
                //                int endIdx = input.lastIndexOf("<");
                val endIdx = input.lastIndexOf("</P")
                // Check if <P> tag content was found
                if (startIdx != -1 && endIdx != -1 && startIdx < endIdx) {
                    val content = input.substring(startIdx + 1, endIdx)
                    // Split the content into an array of strings using space as the delimiter
                    val values = content.split(" ".toRegex()).dropLastWhile { it.isEmpty() }
                        .toTypedArray()
                    val easting = values[0].toDouble()
                    val northing = values[1].toDouble()
                    val elevation = values[2].toDouble()
                    new_finallist.add(easting)
                    new_finallist.add(northing)
                    new_finallist.add(elevation)
//                    System.out.println("Easting ="+Easting +"=Northing="+ Northing +"=Elevation="+Elevation);
                } else {
//                    println("Invalid input format: ")
                }
                val FaceendIdx = input.lastIndexOf("</F>")
                if (startIdx != -1 && FaceendIdx != -1 && startIdx < FaceendIdx) {
                    val content = input.substring(startIdx + 1, FaceendIdx)
                    // Split the content into an array of strings using space as the delimiter
                    val values = content.split(" ".toRegex()).dropLastWhile { it.isEmpty() }
                        .toTypedArray()
                    val P1 = values[0].toInt()
                    val P2 = values[1].toInt()
                    val P3 = values[2].toInt()
                    temp_facepoint.add(P1)
                    temp_facepoint.add(P2)
                    temp_facepoint.add(P3)
                    //                    System.out.println("P1 ="+P1 +"=P2="+ P2 +"=P3="+P3);
                } else {
//                    System.out.println("Invalid input format: ");
                }
            }
        }
        return new_finallist
    }

    fun ReadLandXml(list: MutableList<String>): ArrayList<Double> {
        val splitDataList = list.map { it.split(", ") }
        for (splitData in splitDataList) {
            Log.d(TAG, "readText:splitData =="+splitData)
            val identi = splitData[0]
            val values = identi.split("\\s+".toRegex())
            if(values.isNotEmpty() && values.contains("<P id=")){
                val identifier = values[0]
                val Northing = values[1].toDouble()
                val Easting = values[2].toDouble()
                val Elevation = values[3].toDouble()
                new_finallist.add(Northing)
                new_finallist.add(Easting)
                new_finallist.add(Elevation)
                Log.d(ContentValues.TAG, "readsplitdata:=="+"identifier = "+identifier+"Northing = "+Northing+"Easting = "+Easting+"Elevation = "+Elevation)
            }
        }
        return new_finallist
    }

    fun writecalFile(text: String?,context: Context) {
        val externalFileDir = context.getExternalFilesDir(null)
        val dir = File(externalFileDir!!.absolutePath + File.separator + "projects" + File.separator)
        val root = File(dir, DIRECTORY_NAME)
        //val dir = File(Environment.getExternalStorageDirectory(), "/DesignFiles")
        val tstamp = System.currentTimeMillis()
        val fileName = "LandXML_$tstamp.xml" +
                ""
        val calGen = File(root, fileName)
        if (!root.exists()) {
            try {
                root.mkdirs()
            } catch (e: java.lang.Exception) {
            }
        }
        try {
            FileUtils.write(calGen, text, Charset.defaultCharset())
            Toast.makeText(context, "Land XML File Generated", Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        }
    }

    fun writeArrayListsToCSV(context: Context, arrayList1: ArrayList<Double>, arrayList2: ArrayList<Double>, arrayList3: ArrayList<Double>, fileName: String) {
        val csvFile = File(context.getExternalFilesDir(null), fileName)

        try {
            val fileWriter = FileWriter(csvFile)

            // Write header if needed
            fileWriter.append("Northing,Easting,Elevation\n")

            for (i in 0 until maxOf(arrayList1.size, arrayList2.size, arrayList3.size)) {
                val value1 = if (i < arrayList1.size) arrayList1[i] else ""
                val value2 = if (i < arrayList2.size) arrayList2[i] else ""
                val value3 = if (i < arrayList3.size) arrayList3[i] else ""

                fileWriter.append("$value1,$value2,$value3\n")
            }
            fileWriter.flush()
            fileWriter.close()
            Toast.makeText(context, "CSV File Generated", Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

}