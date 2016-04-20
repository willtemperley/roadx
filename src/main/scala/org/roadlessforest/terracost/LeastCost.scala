package org.roadlessforest.terracost

import edu.princeton.cs.algorithms.{DijkstraAllPairsSP, DirectedEdge, EdgeWeightedDigraph}
import org.roadlessforest.TestResources

/**
  * Created by willtemperley@gmail.com on 09-Mar-16.
  */
object LeastCost {


  def main(args: Array[String]): Unit = {

    /*
    a) Dijkstra from source s to edges of tile (SSSP).
    b) Dijkstra from all boundary vertices to each other (APSP)
    c)
    */


    val ras = TestResources.getTestRasterTile

    val tile = new Tile(ras)

    val edgeWeightedDigraph = new EdgeWeightedDigraph(tile.rows * tile.cols)
    for (e <- tile.edges) {
      edgeWeightedDigraph.addEdge(new DirectedEdge(e.srcId.toInt, e.dstId.toInt, e.attr))
    }

    val apsp = new DijkstraAllPairsSP(edgeWeightedDigraph)


    println(apsp.dist(0,1))


  }



}
