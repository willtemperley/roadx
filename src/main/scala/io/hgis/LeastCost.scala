package io.hgis

import org.apache.spark.graphx._
import org.apache.spark.graphx.util.GraphGenerators
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

// To make some of the examples work we will also need RDD

/**
 * Created by willtemperley@gmail.com on 17-Aug-15.
 *
 */
object LeastCost {

  val conf = new SparkConf().setMaster("local").setAppName("test-local")
  val sc   = new SparkContext(conf)

  //Just the manhattan distance for now
  def cost(a: Double, b: Double) = (a + b) / 2

  /*
  Grid graph
   */
  def main (args: Array[String]): Unit = {

    val rows = 7
    val cols = 7

    def sub2ind(r: Int, c: Int): VertexId = r * cols + c
    val lines = TestResources.getTestCostSurface.map(s => s.split(",").map(_.toInt))

    val m = lines.flatMap(f => f).toList


    val gridGraph = GraphGenerators.gridGraph(sc, 4, 4)
////    val g2 = gridGraph.mapEdges(e => 3.0)
//
//    val sp = ShortestPaths.run(gridGraph, Seq(0,1))
//
//    sp.vertices.collect().foreach(f => println(f._2))



  }

  def gridGraph(sc: SparkContext, rows: Int, cols: Int): Graph[(Int,Int), Double] = {
    // Convert row column address into vertex ids (row major order)
    def sub2ind(r: Int, c: Int): VertexId = r * cols + c

    val vertices: RDD[(VertexId, (Int,Int))] =
      sc.parallelize(0 until rows).flatMap( r => (0 until cols).map( c => (sub2ind(r,c), (r,c)) ) )
    val edges: RDD[Edge[Double]] =
      vertices.flatMap{ case (vid, (r,c)) =>
        (if (r + 1 < rows) { Seq( (sub2ind(r, c), sub2ind(r + 1, c))) } else { Seq.empty }) ++
          (if (c + 1 < cols) { Seq( (sub2ind(r, c), sub2ind(r, c + 1))) } else { Seq.empty })
      }.map{ case (src, dst) => Edge(src, dst, 1.0) }
    Graph(vertices, edges)
  }

}
