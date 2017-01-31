package zone.spooky.spook.service

case object GetConfiguration
final case class GetConfigurationResponse(config: Map[String, String])
final case class ConfigurationChanged(key: String, value: String)
case object ConfigurationSaved
case object End
