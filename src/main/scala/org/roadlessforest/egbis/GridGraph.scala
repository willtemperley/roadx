package org.roadlessforest.egbis

import java.awt.image.Raster

/**
  * Created by willtemperley@gmail.com on 09-Mar-16.
  *
  * Given an image, creates an 8-connnected grid-graph
  *
  * TODO: easily customized
  *
  */
class GridGraph(raster: Raster, includePixel: (Int => Boolean) = (x: Int) => true) {

  /*
  Array subscripts to position in array
   */
  def sub2index(cols: Int)(r: Int, c: Int): PixelIndex = c * cols + r

  def index2sub(cols: Int)(v: PixelIndex): (Long, Long) =  {
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
  private val pixelIndexToPixel: Seq[(PixelIndex, (Int, Int, Int))] = pixelRows
    .flatMap {case(row, rowIdx) => row.zipWithIndex //zips with the column idx
    .map {case (costVal, colIdx) => (sub2ind(colIdx, rowIdx), (colIdx, rowIdx, costVal))}}
    .filter(f => includePixel(f._2._3))
    .toSeq

  /*
  Vertex id to new vertexId, which is the zipped index
  */
  val indexToVertexId: Map[PixelIndex, VertexId] =
    pixelIndexToPixel
      .map(_._1).zipWithIndex.toMap

  /*
  Flip the map
   */
  val vertexIdToIndex: Map[VertexId, PixelIndex]
  = indexToVertexId.map(_.swap)


  val vertices = pixelIndexToPixel.map(f => (indexToVertexId(f._1), f._2))

  /*
  Only include edges of interest.
  An empty seq will be returned if the other pixel is not of interest
  TODO remove graphx stuff?

  Trouble is, we don't know the order
  */
  def getEdge(r0: Int, c0: Int, r1: Int, c1: Int): Seq[Edge] = {

    val idx0 = sub2ind(r0, c0)

    val idx1 = sub2ind(r1, c1)

//    val diag = (r0 != r1) && (c0 != c1)
//    val otherPixVal = pixels(idx1.toInt)

    if (indexToVertexId.contains(idx0) && indexToVertexId.contains(idx1)) {

      val vertexId0: Int = indexToVertexId(idx0)
      val vertexId1: Int = indexToVertexId(idx1)

      return Seq(
        new Edge((vertexId0, vertexId1, 1)),
        new Edge((vertexId1, vertexId0, 1))
      )
    }
    Seq.empty
  }


  val edges: Seq[Edge] =
    pixelIndexToPixel.flatMap{ case (vid, (r,c,v)) =>
      (if (r + 1 < rows)
        { getEdge(r, c, r + 1, c) } else { Seq.empty }) ++
        (if (c + 1 < cols)
        { getEdge(r, c, r, c + 1) } else { Seq.empty }) ++
        (if (c + 1 < cols && r + 1 < rows)
        { getEdge(r, c, r + 1, c +1 ) } else { Seq.empty }) ++
        (if (c - 1 >= 0 && r + 1 < rows)
        { getEdge(r, c, r + 1, c - 1) } else { Seq.empty })
    }

  val allVs = edges.map(_.srcId) ++ edges.map(_.dstId)

  val vertexDegree: Map[VertexId, Int] = allVs.groupBy(identity).mapValues(_.size)

}
