package org.roadlessforest.egbis

import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

import edu.princeton.cs.algorithms._
import edu.princeton.cs.introcs.{In, StdOut}
import org.apache.spark.graphx.{Edge, VertexId}
import org.roadlessforest.TestResources
import org.roadlessforest.terracost.Tile

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

//    val ans = tile.edges.count(f => true)

    val graph = new Digraph(tile.nV)

    for (e <- tile.edges) {
      graph.addEdge(e.srcId.toInt, e.dstId.toInt)
    }

    val scc = new TarjanSCC(graph)

    val nComponents = scc.count()

    println(nComponents)

    /*
    Extract
     */
    val img: BufferedImage = new BufferedImage(ras.getWidth, ras.getHeight, BufferedImage.TYPE_USHORT_GRAY)
    val outRas = img.getRaster
//    val testVals: Array[Int] = (0 until (ras.getWidth * ras.getHeight)).map(f=> 101).toArray
//    outRas.setPixels(0, 0, ras.getWidth, ras.getHeight, testVals)

    for (v <- 0 until graph.V) {
      val componentId = scc.id(v)
      val sub: (Long, Long) = tile.ind2sub(tile.idToVertex(v))

      val array = Array(componentId+1)
      //fixme why??
      outRas.setPixel(sub._2.toInt, sub._1.toInt, array)

    }

    ImageIO.write(img, "TIFF", new File("E:/tmp/outras4.tif"))

    // Iterator[(Int, VertexId)]
//    val z = tile.vertexList.map()


//    (0 until tile.nV) // vertices




    //      edgeWeightedDigraph.addEdge(new DirectedEdge(e.srcId.toInt, e.dstId.toInt, e.attr))
//    scc.

//    val e = edgeWeightedDigraph.
//    val n = e.from()
//
//    graph.
//    val dfs = new DepthFirstSearch(graph, )
//    val edge: Edge[Double] = tile.edges.next()

//    val x = new
  }


}
