package org.roadlessforest.graphx

import javax.swing.JFrame

import com.mxgraph.swing.mxGraphComponent
import com.mxgraph.view.mxGraph
import org.apache.spark.graphx.Graph

/**
 * Created by willtemperley@gmail.com on 20-Aug-15.
 */
object GraphVisualization {

  val graph = new mxGraph()
  val parent = graph.getDefaultParent

  def showGraph(g: Graph[(Int, Int, Int), Double]) {

    val frame = new JFrame()

    def spaceVertex(v: Int): Int = v * 100 + 20

    graph.getModel.beginUpdate()

    try {

      val z = g.mapVertices{case (id, (a,b,c)) => graph.insertVertex(parent, null, c, spaceVertex(b), spaceVertex(a), 30,30, null)}
//
      val VS = z.vertices.collect().toMap
      VS.foreach(f => graph.addCell(f._2))

      val triplets = g.triplets.collect().toList

      for (t <- triplets) {
        graph.insertEdge(parent, null, math.round(t.attr), VS.get(t.srcId).get, VS.get(t.dstId).get)
      }

    } finally {
      graph.getModel.endUpdate()
    }

    val graphComponent = new mxGraphComponent(graph)
    frame.getContentPane.add(graphComponent)

    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
    frame.setSize(800, 800)
    frame.setVisible(true)
  }

}
