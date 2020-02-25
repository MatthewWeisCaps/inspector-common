package org.sireum.hamr.inspector.common

import org.reactivestreams.Publisher
import org.sireum.hamr.inspector.stream.Flux

trait Rule extends Comparable[Rule] {

  sealed trait SeverityLevel
  case object Blocker extends SeverityLevel
  case object Critical extends SeverityLevel
  case object Normal extends SeverityLevel
  case object Minor extends SeverityLevel
  case object Trivial extends SeverityLevel

  def name: String = getClass.getSimpleName

  def severityLevel: SeverityLevel = Normal
  def epic: String = "Rules"
  def feature: String = ""
  def story: String = ""
  def displayName: String = name
  def description: String = ""
  def attachment: String = ""
  def link: String = ""

  def rule(in: Flux[Msg], utils: ArtUtils): Publisher[_ <: Any]

  override def toString: String = name

  override def compareTo(o: Rule): Int = name.compareTo(o.name)

  final override def hashCode(): Int = super.hashCode()
}