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

import art._

/**
 * A Msg is a data (case) class containing all information known by the Inspector when a message is sent from one
 * bridge to another via Art. Previously the content in Msg was contained in a Java class called Art.
 *
 * @param src - the UPort source from which the message originated
 * @param dst - the UPort source to which the message was sent
 * @param data - the DataContent contained within the message
 * @param timestamp - the millisecond timestamp of when message was sent. Time is based off of Art's internal clock.
 * @param sequence - the 0-based sequence number of this message at its particular timestamp.
 */
case class Msg(src: UPort,
               dst: UPort,
               srcBridge: Bridge,
               dstBridge: Bridge,
               data: DataContent,
               timestamp: Long,
               sequence: Long) {

  //  // classifier utility functions
  def isSrcPeriodic: Boolean = Msg.isSrcPeriodic(this)
  def isSrcSporadic: Boolean = Msg.isSrcSporadic(this)

  def isDstPeriodic: Boolean = Msg.isDstPeriodic(this)
  def isDstSporadic: Boolean = Msg.isDstSporadic(this)

  def isDataIn: Boolean = Msg.isDataIn(this)
  def isDataOut: Boolean = Msg.isDataOut(this)
  def isEventIn: Boolean = Msg.isEventIn(this)
  def isEventOut: Boolean = Msg.isEventOut(this)

  def dataEquals(other: Msg): Boolean = Msg.equalData(this, other)

  /**
   * Returns whether or not this Msg was received by the Inspector before "other."
   *
   * Due to the millisecond resolution of Msg.timestamp, two messages may have equal timestamps but distinct orderings.
   *
   * @param other the message to which this Msg is compared
   * @return true iff "this" was received by the inspector before "other" AND "this" != "other"
   */
  def comesBefore(other: Msg): Boolean = Msg.comesBefore(this, other)
  /**
   * Returns whether or not this Msg was received by the Inspector after "other."
   *
   * Due to the millisecond resolution of Msg.timestamp, two messages may have equal timestamps but distinct orderings.
   *
   * @param other the message to which this Msg is compared
   * @return true iff "this" was received by the inspector after "other" AND "this" != "other"
   */
  def comesAfter(other: Msg): Boolean = Msg.comesAfter(this, other)
}

object Msg {

  // classifier utility functions
  def isSrcPeriodic(msg: Msg): Boolean = msg.srcBridge.dispatchProtocol match {
    case DispatchPropertyProtocol.Periodic(_) => true
    case _ => false
  }

  def isSrcSporadic(msg: Msg): Boolean = msg.srcBridge.dispatchProtocol match {
    case DispatchPropertyProtocol.Sporadic(_) => true
    case _ => false
  }

  def isDstPeriodic(msg: Msg): Boolean = msg.dstBridge.dispatchProtocol match {
    case DispatchPropertyProtocol.Periodic(_) => true
    case _ => false
  }

  def isDstSporadic(msg: Msg): Boolean = msg.dstBridge.dispatchProtocol match {
    case DispatchPropertyProtocol.Sporadic(_) => true
    case _ => false
  }

  def isDataIn(msg: Msg): Boolean = msg.dst.mode == PortMode.DataIn
  def isEventIn(msg: Msg): Boolean = msg.dst.mode == PortMode.EventIn

  def isDataOut(msg: Msg): Boolean = msg.src.mode == PortMode.DataOut
  def isEventOut(msg: Msg): Boolean = msg.src.mode == PortMode.EventOut

  def equalData(a: Msg, b: Msg): Boolean = a.data == b.data

  /**
   * Returns whether or not Msg a was received by the Inspector before Msg b.
   *
   * Due to the millisecond resolution of Msg.timestamp, two messages may have equal timestamps but distinct orderings.
   *
   * @param a the frame-of-reference message from which b is compared
   * @param b the message to which a is compared
   * @return true iff a was received by the inspector before b AND a != b
   */
  def comesBefore(a: Msg, b: Msg): Boolean = a.sequence < b.sequence
  /**
   * Returns whether or not Msg a was received by the Inspector after Msg b.
   *
   * Due to the millisecond resolution of Msg.timestamp, two messages may have equal timestamps but distinct orderings.
   *
   * @param a the frame-of-reference message from which b is compared
   * @param b the message to which a is compared
   * @return true iff a was received by the inspector after b AND a != b
   */
  def comesAfter(a: Msg, b: Msg): Boolean = a.sequence > b.sequence

}