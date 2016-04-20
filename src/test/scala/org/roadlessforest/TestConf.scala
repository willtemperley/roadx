package org.roadlessforest


/**
 * Created by willtemperley@gmail.com on 07-Jul-15.
 *
 */
class TestConf {

  def loadConfiguration(): Unit = {

    val c = ConfigurationFactory.get

//    Assert.assertTrue(c.get("hbase.master") != null)

  }

}
