package zone.spooky.spook.service

case object GetConfiguration

sealed trait GetConfigurationResponse
case object GetConfigurationFailure extends GetConfigurationResponse
final case class GetConfigurationSuccess(config: Map[String, String]) extends GetConfigurationResponse

final case class ConfigurationChanged(key: String, value: String)

sealed trait ConfigurationChangedResponse
case object ConfigurationChangedSuccess extends ConfigurationChangedResponse
case object ConfigurationChangedFailure extends ConfigurationChangedResponse