package org.roadlessforest.mxgraph

import javax.swing.JFrame

import com.mxgraph.swing.mxGraphComponent
import com.mxgraph.view.mxGraph
import org.apache.spark.graphx.{Graph, VertexId}
import org.roadlessforest.egbis.GridGraph

/**
 * See:
 * https://jgraph.github.io/mxgraph/docs/manual_javavis.html#2.2.3.1
 *
 * Created by willtemperley@gmail.com on 20-Aug-15.
 */
object GraphVisualization {

  val graph = new mxGraph()
  val parent = graph.getDefaultParent

  def showDigraph(g: GridGraph): Unit = {

    val frame = new JFrame()

    def spaceVertex(v: Int): Int = v * 50 + 20

    graph.getModel.beginUpdate()


    def matchStyle(x: Int): String = x match {
      case 1 => "fillColor=gray"
      case 2 => "fillColor=gray"
      case 3 => "fillColor=gray"
      case 4 => "fillColor=gray"
      case 5 => "fillColor=gray"
      case _ => "fillColor=white"
    }

    def matchStyle2(x: Int): String = x match {
      case 1 => "fillColor=grey"
      case 2 => "fillColor=yellow"
      case 3 => "fillColor=white"
      case 4 => "fillColor=black"
      case 5 => "fillColor=gray"
      case _ => "fillColor=white"
    }

    try {
//      def vertexLabel(a: Int, b: Int) = a + "," + b
      def vertexLabel(id: Int, a: Int, b: Int): String = {

        println(id)
        g.vertexDegree.getOrElse(id, 0) + "" //remember degree can be zero with single pixels
//        id + ""
      }

      val z: Seq[(Int, AnyRef)] = g.vertices.map{case (id, (a,b,c)) =>
  (id, graph.insertVertex(parent, null, vertexLabel(id, a, b), spaceVertex(a), spaceVertex(b), 30,30, matchStyle2(c)))}

      z.foreach(f => graph.addCell(f._2))

      val vertexIdToNode: Map[Int, AnyRef] = z.toMap

      for (t <- g.edges) {
        graph.insertEdge(parent, null, math.round(t.value), vertexIdToNode(t.srcId), vertexIdToNode(t.dstId))
      }


    } finally {
      graph.getModel.endUpdate()
    }

    val graphComponent = new mxGraphComponent(graph)
    frame.getContentPane.add(graphComponent)

    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
    frame.setSize(1000, 1000)
    frame.setVisible(true)
  }


  def showGraph(g: Graph[(Int, Int, Int), Double]) {

    val frame = new JFrame()

    def spaceVertex(v: Int): Int = v * 100 + 20

    graph.getModel.beginUpdate()

    try {

      val z: Graph[AnyRef, Double] = g.mapVertices{case (id, (a,b,c)) => graph.insertVertex(parent, null, c, spaceVertex(b), spaceVertex(a), 30,30, null)}
//
      val VS: Map[VertexId, AnyRef] = z.vertices.collect().toMap
      VS.foreach((f: (VertexId, AnyRef)) => graph.addCell(f._2))

      val triplets = g.triplets.collect().toList

      for (t <- triplets) {
        graph.insertEdge(parent, null, math.round(t.attr), VS(t.srcId), VS(t.dstId))
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
