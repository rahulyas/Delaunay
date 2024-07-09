package com.example.delaunay

import android.content.ContentValues.TAG
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.delaunay.databinding.ActivityMainBinding
import com.example.delaunay.triangularization.Delaunay
import triangularization.Triangle2D
import java.text.SimpleDateFormat
import java.util.Collections
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding :ActivityMainBinding
    var calFileString: StringBuilder = StringBuilder()
    private val PICK_TEXT = 101
    var fileuri: Uri? = null
    var list = ArrayList<String>()
    var new_finallist= ArrayList<Double>()
    var PointNorthinglist = ArrayList<Double>()
    var PointEastinglist = ArrayList<Double>()
    var PointElevationlist = ArrayList<Double>()
    var trianglelist: List<Triangle2D>? = null
    var idlist = ArrayList<Int>()
    var pointList: ArrayList<Point> = ArrayList()
    var FaceNorthinglist = ArrayList<Double>()
    var FaceEastinglist = ArrayList<Double>()
    var FaceElevationlist = ArrayList<Double>()
    var minX = 0.0
    var maxX = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Utils().requestStoragePermission(this)
        binding.load.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "*/*"
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            startActivityForResult(Intent.createChooser(intent, "Select CSV file"), PICK_TEXT)
        }

        binding.save.setOnClickListener {
/*            create_landXml()
            Utils().writecalFile(calFileString.toString(),this)*/
            val fileName = "data.csv"
            Utils().writeArrayListsToCSV(this, PointNorthinglist, PointEastinglist, PointElevationlist, fileName)
        }

    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_TEXT && data != null) {
            fileuri = data.data
            list = Utils().readAnyfile(fileuri!!,this) as ArrayList<String>
            new_finallist = Utils().NewSplitDataLandXml(list)
            finalPoint(new_finallist)
            /*trianglelist = Delaunay.doDelaunayFromGit(new_finallist)
            val list = Delaunay.addHight(trianglelist!!, new_finallist)
            Log.d(TAG, "readNorthingEasting:list == "+list)
            Log.d(TAG, "onActivityResult: delaunayTriangulator == "+trianglelist)
            finalTrianglePoint()
            var i = 0
            while (i < list.size) {
                val xCoordinate = list[i + 0]
                val yCoordinate = list[i + 1]
                val zCoordinate = list[i + 2]
                val id = findPointIdByCoordinates(xCoordinate,yCoordinate,zCoordinate)
                idlist.add(id)
                Log.d(TAG, "id: "+id)
                i = i + 3
            }
            readfaceCoordinates()*/
        }
    }
    fun finalTrianglePoint() {
        var i = 0
        var id = 1
        while (i < new_finallist.size) {
            PointNorthinglist.add(new_finallist[i + 0])
            PointEastinglist.add(new_finallist[i + 1])
            PointElevationlist.add(new_finallist[i + 2])
            val p = Point(new_finallist[i + 0], new_finallist[i + 1], new_finallist[i + 2], id++)
            pointList.add(p)
            i = i + 3
        }
    }

    fun findPointIdByCoordinates(x: Double, y: Double,z: Double): Int {
        val foundPoint = pointList.find { it.x == x && it.y == y  && it.z == z}
        return foundPoint!!.id
    }

    fun readfaceCoordinates(){
        var j = 0
        while (j < idlist.size) {
            val xCoordinate = idlist[j + 0]
            val yCoordinate = idlist[j + 1]
            val zCoordinate = idlist[j + 2]
            FaceNorthinglist.add(xCoordinate.toDouble())
            FaceEastinglist.add(yCoordinate.toDouble())
            FaceElevationlist.add(zCoordinate.toDouble())
            j = j + 3
        }
        Log.d(TAG, "FaceNorthinglist =="+FaceNorthinglist)
    }

    fun create_landXml(){
        minX = Collections.min(PointElevationlist)
        maxX = Collections.max(PointElevationlist)
        val currentDate: String = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(
            Date()
        )
        calFileString.append(
            "<?xml version=\""+1.0+"\" encoding=UTF-\""+8+"\"?>\r\n" +
                    "<LandXML version=\""+1.2+"\"    >\r\n" +
                    "  <Units>\r\n"+
                    "    <Metric linearUnit=\"meter\" widthUnit=\"meter\" heightUnit=\"meter\" diameterUnit=\"meter\" areaUnit=\"squareMeter\" volumeUnit=\"cubicMeter\" temperatureUnit=\"celsius\" pressureUnit=\"HPA\" angularUnit=\"decimal degrees\" directionUnit=\"decimal degrees\" />\r\n"+
                    "  </Units>\r\n"+
                    "  <Application name=\"GEOMaster\" manufacturer=\"Apogee Gnss\" version=\"37.0.8236.15475\" timeStamp=\""+currentDate+"\">\r\n"+
                    "    <Author createdBy=\"createdByName\" timeStamp=\""+currentDate+"\" />\r\n"+
                    "  </Application>\r\n"+
                    "  <Surfaces>\r\n" +
                    "    <Surface name=\"MCW (Finish)\">\r\n" +
                    "      <Definition surfType=\"TIN\" elevMax=\""+minX+"\" elevMin=\""+maxX+"\">\r\n" +
                    "        <Pnts>\r\n"
        )
        Log.d(TAG, "create_landXml: PointNorthinglist"+PointNorthinglist.size+"=="+PointEastinglist.size+"=="+PointElevationlist.size)
        for (i in PointNorthinglist.indices){
            val n= i+1
            calFileString.append(
                "          <P id=\""+n+"\">"+PointNorthinglist[i]+" "+PointEastinglist[i]+" "+PointElevationlist[i]+"</P>\r\n"
            )
        }
        calFileString.append(
            "        </Pnts>\r\n" +
                    "        <Faces>\r\n"
        )
        for (i in FaceNorthinglist.indices){
            calFileString.append(
                "          <F>"+FaceNorthinglist[i].toInt()+" "+FaceEastinglist[i].toInt()+" "+FaceElevationlist[i].toInt()+"</F>\r\n"
            )
        }
        calFileString.append(
            "        </Faces>\r\n" +
                    "        <Feature code=\"ApogeeGNSS\">\r\n"+
                    "          <Property label=\"color\" value=\"128,128,128\" />\r\n"+
                    "        </Feature>\r\n"+
                    "      </Definition>\r\n"+
                    "    </Surface>\r\n"+
                    "  </Surfaces>\r\n"+
                    "</LandXML>\r\n"
        )

        Log.d(TAG, "create_landXml: calFileString"+calFileString)
        Toast.makeText(this,"Now You Can Save Your Land Xml file ", Toast.LENGTH_SHORT).show()
    }

    /**
     *
      */
    fun finalPoint(new_finallist:ArrayList<Double>) {
        var i = 0
        while (i < new_finallist.size) {
            PointNorthinglist.add(new_finallist[i + 0])
            PointEastinglist.add(new_finallist[i + 1])
            PointElevationlist.add(new_finallist[i + 2])
            i = i + 3
        }
    }

}