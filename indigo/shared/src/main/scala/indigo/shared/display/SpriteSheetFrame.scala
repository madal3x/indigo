package indigo.shared.display

import indigo.shared.datatypes.Vector2

import indigo.shared.EqualTo
import indigo.shared.datatypes.Rectangle

object SpriteSheetFrame {

  def calculateFrameOffset(atlasSize: Vector2, frameCrop: Rectangle, textureOffset: Vector2): SpriteSheetFrameCoordinateOffsets = {
    val frameSize   = frameCrop.size.toVector
    val scaleFactor = frameSize / atlasSize

    val translator: Vector2 => Vector2 =
      texturePosition => scaleFactor * ((frameCrop.position.toVector + texturePosition) / frameSize)

    new SpriteSheetFrameCoordinateOffsets(scaleFactor, translator(textureOffset), translator)
  }

  val defaultOffset: SpriteSheetFrameCoordinateOffsets =
    calculateFrameOffset(Vector2(1.0, 1.0), Rectangle(0, 0, 1, 1), Vector2.zero)

  final case class SpriteSheetFrameCoordinateOffsets(scale: Vector2, translate: Vector2, translateCoords: Vector2 => Vector2) {
    def offsetToCoords(textureOffset: Vector2): Vector2 =
      translateCoords(textureOffset)
  }
  object SpriteSheetFrameCoordinateOffsets {

    implicit val eq: EqualTo[SpriteSheetFrameCoordinateOffsets] = {
      val ev = implicitly[EqualTo[Vector2]]

      EqualTo.create { (a, b) =>
        ev.equal(a.scale, b.scale) && ev.equal(a.translate, b.translate)
      }
    }

  }

}
