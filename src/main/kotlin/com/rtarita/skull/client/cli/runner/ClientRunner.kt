package com.rtarita.skull.client.cli.runner

import com.github.ajalt.mordant.markdown.Markdown
import com.github.ajalt.mordant.rendering.BorderType
import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.rendering.TextStyle
import com.github.ajalt.mordant.table.SectionBuilder
import com.github.ajalt.mordant.table.table
import com.github.ajalt.mordant.terminal.Terminal
import com.rtarita.skull.client.cli.action.BootstrapAction
import com.rtarita.skull.client.cli.action.ShutdownAction
import com.rtarita.skull.client.cli.util.truncate
import com.rtarita.skull.common.Card
import com.rtarita.skull.common.GameState
import com.rtarita.skull.common.PlayerGameState
import com.rtarita.skull.common.TurnMode

object ClientRunner {
    private const val MAX_TABLE_CELL_LENGTH = 30
    private val rowGroupHeaderStyle = TextStyle(TextColors.magenta, bold = true)

    suspend fun run() {
        val context = ClientContext()

        context.terminal.println("Welcome to Skull Game!")

        executeAndUpdate(context, BootstrapAction)

        do {
            val nextAction = updateScreen(context)
            executeAndUpdate(context, nextAction)

            context.terminal.prompt(
                prompt = "continue (enter)",
                promptSuffix = "? "
            )
        } while (nextAction != ShutdownAction)
    }

    private suspend fun executeAndUpdate(context: ClientContext, action: com.rtarita.skull.client.cli.action.Action) {
        for (pre in action.preActions) {
            context.clientState = pre.execute(context)
        }
        context.clientState = action.execute(context)
        for (post in action.postActions) {
            context.clientState = post.execute(context)
        }
    }

    private fun updateScreen(context: ClientContext): com.rtarita.skull.client.cli.action.Action {
        context.terminal.clear()
        context.terminal.println(Markdown("# ${context.clientState.msg}"))

        if (context.clientState.token != null) {
            val table = table {
                borderType = BorderType.SQUARE_DOUBLE_SECTION_SEPARATOR
                header {
                    context.clientState.login?.also { row("login", if (context.clientState.adminMode) "$it (admin)" else it) }
                    context.clientState.token?.also { row("token", it.truncate(MAX_TABLE_CELL_LENGTH)) }
                    context.clientState.gameid?.also { row("game id", it) }
                }

                val gs = context.clientState.lastGameState
                val pgs = context.clientState.lastPlayerGameState
                body {
                    if (gs != null) {
                        gameState(gs)
                    } else if (pgs != null) {
                        playerGameState(pgs)
                    }
                }

                captionBottom("Available actions: ${context.clientState.availableActions.joinToString { it.command }}")
            }

            context.terminal.println(table)
        }

        context.terminal.println()
        val answer = context.terminal.prompt("next action", choices = context.clientState.availableActions.map { it.command })
        return context.clientState.availableActions.find { it.command == answer } ?: ShutdownAction
    }

    private fun Terminal.clear() {
        cursor.move {
            clearScreen()
        }
    }

    private fun SectionBuilder.gameState(state: GameState) {
        commonGameStateProps1(
            state.numPlayers,
            state.roundCount,
            state.lastRoundBeginner,
            state.currentTurn,
            state.currentTurnMode
        )
        cardLists("available cards", state.cardsAvailable)
        cardLists("cards on table", state.cardsOnTable)
        cardLists("cards in hand", state.cardsInHand)

        commonGameStateProps2(
            state.bids,
            state.points
        ) {
            if (state.revealedCards.isNotEmpty()) {
                revealedCards(state.revealedCards)
            }
        }
    }

    private fun SectionBuilder.playerGameState(state: PlayerGameState) {
        row("player index", "#${state.playerIndex}")
        commonGameStateProps1(
            state.numPlayers,
            state.roundCount,
            state.lastRoundBeginner,
            state.currentTurn,
            state.currentTurnMode
        )
        row("own available cards", state.ownCardsAvailable.joinToString())
        playerStats("available cards of others", state.allCardsAvailable.filterKeys { it != state.playerIndex })
        row("own cards on table", state.ownCardsOnTable.joinToString())
        playerStats("cards on table of others", state.allCardsOnTable.filterKeys { it != state.playerIndex })
        row("own cards in hand", state.ownCardsInHand.joinToString())
        playerStats("cards in hand of others", state.allCardsInHand.filterKeys { it != state.playerIndex })

        commonGameStateProps2(
            state.bids,
            state.points
        ) {
            if (state.revealedCards.any { (_, v) -> v.isNotEmpty() }) {
                playerStats("revealed cards", state.revealedCards) { it.joinToString() }
            }
        }
    }

    private fun SectionBuilder.commonGameStateProps1(
        numPlayers: Int,
        roundCount: Int,
        lastRoundBeginner: Int,
        currentTurn: Int,
        currentTurnMode: TurnMode,
    ) {
        row("number of players", numPlayers)
        row("round #", roundCount)
        row("last round beginner", "#$lastRoundBeginner")
        row("current turn", "#$currentTurn")
        row("current turn mode", currentTurnMode)
    }

    private fun SectionBuilder.commonGameStateProps2(
        bids: Map<Int, Int>,
        points: Map<Int, Int>,
        printRevealedCards: SectionBuilder.() -> Unit
    ) {
        val filteredBids = bids.filterValues { it > 0 }
        if (filteredBids.isNotEmpty()) {
            playerStats("bids", filteredBids)
        }
        printRevealedCards()
        val filteredPoints = points.filterValues { it > 0 }
        if (filteredPoints.isNotEmpty()) {
            playerStats("points", filteredPoints)
        }
    }

    private fun SectionBuilder.cardLists(title: String, lists: Map<Int, List<Card>>) = playerStats(title, lists) {
        it.joinToString()
    }

    private fun SectionBuilder.revealedCards(revealed: List<Pair<Int, Int>>) = playerStats(
        "revealed cards",
        revealed.groupBy(Pair<Int, Int>::first) { (_, v) -> v }
            .mapValues { (_, v) -> v.sorted() }
    ) {
        it.joinToString()
    }

    private fun <T : Any> SectionBuilder.playerStats(title: String, stats: Map<Int, T>, transform: (T) -> String = Any::toString) {
        row(title, "") { style = rowGroupHeaderStyle }
        for ((pIdx, stat) in stats.entries.sortedBy { (k, _) -> k }) {
            row("#$pIdx", transform(stat))
        }
    }
}
