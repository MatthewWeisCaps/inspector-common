/*
 * Copyright (c) 2020, Matthew Weis, Kansas State University
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.sireum.hamr.inspector.common

import org.reactivestreams.Publisher
import org.sireum.hamr.inspector.stream.Flux

/**
 * A filter for Art's message stream that has an (optionally) overridable unique name.
 *
 * Contains a "filter" function that converts a "Flux" (i.e. a real-time or prerecorded stream) of Msgs to a
 * "Publisher" (which is just Flux's parent/super-interface).
 *
 * Also contains a "name" function that can be overwritten to give this filter a custom name.
 *
 */
trait Filter {

  /**
   * The UNIQUE name of this Filter. Used for logging and displaying in user interfaces.
   *
   * By default the name will equal the simple name of its implementation.
   *
   * @return The name associated with this filter
   */
  def name: String = getClass.getSimpleName

  /**
   * A function which can be applied to a Flux of messages to return a new (reactive-)stream of messages.
   *
   * This function MUST BE PURE (free of side effects).
   *
   * Note: A Flux is a reactive-stream implementation that includes many operators out of the box.
   *
   * @param in an input (reactive-)stream of messages to which the filter will be applied.
   * @return a new (reactive-)stream of messages that has possibly undergone some transformation
   */
  def filter(in: Flux[Msg]): Publisher[Msg]

  /**
   * Associate a set of rules with this filter. This is currently experimental in the interface and may be removed.
   * @return
   */
  def rules: Iterable[_ <: Rule] = Seq.empty[Rule]

  override def toString: String = name
}
