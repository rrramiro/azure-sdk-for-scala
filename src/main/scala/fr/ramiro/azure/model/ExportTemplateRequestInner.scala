package fr.ramiro.azure.model

case class ExportTemplateRequestInner(
  resources: Seq[String],
  options: String
)