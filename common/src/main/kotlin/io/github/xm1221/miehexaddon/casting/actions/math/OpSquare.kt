package io.github.xm1221.miehexaddon.casting.actions.math

import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getDouble
import at.petrak.hexcasting.api.casting.iota.Iota

/** A pure (non-spell) example action: pops a number and pushes its square. Costs no media. */
object OpSquare : ConstMediaAction {
    override val argc = 1

    override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
        val x = args.getDouble(0, argc)
        return (x * x).asActionResult
    }
}
