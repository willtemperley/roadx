package org.roadlessforest.graphx

import org.apache.spark.graphx.{VertexId, _}

/**
 * Computes shortest paths to the given set of landmark vertices, returning a graph where each
 * vertex attribute is a map containing the shortest-path distance to each reachable landmark.
 */
object ShortestPaths {

  /**
   * Computes shortest paths to the given set of landmark vertices.
   *
   * @param graph the graph for which to compute the shortest paths
   * @param landmarks the list of landmark vertex ids. Shortest paths will be computed to each
   * landmark.
   *
   * @return a graph where each vertex attribute is the shortest-path distance to the nearest landmark vertex
   *
   */
  def run(graph: Graph[(Int, Int, Int), Double], landmarks: Seq[VertexId]): Graph[Double, Double] = {
    val spGraph = graph.mapVertices { (vid, attr) =>
      if (landmarks.contains(vid)) 0 else Double.PositiveInfinity
    }

    val initialMessage = Double.PositiveInfinity

    def vertexProgram(id: VertexId, attr: Double, msg: Double): Double = math.min(attr, msg)

    def sendMessage(triplet: EdgeTriplet[Double, Double]): Iterator[(VertexId, Double)] = {
      if (triplet.srcAttr + triplet.attr < triplet.dstAttr) {
        Iterator((triplet.dstId, triplet.srcAttr + triplet.attr))
      } else {
        Iterator.empty
      }
    }

    Pregel(spGraph, initialMessage)(vertexProgram, sendMessage, (a,b) => math.min(a,b))
  }
}
