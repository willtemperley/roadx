package org.roadlessforest.egbis

import java.awt.image.Raster

import org.junit.{Assert, Test}
import org.roadlessforest.TestResources
import org.roadlessforest.mxgraph.GraphVisualization

/**
  * Created by willtemperley@gmail.com on 18-Apr-16.
  */
class TestGridGraph {

  val raster = TestResources.getTestRaster("rasters/africa_test.tif")
  val gridGraph = new GridGraph(raster)

  @Test
  /*
  The gridgraph should be able to convert between image subindexes and vertex ids
   */
  def testIdxInverts(): Unit = {

//    val i = 100

    //some examples
    val coords  = Array(
      (220, 105),
      (21, 3),
      (55,4)
    )

    for (coord <- coords) {
      val idx = gridGraph.sub2ind(coord._1, coord._2)
      val sub = gridGraph.ind2sub(idx)

      println(coord)
      println(sub)

      Assert.assertEquals(coord, sub)
    }

  }

  @Test
  def smallImgTest(): Unit = {

    val ras = TestResources.getTestRaster("rasters/oblong.tif")
    /*
     */
    val tile = new GridGraph(ras, f => f != 1)

    GraphVisualization.showDigraph(tile)
//    for (pr <- tile.pixelRows) {
//      print(pr._2)
//      print("=")
//      print(pr._1)
//    }


  }
}
