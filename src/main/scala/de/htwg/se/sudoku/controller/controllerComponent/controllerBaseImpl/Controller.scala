package de.htwg.se.sudoku.controller.controllerComponent.controllerBaseImpl

import com.google.inject.name.Names
import com.google.inject.{Guice, Inject}
import net.codingwell.scalaguice.InjectorExtensions._
import de.htwg.se.sudoku.SudokuModule
import de.htwg.se.sudoku.controller.controllerComponent.GameStatus._
import de.htwg.se.sudoku.controller.controllerComponent._
import de.htwg.se.sudoku.model.gridComponent.GridInterface
import de.htwg.se.sudoku.util.UndoManager

import scala.swing.Publisher

class  Controller @Inject() (var grid: GridInterface) extends ControllerInterface with Publisher {

  var gameStatus: GameStatus = IDLE
  var showAllCandidates: Boolean = false
  private val undoManager = new UndoManager
  val injector = Guice.createInjector(new SudokuModule)

  def createEmptyGrid(size: Int): Unit = {
    size match {
      case 1 => grid = injector.instance[GridInterface](Names.named("tiny"))
      case 4 => grid = injector.instance[GridInterface](Names.named("small"))
      case 9 => grid = injector.instance[GridInterface](Names.named("normal"))
      case _ =>
    }
    publish(new CellChanged)
  }

  def resize(newSize:Int) :Unit = {
    newSize match {
      case 1 => grid = injector.instance[GridInterface](Names.named("tiny"))
      case 4 => grid = injector.instance[GridInterface](Names.named("small"))
      case 9 => grid = injector.instance[GridInterface](Names.named("normal"))
      case _ =>
    }
    gameStatus=RESIZE
    publish(new GridSizeChanged(newSize))
  }


  override def createNewGrid(size: Int): Unit = {
    size match {
      case 1 => grid = injector.instance[GridInterface](Names.named("tiny"))
      case 4 => grid = injector.instance[GridInterface](Names.named("small"))
      case 9 => grid = injector.instance[GridInterface](Names.named("normal"))
      case _ =>
    }
    grid = grid.createNewGrid(size)
    gameStatus = NEW
    publish(new CellChanged)
  }

  def gridToString: String = grid.toString

  def set(row: Int, col: Int, value: Int): Unit = {
    undoManager.doStep(new SetCommand(row, col, value, this))
    gameStatus = SET
    publish(new CellChanged)
  }

  def solve: Unit = {
    undoManager.doStep(new SolveCommand(this))
    gameStatus = SOLVED
    publish(new CellChanged)
  }

  def undo: Unit = {
    undoManager.undoStep
    gameStatus = UNDO
    publish(new CellChanged)
  }

  def redo: Unit = {
    undoManager.redoStep
    gameStatus = REDO
    publish(new CellChanged)
  }

  def cell(row:Int, col:Int) = grid.cell(row,col)

  def isGiven(row: Int, col: Int):Boolean = grid.cell(row, col).given
  def isSet(row:Int, col:Int):Boolean = grid.cell(row, col).isSet
  def available(row:Int, col:Int):Set[Int] = grid.available(row, col)
  def showCandidates(row:Int, col:Int):Unit = {
    grid=grid.setShowCandidates(row, col)
    gameStatus = CANDIDATES
    publish(new CandidatesChanged)
  }

  def isShowCandidates(row:Int, col:Int):Boolean = grid.cell(row, col).showCandidates
  def gridSize:Int = grid.size
  def blockSize:Int = Math.sqrt(grid.size).toInt
  def isShowAllCandidates:Boolean = showAllCandidates
  def toggleShowAllCandidates:Unit = {
    showAllCandidates = !showAllCandidates
    gameStatus = CANDIDATES
    publish(new CellChanged)
  }
  def isHighlighted(row:Int, col: Int):Boolean = grid.isHighlighted(row, col)
  def statusText:String = GameStatus.message(gameStatus)
  def highlight(index:Int):Unit = {
    grid = grid.highlight(index)
    publish(new CellChanged)
  }

}