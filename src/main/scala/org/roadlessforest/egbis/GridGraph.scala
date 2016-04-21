package org.roadlessforest.egbis

import java.awt.image.Raster

import org.apache.spark.graphx.{Edge, VertexId}

import scala.collection.JavaConversions.mapAsScalaMap

/**
  * Created by willtemperley@gmail.com on 09-Mar-16.
  */

class GridGraph(raster: Raster, includePixel: (Int => Boolean) = (x: Int) => true) {


  /*
  Array subscripts to position in array
   */
  def sub2index(cols: Int)(r: Int, c: Int): VertexId = c * cols + r

  def index2sub(cols: Int)(v: VertexId): (Long, Long) =  {
    val x = v % cols //
    val y = (v - x) / cols

    (x, y)
  }

  val rows = raster.getHeight
  val cols = raster.getWidth

  /*
  The array index of a pixel given a row and column and the inverse
   */
  def sub2ind = sub2index(cols) _
  def ind2sub = index2sub(cols) _

  /*
  Retrieve all pixels
   */
  val pixels: Array[Int] = raster.getPixels(0,0, cols, rows, new Array[Int](rows * cols))

  /*
  Group pixels by row and zip up with their row number
   */
  val pixelRows = pixels.grouped(cols).zipWithIndex

  /*
  the vertex values are row, column and cost value
   */
  val vertices: Seq[(VertexId, (Int, Int, Int))] = pixelRows
    .flatMap {case(row, rowIdx) => row.zipWithIndex //zips with the column idx
    .map {case (costVal, colIdx) => (sub2ind(rowIdx, colIdx), (rowIdx, colIdx, costVal))}}
    .toSeq

  /*
  Vertex id to new vertex id
  */
  val vertexToID: Map[VertexId, Int] =
    vertices
      .filter(f => includePixel(f._2._3))
      .map(_._1).zipWithIndex.toMap

  /*
  Number of interesting vertices
   */
  val nV = vertexToID.size

  /*
  Flip the map
   */
  val idToVertex = vertexToID.map(_.swap)

  //FIXME need to get VALUE for id
//  def getValueForId(id: Int) = ind2sub(vertexToID.get(id))
//  def

//  println(pixMap)

//  val pixMap2 =
//    vertices.zipWithIndex

//      .filter(f => includePixel(f._2._3))
//      .map(_._1).zipWithIndex.toMap


//  todo test ... does the SCC alg work with missing nodes
//  val nV = rows * cols

//  val vMap = potentialVertices.map(_._1).toArray

//  val vertexList: Iterator[(VertexId, (Int, Int, Int))] = potentialVertices.map(f => f)
//  val

  /*
  Only include edges of interest.
  An empty seq will be returned if the other pixel is not of interest
  TODO remove graphx stuff?

  Trouble is, we don't know the order
  */
  def getEdge(r0: Int, c0: Int, r1: Int, c1: Int): Seq[Edge[Double]] = {

    val idx0 = sub2ind(r0, c0)

    val idx1 = sub2ind(r1, c1)
//    val diag = (r0 != r1) && (c0 != c1)

//    val otherPixVal = pixels(idx1.toInt)


    if (vertexToID.contains(idx0) && vertexToID.contains(idx1)) {

      val vertexId0 = vertexToID(idx0)
      val vertexId1 = vertexToID(idx1)

      return Seq(
        Edge(vertexId0, vertexId1, 1),
        Edge(vertexId1, vertexId0, 1)
      )
    }
    Seq.empty
  }


  val edges: Seq[Edge[Double]] =
    vertices.flatMap{ case (vid, (r,c,v)) =>
      (if (r + 1 < rows)
        { getEdge(r, c, r + 1, c) } else { Seq.empty }) ++
        (if (c + 1 < cols)
        { getEdge(r, c, r, c + 1) } else { Seq.empty }) ++
        (if (c + 1 < cols && r + 1 < rows)
        { getEdge(r, c, r + 1, c +1 ) } else { Seq.empty }) ++
        (if (c - 1 >= 0 && r + 1 < rows)
        { getEdge(r, c, r + 1, c - 1) } else { Seq.empty })
    }

}
