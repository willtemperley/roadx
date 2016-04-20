package org.roadlessforest.egbis

import java.awt.image.Raster

import org.junit.{Assert, Test}
import org.roadlessforest.TestResources

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

      println(idx)
      println(sub)

      Assert.assertEquals(coord, sub)
    }

  }
}
