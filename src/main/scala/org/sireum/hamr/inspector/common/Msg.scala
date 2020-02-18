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
 * @param uid - a unique id assigned to this message. Note message uids are not unique across sessions, only streams
 */
case class Msg(src: UPort,
               dst: UPort,
               srcBridge: Bridge,
               dstBridge: Bridge,
               data: DataContent,
               timestamp: Long,
               uid: Long) {

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
  def comesBefore(a: Msg, b: Msg): Boolean = a.uid < b.uid
  /**
   * Returns whether or not Msg a was received by the Inspector after Msg b.
   *
   * Due to the millisecond resolution of Msg.timestamp, two messages may have equal timestamps but distinct orderings.
   *
   * @param a the frame-of-reference message from which b is compared
   * @param b the message to which a is compared
   * @return true iff a was received by the inspector after b AND a != b
   */
  def comesAfter(a: Msg, b: Msg): Boolean = a.uid > b.uid

}