package codes.mark.geilematte.components

import codes.mark.geilematte.LatexText
import codes.mark.geilematte.facades.SafeKaTex
import japgolly.scalajs.react.vdom.prefix_<^._
import japgolly.scalajs.react.{Callback, ReactComponentB, ReactNode, TopNode}

import scalaz.\/

object Matte {
  def setShit(el:TopNode, ls:LatexText) = {
    Callback {
      SafeKaTex.render(ls.raw, el).fold(
        ())(e => el.innerHTML = e)
    }
  }

  def component =
    ReactComponentB[LatexText]("Math component")
      .initialState(Option.empty[String])
      .render($ => $.state.fold(
        <.span("Replace me, baby")
      )(errStr => <.span(errStr))
      )
      .componentDidMount(cdm => {
        val el = cdm.getDOMNode
        val math = cdm.props
        setShit(el, math)
      })
      .componentDidUpdate(cdu => {
        val el = cdu.$.getDOMNode
        val math = cdu.currentProps
        setShit(el, math)
      })
      .build
}
