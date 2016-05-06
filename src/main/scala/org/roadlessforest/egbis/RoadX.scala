package org.roadlessforest.egbis

import java.awt.geom.Point2D
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

import edu.princeton.cs.algorithms._
import org.roadlessforest.{ExportUtils, TestResources}

/**
  * Created by willtemperley@gmail.com on 14-Apr-16.
  */
object RoadX {

  val evergreen = 1
  val secondary = 2
  val regrowth  = 3
  val disturbed = 5

  def main(args: Array[String]) {

    //2 or 3 are the most likely to be roads (this could be determined empirically)

    val ras = TestResources.getTestRaster("rasters/africa_test.tif")

    /*
    Filtered to secondary and regrowth forest types
     */
    val tile = new GridGraph(ras, f => f == secondary || f == regrowth)

    val graph = new Digraph(tile.vertices.size)

    for (e <- tile.edges) {
      graph.addEdge(e.srcId.toInt, e.dstId.toInt)
    }

    val scc = new TarjanSCC(graph)

    val nComponents = scc.count()
    println(nComponents)

    /*
    Basic morphology
    Calculate the in-degree of the vertices (also out-degree?)
    */



    /*
    Extract
     */
    val img = new BufferedImage(ras.getWidth, ras.getHeight, BufferedImage.TYPE_USHORT_GRAY)
    val outRas = img.getRaster

    for (v <- 0 until graph.V) {
      val componentId = scc.id(v)
      val vertexId = tile.vertexIdToIndex(v)

      //fixme vertex id shouldn't be larger than the array size.
//      val pix = tile.pixels(vertexId.toInt)
//      if (!(pix == secondary || pix == regrowth)) {
//        println(pix)
//        throw new RuntimeException("there's something happening here, what it is ain't exactly clear")
//      }

      val sub: (Long, Long) = tile.ind2sub(vertexId)

      val array = Array(componentId + 1)

      //fixme why is the y index incorrect?
      val y = ras.getHeight - sub._1.toInt -1
      outRas.setPixel(sub._2.toInt, y, array)

    }



    //    ExportUtils.writeGeoTiff(outRas, new File("E:/tmp/roadx/scc.tif"))
    val orig = new Point2D.Double(-9.3417715 + (0.00026949 / 2), 5.76915976 + (0.00026949 / 2) - (0.00026949 * ras.getHeight))
    ImageIO.write(img, "TIFF", new File("E:/tmp/roadx/scc.tif"))
    ExportUtils.writeTFWfile(new File("E:/tmp/roadx/scc.tfw"), orig, 0.00026949)

  }


}
