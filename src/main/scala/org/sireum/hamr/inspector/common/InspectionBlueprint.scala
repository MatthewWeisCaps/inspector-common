package org.sireum.hamr.inspector.common

import art.{ArchitectureDescription, DataContent}

trait InspectionBlueprint {
  def ad(): ArchitectureDescription
  def serializer(): DataContent => String
  def deserializer(): String => DataContent
}