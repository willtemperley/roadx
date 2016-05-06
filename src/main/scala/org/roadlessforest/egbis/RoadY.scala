package org.roadlessforest.egbis

import java.awt.geom.Point2D
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

import edu.princeton.cs.algorithms._
import org.roadlessforest.mxgraph.GraphVisualization
import org.roadlessforest.{ExportUtils, TestResources}

import scala.collection.immutable.IndexedSeq

/**
  * Created by willtemperley@gmail.com on 14-Apr-16.
  */
object RoadY {

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
    val tile = new GridGraph(ras, f => f != evergreen)

    val graph = new Digraph(tile.vertices.size)

    for (e <- tile.edges) {
      graph.addEdge(e.srcId, e.dstId)
    }

    val scc = new TarjanSCC(graph)

    val nComponents = scc.count()
    println(nComponents)


    /*
    Basic morphology
    Need the vertex degree vs the component id
    Then get the component to degree
    FIXME This should all be vectorized
     */
    val vertexToComponent: IndexedSeq[(VertexId, Int)]
    = (0 until graph.V).map(f => (f, scc.id(f)))

    val componentToDegree: IndexedSeq[(Int, Int)] = vertexToComponent.map(f => (f._2, tile.vertexDegree.getOrElse(f._1, 0)))

    val componentToDegreeAverage: Map[Int, Double] = componentToDegree.groupBy(_._1)
      .map(
        f => (f._1, f._2.map(_._2).sum.toDouble / f._2.size)
      )

    /*
    Extract
     */
    val img = new BufferedImage(ras.getWidth, ras.getHeight, BufferedImage.TYPE_USHORT_GRAY)
    val outRas = img.getRaster

    for (v <- 0 until graph.V) {
      val componentId = scc.id(v)

      val vertexId = tile.vertexIdToIndex(v)

      val sub: (Long, Long) = tile.ind2sub(vertexId)

      val degreeAverage = componentToDegreeAverage(componentId)
      if (degreeAverage < 9.5) {
//          val array = Array((degreeAverage * 10).toInt)
          val array = Array(tile.vertexDegree.getOrElse(v, 1000))
          //fixme why is the y index incorrect?
          val y = ras.getHeight - sub._2.toInt -1
          outRas.setPixel(sub._1.toInt, y, array)
      }
    }


//    GraphVisualization.showDigraph(tile)


//    val x: IndexedSeq[((Long, Long), Int)] = (0 until graph.V).map(v => (tile.ind2sub(tile.idToVertex(v)), scc.id(v)))

    //need to get edges
//    (0 until graph.E).map()


    //    ExportUtils.writeGeoTiff(outRas, new File("E:/tmp/roadx/scc.tif"))
    val orig = new Point2D.Double(-9.3417715, 5.76915976 - (0.00026949 * ras.getHeight))
    ImageIO.write(img, "TIFF", new File("E:/tmp/roadx/scc7.tif"))
    ExportUtils.writeTFWfile(new File("E:/tmp/roadx/scc7.tfw"), orig, 0.00026949, pixShift = true)

  }


}
