package org.roadlessforest.egbis

import java.awt.image.Raster

import org.apache.spark.graphx.{Edge, VertexId}

/**
  * Created by willtemperley@gmail.com on 09-Mar-16.
  */

class GridGraph(raster: Raster, includePixel: (Int => Boolean) = (x: Int) => true) {

  // The edge cost is the average of the vertex costs, multiplied by sqrt2 if it's diagonal
  def cost(a: Double, b: Double, diagonal: Boolean): Double = {
    val dist = if(diagonal) math.sqrt(2) else 1
    ((a + b) / 2) * dist
  }

  def sub2index(cols: Int)(r: Int, c: Int): VertexId = c * cols + r

  def index2sub(cols: Int)(v: VertexId) =  {

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
  val filteredVertices: Iterator[(VertexId, (Int, Int, Int))] = pixelRows
    .flatMap {case(row, rowIdx) => row.zipWithIndex //zips with the column idx
    .filter {case (costVal, colIdx) => includePixel(costVal)} //filter the unwanted
    .map {case (costVal, colIdx) => (sub2ind(rowIdx, colIdx), (rowIdx, colIdx, costVal))}}


  /*
  Need: Array subindexes;
  A mapping between
   */
  val pixMap =
  filteredVertices.map(f => f._1)

//  todo test ... does the SCC alg work with missing nodes
//  val nV = rows * cols

//  val vMap = potentialVertices.map(_._1).toArray

//  val vertexList: Iterator[(VertexId, (Int, Int, Int))] = potentialVertices.map(f => f)
//  val

  def getEdge(r0: Int, c0: Int, r1: Int, c1: Int): Seq[Edge[Double]] = {
    val idx0 = sub2ind(r0, c0)
    val idx1 = sub2ind(r1, c1)
//    val diag = (r0 != r1) && (c0 != c1)

//    val pixval0 = pixels(idx0.toInt)
//    val pixval1 = pixels(idx1.toInt)

//    val c = cost(pixval0, pixval1, diag)
    //todo remove graphx stuff?
    Seq(
      Edge(idx0, idx1, 1),
      Edge(idx1, idx0, 1)
    )

  }

  val edges: Iterator[Edge[Double]] =
    filteredVertices.flatMap{ case (vid, (r,c,v)) =>
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
