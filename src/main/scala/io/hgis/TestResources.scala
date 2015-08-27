package io.hgis

import java.awt.image.Raster
import java.io.File

import org.geotools.coverage.grid.GridCoverage2D
import org.geotools.coverage.grid.io.{GridFormatFinder, OverviewPolicy, AbstractGridFormat}

/**
 *
 * Created by willtemperley@gmail.com on 20-Jul-15.
 */
object TestResources {

  def getTestCostSurface: Iterator[String] = {
    scala.io.Source.fromFile("src/test/resources/leastcost/cost-6x7").getLines()
  }

  def getTestTiffRaster: Raster = {

//    val file = new File("src/test/resources/leastcost/cost_test.tif")
    val file = new File("src/test/resources/leastcost/test_small.tif")

    val format = GridFormatFinder.findFormat( file )
    val reader = format.getReader( file )

    val gridsize = AbstractGridFormat.SUGGESTED_TILE_SIZE.createValue()
    val policy = AbstractGridFormat.OVERVIEW_POLICY.createValue()
    policy.setValue(OverviewPolicy.IGNORE)

    //Setting read type: use JAI ImageRead (true) or ImageReaders read methods (false)
    val useJaiRead = AbstractGridFormat.USE_JAI_IMAGEREAD.createValue()
    useJaiRead.setValue(true)

    val coverage: GridCoverage2D = reader.read(Array(policy, gridsize))

    coverage.getRenderedImage.getData

  }

}
