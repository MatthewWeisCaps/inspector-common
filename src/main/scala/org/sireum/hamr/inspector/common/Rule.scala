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
 * Rules are like Filters, except that while
 *    filters map FROM message-stream TO message-stream,
 *      rules map FROM message-stream TO stream-of-anything.
 *
 * The "catch" is that the Inspector doesn't observe the rule's output, but instead any errors it throws when running.
 * These errors treated as rule failures and will be visible to the user in the GUI (or some other way depending on
 * the inspector-services implementation).
 */
trait Rule extends Comparable[Rule] {

  /**
   * The UNIQUE name of this rule. Used for logging and displaying in user interfaces.
   *
   * By default the name will equal the simple name of its implementation.
   *
   * @return The UNIQUE name of this rule.
   */
  def name: String = getClass.getSimpleName

  /**
   * A function which can be applied to a Flux of messages to return a new (reactive-)stream whose output is ignored
   * but is monitored for errors thrown within to indicate that the rule has failed.
   *
   * This function MUST BE PURE (free of side effects).
   *
   * Note: A Flux is a reactive-stream implementation that includes many operators out of the box.
   *
   * @param in an input (reactive-)stream of messages to which the rule will be applied.
   * @return a new (reactive-)stream to watch for exceptions.
   */
  def rule(in: Flux[Msg], utils: ArtUtils): Publisher[_ <: Any]

  override def toString: String = name

  override def compareTo(o: Rule): Int = name.compareTo(o.name)
}