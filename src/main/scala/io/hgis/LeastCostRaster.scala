package io.hgis

import java.awt.image.{DataBufferInt, DataBufferByte}

import org.apache.spark.graphx._
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}


/**
 * Created by willtemperley@gmail.com on 17-Aug-15.
 *
 */
object LeastCostRaster {

  // The edge cost is the average of the vertex costs, multiplied by sqrt2 if it's diagonal
  def cost(a: Double, b: Double, diagonal: Boolean) = {
    val dist = if(diagonal) math.sqrt(2) else 1
    ((a + b) / 2) * dist
  }

  def sub2index(cols: Int)(r: Int, c: Int): VertexId = r * cols + c

  /*
  Grid graph
   */
  def main (args: Array[String]): Unit = {

    val conf = new SparkConf().setMaster("local[8]").setAppName("test-local")

    val sc   = new SparkContext(conf)

    //Get raster
    val raster = TestResources.getTestTiffRaster
    val rows = raster.getHeight
    val cols = raster.getWidth

    def sub2ind = sub2index(cols) _

    //Retrieve all pixels
    val pixels = raster.getPixels(0,0, cols, rows, new Array[Int](rows * cols))

    //Group pixels by row and zip up with their row number
    val pixelRows = pixels.grouped(cols).zipWithIndex

    // the vertex values are row, column and cost value
    val vertices: RDD[(VertexId, (Int, Int, Int))] =
      sc.parallelize(pixelRows.flatMap {case(row, rowIdx) => row.zipWithIndex.map{case (costVal, colIdx) => (sub2ind(rowIdx, colIdx), (rowIdx, colIdx, costVal))}}.toSeq)

    /*
    Creates sequence of the edge and its reverse ... TODO build whole edge instead?
     */
    def getVertexIds(r0: Int, c0: Int, r1: Int, c1: Int): Seq[(VertexId, VertexId, Boolean)] = {
      val idx0 = sub2ind(r0, c0)
      val idx1 = sub2ind(r1, c1)
      val diag = (r0 != r1) && (c0 != c1)
      Seq((idx0, idx1, diag), (idx1, idx0, diag))
    }

    val edges: RDD[Edge[Boolean]] =
      vertices.flatMap{ case (vid, (r,c,v)) =>
        (if (r + 1 < rows)
          { getVertexIds(r, c, r + 1, c) } else { Seq.empty }) ++
        (if (c + 1 < cols)
          { getVertexIds(r, c, r, c + 1) } else { Seq.empty }) ++
        (if (c + 1 < cols && r + 1 < rows)
          { getVertexIds(r, c, r + 1, c +1 ) } else { Seq.empty }) ++
        (if (c - 1 >= 0 && r + 1 < rows)
          { getVertexIds(r, c, r + 1, c - 1) } else { Seq.empty })

          //TODO remove this and build edge directly instead of getVertexIds
      }.map{ case (src, dst, diag) => Edge(src, dst, diag) }


    val g = Graph(vertices, edges)
    val G = g.mapTriplets(f => cost(f.srcAttr._3, f.dstAttr._3, f.attr))

    val sp = ShortestPaths.run(G, Seq(10000, 20000))

//    GraphVisualization.showGraph(G)

  }

}
