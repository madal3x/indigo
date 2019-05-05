package indigo.shared

import indigo.shared

trait SharedTypeAliases {

  type AsString[A] = shared.AsString[A]
  val AsString: shared.AsString.type = shared.AsString

  type EqualTo[A] = shared.EqualTo[A]
  val EqualTo: shared.EqualTo.type = shared.EqualTo

  type AssetType = shared.AssetType
  val AssetType: shared.AssetType.type = shared.AssetType

  type ClearColor = shared.ClearColor
  val ClearColor: shared.ClearColor.type = shared.ClearColor

  type GameConfig = shared.config.GameConfig
  val GameConfig: shared.config.GameConfig.type = shared.config.GameConfig

  type GameViewport = shared.config.GameViewport
  val GameViewport: shared.config.GameViewport.type = shared.config.GameViewport

  type AdvancedGameConfig = shared.config.AdvancedGameConfig
  val AdvancedGameConfig: shared.config.AdvancedGameConfig.type = shared.config.AdvancedGameConfig

  val IndigoLogger: shared.IndigoLogger.type = shared.IndigoLogger

  type Aseprite = shared.formats.Aseprite
  val Aseprite: shared.formats.Aseprite.type = shared.formats.Aseprite

  type TiledMap = shared.formats.TiledMap
  val TiledMap: shared.formats.TiledMap.type = shared.formats.TiledMap

}
