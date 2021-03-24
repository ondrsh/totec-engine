/**
 * Created by ndrsh on 14.11.20
 */

package com.totec.trading.engine.exchange.instrument.ipces

import com.totec.trading.core.engines.*
import com.totec.trading.core.exchanges.Exchange
import com.totec.trading.core.instrument.InstrumentInfo
import com.totec.trading.core.instrument.Liquidation
import com.totec.trading.core.instrument.book.Book
import com.totec.trading.core.instrument.book.Trade
import com.totec.trading.core.instrument.book.ops.BookOp
import javax.inject.Inject

class SampleEngines @Inject constructor(@Samplers private val sampleEngines: Set<SampleEngine>): BookEngine, InfoEngine, TradeEngine, LiquidationEngine, LagEngine {
	
	override lateinit var exchange: Exchange
	override lateinit var instrumentInfo: InstrumentInfo
	
	override fun addAskOp(bookOp: BookOp) {
		for(sampler in sampleEngines) {
			bookOp.incCount()
			sampler.addOp(bookOp)
		}
		bookOp.decCount()
	}
	
	override fun addBidOp(bookOp: BookOp) {
		addAskOp(bookOp)
	}
	
	override fun addTrade(trade: Trade) {
		for(sampler in sampleEngines) {
			trade.incCount()
			sampler.addTrade(trade)
		}
		trade.decCount()
	}
	
	override fun addBook(book: Book) {
		// empty
	}
	
	override fun addInfoFull(instrumentInfo: InstrumentInfo) {
		TODO("Not yet implemented")
	}
	
	override fun addInfoUpdate(instrumentInfo: InstrumentInfo) {
		TODO("Not yet implemented")
	}
	
	override fun addBookLag(lag: Long, timestamp: Long) {
		TODO("Not yet implemented")
	}
	
	override fun addTradeLag(lag: Long, timestamp: Long) {
		TODO("Not yet implemented")
	}
	
	override fun addInstrumentLag(lag: Long, timestamp: Long) {
		TODO("Not yet implemented")
	}
	
	override fun deleteLiquidation(liquidation: Liquidation) {
		TODO("Not yet implemented")
	}
	
	override fun insertLiquidation(liquidation: Liquidation) {
		TODO("Not yet implemented")
	}
	
	// TODO("Check that we flush the Instrument Info")
	override fun flush(timestamp: Long) {
		TODO("Not yet implemented")
	}
}
