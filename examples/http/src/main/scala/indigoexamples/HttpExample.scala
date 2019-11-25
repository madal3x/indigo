package indigoexamples

import indigo._
import indigoexts.entrypoint._
import indigoexts.ui._

object HttpExample extends IndigoGameBasic[Unit, MyGameModel, Unit] {

  val config: GameConfig = defaultGameConfig

  val assets: Set[AssetType] = Set(AssetType.Image("graphics", "assets/graphics.png"))

  val fonts: Set[FontInfo] = Set()

  val animations: Set[Animation] = Set()

  val subSystems: Set[SubSystem] = Set()

  def setup(assetCollection: AssetCollection): Startup[StartupErrors, Unit] =
    Startup.Success(())

  def initialModel(startupData: Unit): MyGameModel =
    MyGameModel(
      button = Button(ButtonState.Up).withUpAction { () =>
        List(HttpRequest.GET("http://localhost:8080/ping"))
      },
      count = 0
    )

  def update(gameTime: GameTime, model: MyGameModel, dice: Dice): GlobalEvent => Outcome[MyGameModel] = {
    case e: ButtonEvent =>
      Outcome(
        model.copy(
          button = model.button.update(e)
        )
      )

    case HttpResponse(status, headers, body) =>
      println("Status code: " + status.toString)
      println("Headers: " + headers.map(p => p._1 + ": " + p._2).mkString(", "))
      println("Body: " + body.getOrElse("<EMPTY>"))
      Outcome(model)

    case HttpError =>
      println("Http error message")
      Outcome(model)

    case _ =>
      Outcome(model)
  }

  def initialViewModel(startupData: Unit): MyGameModel => Unit = _ => ()

  def updateViewModel(gameTime: GameTime, model: MyGameModel, viewModel: Unit, frameInputEvents: FrameInputEvents, dice: Dice): Outcome[Unit] =
    Outcome(())

  def present(gameTime: GameTime, model: MyGameModel, viewModel: Unit, frameInputEvents: FrameInputEvents): SceneUpdateFragment = {
    val button: ButtonViewUpdate = model.button.draw(
      bounds = Rectangle(10, 10, 16, 16),
      depth = Depth(2),
      frameInputEvents = frameInputEvents,
      buttonAssets = ButtonAssets(
        up = Graphic(0, 0, 16, 16, 2, "graphics").withCrop(32, 0, 16, 16),
        over = Graphic(0, 0, 16, 16, 2, "graphics").withCrop(32, 16, 16, 16),
        down = Graphic(0, 0, 16, 16, 2, "graphics").withCrop(32, 32, 16, 16)
      )
    )

    button.toSceneUpdateFragment
  }
}

// We need a button in our model
final case class MyGameModel(button: Button, count: Int)