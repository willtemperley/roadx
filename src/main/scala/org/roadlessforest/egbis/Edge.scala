package org.roadlessforest.egbis

/**
  * Created by willtemperley@gmail.com on 05-May-16.
  */
class Edge(val v: (VertexId, VertexId, Int)) extends AnyVal {
  def srcId = v._1
  def dstId = v._2
  def value = v._3
}
