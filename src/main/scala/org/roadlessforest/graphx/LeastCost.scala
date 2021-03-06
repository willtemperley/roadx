package org.roadlessforest.graphx

import org.apache.spark.graphx._
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}
import org.roadlessforest.TestResources
import org.roadlessforest.mxgraph.GraphVisualization

// To make some of the examples work we will also need RDD

/**
 * Created by willtemperley@gmail.com on 17-Aug-15.
 *
 */
object LeastCost {

  val conf = new SparkConf().setMaster("local").setAppName("test-local")
  val sc   = new SparkContext(conf)

  val rows = 6
  val cols = 7

  //Just the manhattan distance for now
  def cost(a: Double, b: Double, diagonal: Boolean) = {
    val dist = if(diagonal) math.sqrt(2) else 1
    ((a + b) / 2) * dist
  }

  def sub2ind(r: Int, c: Int): VertexId = r * cols + c

  /*
  Grid graph
   */
  def main (args: Array[String]): Unit = {

    val vertexVals = sc.parallelize(TestResources.getTestCostSurface.map(s => s.split(",").map(_.toInt)).flatMap(f => f).toSeq)
    val vertexIdxs = sc.parallelize(0 until rows).flatMap( r => (0 until cols).map(c => (r,c)))
    val vertices = vertexIdxs.zip(vertexVals).map(v => (sub2ind(v._1._1, v._1._2), (v._1._1, v._1._2, v._2)))

    /*
    Creates sequence of the edge and its reverse
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

      }.map{ case (src, dst, diag) => Edge(src, dst, diag) }


    val g = Graph(vertices, edges)
    val G = g.mapTriplets(f => cost(f.srcAttr._3, f.dstAttr._3, f.attr))

    val sp = ShortestPaths.run(G, Seq(0, 33))

    GraphVisualization.showGraph(G)

    val spV = sp.vertices.collect()


    spV.sortBy(f => f._1).map(f => f._2).grouped(7).foreach(f => {
      println(f.map(math.round).mkString(","))
    })

//    val sp = ShortestPaths.run(gridGraph, Seq(0,1))
  }

}
