package org.roadlessforest

import java.awt.Point
import java.awt.geom.Point2D
import java.awt.image.{Raster, WritableRaster}
import java.io.{File, PrintWriter}
import javax.imageio.ImageWriteParam

import org.geotools.coverage.grid.GridCoverageFactory
import org.geotools.coverage.grid.io.AbstractGridFormat
import org.geotools.gce.geotiff.{GeoTiffFormat, GeoTiffWriteParams, GeoTiffWriter}
import org.geotools.geometry.Envelope2D
import org.geotools.referencing.CRS
import org.opengis.parameter.GeneralParameterValue

/**
  * Created by willtemperley@gmail.com on 22-Apr-16.
  */
object ExportUtils {

  def writeGeoTiff(bigImage: WritableRaster, outFile: File): Unit = {

    //getting the write parameters
    val wp = new GeoTiffWriteParams
    val format = new GeoTiffFormat

    //setting compression to LZW
    wp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT)
    wp.setCompressionType("LZW")
    wp.setCompressionQuality(1.0F)

    val params = format.getWriteParameters
    params.parameter(
      AbstractGridFormat.GEOTOOLS_WRITE_PARAMS.getName.toString)
      .setValue(wp)

    val sourceCRS = CRS.decode("EPSG:4326")
    val bbox = new Envelope2D(sourceCRS, -90, -180, 180, 360)
    val coverage = new GridCoverageFactory().create("tif", bigImage, bbox)
    val gtw = new GeoTiffWriter(outFile)
    gtw.write(coverage, params.values().toArray(new Array[GeneralParameterValue](1)))

  }

  def writeTFWfile(outFile: File, pt: Point2D.Double, pixelSize: Double): Unit = {

    val pw = new PrintWriter(outFile)


    //    Line 1: A: pixel size in the x-direction in map units/pixel
    //    Line 2: D: rotation about y-axis
    //    Line 3: B: rotation about x-axis
    //    Line 4: E: pixel size in the y-direction in map units, almost always negative[3]
    //    Line 5: C: x-coordinate of the center of the upper left pixel
    //    Line 6: F: y-coordinate of the center of the upper left pixel
    pw.write("" + pixelSize)
    pw.write("\n")
    pw.write("0")
    pw.write("\n")
    pw.write("0")
    pw.write("\n")
    pw.write("" + pixelSize)
    pw.write("\n")
    pw.write("" + pt.getX)
    pw.write("\n")
    pw.write("" + pt.getY)
    pw.close()

  }
}
