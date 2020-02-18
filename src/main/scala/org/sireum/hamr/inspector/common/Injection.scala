package org.sireum.hamr.inspector.common

import art.{Bridge, DataContent, UPort}

trait Injection {
  def name: String = getClass.getSimpleName
  def port: UPort
  def bridge: Bridge
  def dataContent: DataContent

  override def toString: String = name
}