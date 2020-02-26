package org.sireum.hamr.inspector.common

import org.reactivestreams.Publisher
import org.sireum.hamr.inspector.stream.Flux

trait Filter {
  def name: String = getClass.getSimpleName

  def filter(in: Flux[Msg], utils: ArtUtils): Publisher[Msg]

  def rules: Iterable[_ <: Rule] = Seq.empty[Rule]

  override def toString: String = name
}
