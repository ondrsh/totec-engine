/**
 * Created by ndrsh on 14.11.20
 */

package com.totec.trading.engine.exchange.instrument.ipces

import com.totec.trading.core.instrument.InstrumentInfo
import com.totec.trading.core.instrument.book.Trade
import com.totec.trading.core.instrument.book.ops.BookOp

abstract class SampleEngine {
	
	private val currentOpsQueue = ArrayDeque<BookOp>(512)
	private val currentTradeQueue = ArrayDeque<Trade>(512)
	private val bookLags = ArrayDeque<Long>(512)
	private val tradeLags = ArrayDeque<Long>(512)
	private val instrumentLags = ArrayDeque<Long>(512)
	
	// TODO base this on running stuff so less computational effort
	protected abstract fun sampleDecision(): Boolean
	
	abstract fun addInfoUpdate(info: InstrumentInfo)
	
	fun addOp(op: BookOp) = currentOpsQueue.addLast(op)
	
	fun addTrade(trade: Trade) = currentTradeQueue.addLast(trade)
	
	fun addBookLag(lag: Long, timestamp: Long)  = bookLags.add(lag)
	
	fun addTradeLag(lag: Long, timestamp: Long) = tradeLags.add(lag)
	
	fun addInstrumentLag(lag: Long, timestamp: Long) = instrumentLags.add(lag)
}
