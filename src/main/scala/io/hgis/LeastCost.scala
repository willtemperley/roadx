package io.hgis

import com.mxgraph.view.mxGraph
import org.apache.spark.graphx._
import org.apache.spark.graphx.lib.ShortestPaths
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

  val rows = 6
  val cols = 7

  //Just the manhattan distance for now
  def cost(a: Double, b: Double) = (a + b) / 2

  def sub2ind(r: Int, c: Int): VertexId = r * cols + c

  /*
  Grid graph
   */
  def main (args: Array[String]): Unit = {


    val vertexVals = sc.parallelize(TestResources.getTestCostSurface.map(s => s.split(",").map(_.toInt)).flatMap(f => f).toSeq)
    val vertexIdxs = sc.parallelize(0 until rows).flatMap( r => (0 until cols).map(c => (r,c)))
    val vertices = vertexIdxs.zip(vertexVals).map(v => (sub2ind(v._1._1, v._1._2), (v._1._1, v._1._2,v._2)))


    val edges: RDD[Edge[Double]] =
      vertices.flatMap{ case (vid, (r,c,v)) =>
        (if (r + 1 < rows) { Seq( (sub2ind(r, c), sub2ind(r + 1, c))) } else { Seq.empty }) ++
          (if (c + 1 < cols) { Seq( (sub2ind(r, c), sub2ind(r, c + 1))) } else { Seq.empty })
      }.map{ case (src, dst) => Edge(src, dst, 1.0) }


    val g = Graph(vertices, edges)
    val G = g.mapTriplets(f => cost(f.srcAttr._3, f.dstAttr._3))



//    G.edges.collect().foreach(println)

    val sp = ShortestPaths.run(G, Seq(1,5,7))

//    sp.vertices.collect().foreach(f => println(f))

    println(g.edges.count())
    println(G.edges.count())

    GraphVisualization.showGraph(G)
//    val sp = ShortestPaths.run(gridGraph, Seq(0,1))
//



  }



}
