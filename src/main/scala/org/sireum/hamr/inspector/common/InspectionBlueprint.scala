package org.sireum.hamr.inspector.common

import art.{ArchitectureDescription, DataContent}

trait InspectionBlueprint {
  def ad(): ArchitectureDescription
  def serializer(): DataContent => org.sireum.String
  def deserializer(): org.sireum.String => DataContent
}