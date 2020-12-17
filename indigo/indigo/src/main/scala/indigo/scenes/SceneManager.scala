package indigo.scenes

import indigo.shared.Outcome
import indigo.shared.events.GlobalEvent
import indigo.shared.scenegraph.SceneUpdateFragment
import indigo.shared.IndigoLogger
import indigo.shared.collections.NonEmptyList

import indigo.shared.subsystems.SubSystemsRegister
import indigo.shared.FrameContext
import indigo.shared.subsystems.SubSystemFrameContext._
import indigo.shared.events.EventFilters
import indigo.shared.subsystems.SubSystemFrameContext

class SceneManager[StartUpData, GameModel, ViewModel](scenes: NonEmptyList[Scene[StartUpData, GameModel, ViewModel]], scenesFinder: SceneFinder) {

  // Scene management
  // @SuppressWarnings(Array("org.wartremover.warts.Var"))
  private var finderInstance: SceneFinder = scenesFinder

  // @SuppressWarnings(Array("org.wartremover.warts.MutableDataStructures"))
  private val subSystemStates: Map[SceneName, SubSystemsRegister] =
    scenes.toList.map { s =>
      val r = new SubSystemsRegister(Nil)
      r.register(s.subSystems.toList)
      (s.name -> r)
    }.toMap

  // Scene delegation
  // @SuppressWarnings(Array("org.wartremover.warts.NonUnitStatements"))
  def updateModel(frameContext: FrameContext[StartUpData], model: GameModel): GlobalEvent => Outcome[GameModel] = {
    case SceneEvent.Next =>
      val from = finderInstance.current.name
      finderInstance = finderInstance.forward
      val to = finderInstance.current.name
      val events =
        if (from == to) Nil
        else List(SceneEvent.SceneChange(from, to, frameContext.gameTime.running))

      Outcome(model, events)

    case SceneEvent.Previous =>
      val from = finderInstance.current.name
      finderInstance = finderInstance.backward
      val to = finderInstance.current.name
      val events =
        if (from == to) Nil
        else List(SceneEvent.SceneChange(from, to, frameContext.gameTime.running))

      Outcome(model, events)

    case SceneEvent.JumpTo(name) =>
      val from = finderInstance.current.name
      finderInstance = finderInstance.jumpToSceneByName(name)
      val to = finderInstance.current.name
      val events =
        if (from == to) Nil
        else List(SceneEvent.SceneChange(from, to, frameContext.gameTime.running))

      Outcome(model, events)

    case event =>
      scenes.find(_.name == finderInstance.current.name) match {
        case None =>
          IndigoLogger.errorOnce("Could not find scene called: " + finderInstance.current.name.name)
          Outcome(model)

        case Some(scene) =>
          Scene
            .updateModel(scene, frameContext, model)(event)
      }
  }

  def updateSubSystems(frameContext: SubSystemFrameContext, globalEvents: List[GlobalEvent]): Outcome[SubSystemsRegister] =
    scenes
      .find(_.name == finderInstance.current.name)
      .flatMap { scene =>
        subSystemStates
          .get(scene.name)
          .map {
            _.update(frameContext, globalEvents)
          }
      }
      .getOrElse(Outcome.raiseError(new Exception(s"Couldn't find scene with name '${finderInstance.current.name}' in order to update subsystems")))

  def updateViewModel(frameContext: FrameContext[StartUpData], model: GameModel, viewModel: ViewModel): GlobalEvent => Outcome[ViewModel] =
    scenes.find(_.name == finderInstance.current.name) match {
      case None =>
        IndigoLogger.errorOnce("Could not find scene called: " + finderInstance.current.name.name)
        _ => Outcome(viewModel)

      case Some(scene) =>
        Scene.updateViewModel(scene, frameContext, model, viewModel)
    }

  def updateView(frameContext: FrameContext[StartUpData], model: GameModel, viewModel: ViewModel): Outcome[SceneUpdateFragment] =
    scenes.find(_.name == finderInstance.current.name) match {
      case None =>
        IndigoLogger.errorOnce("Could not find scene called: " + finderInstance.current.name.name)
        Outcome(SceneUpdateFragment.empty)

      case Some(scene) =>
        val subsystemView = subSystemStates
          .get(scene.name)
          .map { ssr =>
            ssr.present(frameContext.forSubSystems)
          }
          .getOrElse(Outcome(SceneUpdateFragment.empty))

        Outcome.merge(Scene.updateView(scene, frameContext, model, viewModel), subsystemView)(_ |+| _)

    }

  val defaultFilter: GlobalEvent => Option[GlobalEvent] =
    (e: GlobalEvent) => Some(e)

  def eventFilters: EventFilters =
    scenes.find(_.name == finderInstance.current.name) match {
      case None =>
        EventFilters.Default

      case Some(value) =>
        value.eventFilters
    }

}

object SceneManager {

  def apply[StartUpData, GameModel, ViewModel](scenes: NonEmptyList[Scene[StartUpData, GameModel, ViewModel]], initialScene: SceneName): SceneManager[StartUpData, GameModel, ViewModel] =
    new SceneManager[StartUpData, GameModel, ViewModel](scenes, SceneFinder.fromScenes[StartUpData, GameModel, ViewModel](scenes).jumpToSceneByName(initialScene))

}
