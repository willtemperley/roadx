package org.roadlessforest.terracost

import java.awt.image.Raster

import com.vividsolutions.jts.geom.Coordinate
import edu.princeton.cs.algorithms.DirectedEdge
import org.apache.spark.graphx.{Edge, VertexId}
import org.apache.spark.rdd.RDD

/**
  * Created by willtemperley@gmail.com on 09-Mar-16.
  */

class Tile(raster: Raster, includePixel: (Int => Boolean) = (x: Int) => true) {

  // The edge cost is the average of the vertex costs, multiplied by sqrt2 if it's diagonal
  def cost(a: Double, b: Double, diagonal: Boolean): Double = {
    val dist = if(diagonal) math.sqrt(2) else 1
    ((a + b) / 2) * dist
  }

  def sub2index(cols: Int)(r: Int, c: Int): VertexId = r * cols + c

  val rows = raster.getHeight
  val cols = raster.getWidth

  def sub2ind = sub2index(cols) _

  //Retrieve all pixels
  val pixels: Array[Int] = raster.getPixels(0,0, cols, rows, new Array[Int](rows * cols))

  //Group pixels by row and zip up with their row number
  val pixelRows = pixels.grouped(cols).zipWithIndex

  // the vertex values are row, column and cost value
  val vertices: Iterator[(VertexId, (Int, Int, Int))] = pixelRows
    .flatMap {case(row, rowIdx) => row.zipWithIndex //zips with the column idx
      .map{case (costVal, colIdx) => (sub2ind(rowIdx, colIdx), (rowIdx, colIdx, costVal))}}

  def getEdge(r0: Int, c0: Int, r1: Int, c1: Int): Seq[Edge[Double]] = {
    val idx0 = sub2ind(r0, c0)
    val idx1 = sub2ind(r1, c1)
    val diag = (r0 != r1) && (c0 != c1)

    val pixval0 = pixels(idx0.toInt)
    val pixval1 = pixels(idx1.toInt)

    if (includePixel(pixval0) || includePixel(pixval1)) {
      val c = cost(pixval0, pixval1, diag)

      return Seq(
        Edge(idx0, idx1, c),
        Edge(idx1, idx0, c)
      )
    }

    Seq.empty
  }

  val edges: Iterator[Edge[Double]] =
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
