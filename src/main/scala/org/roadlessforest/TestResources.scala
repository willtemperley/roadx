package org.roadlessforest

import java.awt.image.{BufferedImage, Raster}
import java.io.File
import javax.imageio.ImageIO

import org.geotools.coverage.grid.GridCoverage2D
import org.geotools.coverage.grid.io.{AbstractGridFormat, GridFormatFinder, OverviewPolicy}
import org.geotools.parameter.DefaultParameterDescriptor

/**
 *
 * Created by willtemperley@gmail.com on 20-Jul-15.
 */
object TestResources {

  def getTestCostSurface: Iterator[String] = {
    scala.io.Source.fromFile("src/test/resources/leastcost/cost-6x7").getLines()
  }

  def getTestRasterTile: Raster = {

    val file = new File("src/test/resources/leastcost/cost_test.tif")

    val format = GridFormatFinder.findFormat( file )
    val reader = format.getReader( file )

    //    val gridsize = AbstractGridFormat.SUGGESTED_TILE_SIZE.createValue()
    val gridSize = new DefaultParameterDescriptor("SUGGESTED_TILE_SIZE", classOf[String], null, "100,100").createValue()
    val policy = AbstractGridFormat.OVERVIEW_POLICY.createValue()
    policy.setValue(OverviewPolicy.IGNORE)

    //Setting read type: use JAI ImageRead (true) or ImageReaders read methods (false)
    val useJaiRead = AbstractGridFormat.USE_JAI_IMAGEREAD.createValue()
    useJaiRead.setValue(true)

    val coverage: GridCoverage2D = reader.read(Array(policy, gridSize))

    val renderedImage = coverage.getRenderedImage

    val rasTile = renderedImage.getTile(0,0)

    rasTile
  }

  def getLenna: Raster = {

    val file = new File("src/test/resources/rasters/Lenna.png")

    val img: BufferedImage = ImageIO.read(file)
    img.getRaster

  }


  def getTestRaster(name: String): Raster = {
    val file = new File("src/test/resources/", name)

    val format = GridFormatFinder.findFormat(file)
    val reader = format.getReader(file)

//    val gridsize = AbstractGridFormat.SUGGESTED_TILE_SIZE.createValue()
    val gridSize = new DefaultParameterDescriptor("SUGGESTED_TILE_SIZE", classOf[String], null, "100,100").createValue()
    val policy = AbstractGridFormat.OVERVIEW_POLICY.createValue()
    policy.setValue(OverviewPolicy.IGNORE)

    //Setting read type: use JAI ImageRead (true) or ImageReaders read methods (false)
    val useJaiRead = AbstractGridFormat.USE_JAI_IMAGEREAD.createValue()
    useJaiRead.setValue(true)

    val coverage: GridCoverage2D = reader.read(Array(policy, gridSize))

    val renderedImage = coverage.getRenderedImage

    renderedImage.getData
  }

}
