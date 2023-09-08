package com.rtarita.skull.client.cli.action

import com.github.ajalt.mordant.rendering.TextStyles
import com.rtarita.skull.client.cli.runner.ClientContext
import com.rtarita.skull.client.cli.state.ClientState
import com.rtarita.skull.client.cli.util.authenticated
import com.rtarita.skull.client.cli.util.promptListElement
import com.rtarita.skull.common.Bid
import com.rtarita.skull.common.Card
import com.rtarita.skull.common.FirstCard
import com.rtarita.skull.common.Guess
import com.rtarita.skull.common.Lay
import com.rtarita.skull.common.Move
import com.rtarita.skull.common.MoveOutcome
import com.rtarita.skull.common.PlayerGameState
import com.rtarita.skull.common.TurnMode
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.path
import io.ktor.http.takeFrom

data object MoveAction : Action {
    override val name = "Move"
    override val description = "make a move in an active game"
    override val command = "move"

    override suspend fun execute(context: ClientContext): ClientState {
        val url = context.clientState.serverUrl ?: return context.clientState.copy(msg = "provide a server url")
        val gameid = context.clientState.gameid ?: return context.clientState.copy(msg = "provide a game id")
        val state = context.clientState.lastPlayerGameState ?: return context.clientState.copy(msg = "refresh game first")

        if (state.currentTurn >= 0 && state.currentTurn != state.playerIndex) {
            return context.clientState.copy(msg = "it's not your turn")
        }

        val possibleMoves = getPossibleMoves(state.currentTurnMode)

        val selectedMove = promptMove(context, possibleMoves)

        val move = when (selectedMove) {
            TurnMode.FIRST_CARD -> promptFirstCard(context, state)
            TurnMode.LAY -> promptLay(context, state)
            TurnMode.BID -> promptBid(context, state)
            TurnMode.GUESS -> promptGuess(context, state)
        }

        context.terminal.println(move)

        val response = makeRequest(context, url, gameid, move)

        return context.clientState.copy(
            msg = response.body<MoveOutcome>().toString(),
            readyForPrompt = false
        )
    }

    private fun getPossibleMoves(currentTurnMode: TurnMode) = when (currentTurnMode) {
        TurnMode.FIRST_CARD -> listOf(TurnMode.FIRST_CARD)
        TurnMode.LAY -> listOf(TurnMode.LAY, TurnMode.BID)
        TurnMode.BID -> listOf(TurnMode.BID)
        TurnMode.GUESS -> listOf(TurnMode.GUESS)
    }

    private fun promptMove(context: ClientContext, possibleMoves: List<TurnMode>): TurnMode {
        val selectedMove = possibleMoves.singleOrNull() ?: context.terminal.promptListElement(possibleMoves, "select move")

        context.terminal.println(TextStyles.bold("=== $selectedMove ==="))

        return selectedMove
    }

    private suspend fun makeRequest(context: ClientContext, url: String, gameid: String, move: Move) = context.http.post {
        url {
            takeFrom(url)
            path("move", gameid)
        }
        authenticated(context)
        contentType(ContentType.Application.Json)
        setBody(move)
    }

    private fun promptFirstCard(context: ClientContext, state: PlayerGameState): FirstCard = promptCard(
        context,
        state,
        "First Card",
        ::FirstCard
    )

    private fun promptLay(context: ClientContext, state: PlayerGameState): Lay = promptCard(
        context,
        state,
        "Lay Card",
        ::Lay
    )

    private fun promptBid(context: ClientContext, state: PlayerGameState): Bid {
        val lowerBound = state.bids.values.max() + 1
        val upperBound = state.allCardsOnTable.values.sum()
        val amount = context.terminal.prompt(
            prompt = "Bid",
            choices = (lowerBound..upperBound).map { it.toString() } + "-1"
        )?.toInt() ?: -1

        return Bid(amount)
    }

    private fun promptGuess(context: ClientContext, state: PlayerGameState): Guess {
        val playerIdx = if ((state.revealedCards[state.playerIndex]?.size ?: 0) >= state.ownCardsOnTable.size) {
            val possiblePlayers = state.allCardsOnTable
                .filter { (pIdx, numCards) -> numCards > (state.revealedCards[pIdx]?.size ?: 0) }
                .keys

            context.terminal.prompt(
                prompt = "reveal card from player #",
                choices = possiblePlayers.map { it.toString() }
            )?.toInt() ?: possiblePlayers.first()

        } else {
            context.terminal.println(TextStyles.bold("reveal your own cards"))
            state.playerIndex
        }

        val revealed = state.revealedCards[playerIdx]?.map { it.first } ?: emptyList()
        val possibleCards = (0..<state.allCardsOnTable.getValue(playerIdx)).filter { it !in revealed }

        val cardIdx = context.terminal.prompt(
            prompt = "reveal card #",
            choices = possibleCards.map { it.toString() }
        )?.toInt() ?: possibleCards.first()

        return Guess(playerIdx, cardIdx)
    }

    private fun <M : Move> promptCard(context: ClientContext, state: PlayerGameState, prompt: String, constructor: (Card) -> M): M {
        val card = context.terminal.promptListElement(state.ownCardsInHand.distinct(), prompt)

        return constructor(card)
    }
}
