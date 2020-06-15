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

import art.{Bridge, DataContent, UPort}

/**
 * A blueprint for injecting a message into Art's data stream to(/from) a particular incoming(/outgoing) port.
 */
trait Injection {

  /**
   * The UNIQUE name of this injection. Used for logging and displaying in user interfaces.
   *
   * @return The name associated with this injection
   */
  def name: String = getClass.getSimpleName

  /**
   * The bridge of the message to be injected.
   *
   * // todo This information is currently redundant but can't be removed until updating ArtDebug
   *
   * @return the bridge to associate this injection with
   */
  def bridge: Bridge

  /**
   * The port of the message to be injected. Messages injected into the event stream will:
   *   - claim to be FROM the port if the port is OUTGOING
   *   - be delivered TO the port if the port is INCOMING
   *
   * @return the port to associate this injection with
   */
  def port: UPort

  /**
   * The DataContent attached to the injected msg
   *
   * @return The DataContent attached to the injected msg
   */
  def dataContent: DataContent

  override def toString: String = name
}