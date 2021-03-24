/**
 * Created by ndrsh on 15.11.20
 */

package com.totec.trading.engine.exchange.instrument.book

import com.totec.trading.core.instrument.CurrenciesAllowed
import com.totec.trading.core.instrument.DaggerDummyEngineComponent
import com.totec.trading.core.instrument.DaggerInstrumentProcessEnginesComponent
import com.totec.trading.core.instrument.DummyInstrumentProcessEngine
import com.totec.trading.core.instrument.book.BookEntry
import com.totec.trading.core.instrument.book.ops.OpType
import com.totec.trading.core.networking.DaggerDummyRestClientComponent
import com.totec.trading.core.utils.setUpLogger
import com.totec.trading.engine.exchange.impls.gdax.DaggerCoinbaseExchangeComponent
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.doubles.shouldBeExactly
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.longs.shouldBeExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldNotBeSameInstanceAs

class BookToBookOpsTest : StringSpec() {
	
	init {
		val instrumentInfoFilter = CurrenciesAllowed()
		val dummyEngineComponent = DaggerDummyEngineComponent.create()
		val enginesComponent = DaggerInstrumentProcessEnginesComponent.factory().create(listOf(dummyEngineComponent))
		val bitmexExchangeComponent = DaggerCoinbaseExchangeComponent.factory()
			.create(DaggerDummyRestClientComponent.create(), enginesComponent, instrumentInfoFilter)
		val instrument = bitmexExchangeComponent.exchange().instrumentComponent.instrument()
		setUpLogger()
		
		"basic bookops test" {
			val initialBook = instrument.exchange.instrumentComponent.book()
			
			initialBook.asks.engines = instrument.engines
			initialBook.asks.lastUpdated = 15000L
			initialBook.bids.engines = instrument.engines
			initialBook.bids.lastUpdated = 15000L
			
			val asks = listOf(
				BookEntry.getNext(15202.0, 250.0, 15000L),
				BookEntry.getNext(15203.5, 400.0, 15000L),
				BookEntry.getNext(15204.0, 1200.0, 15000L)
			)
			val bids = listOf(
				BookEntry.getNext(15201.0, 150.0, 15000L),
				BookEntry.getNext(15199.5, 400.0, 15000L),
				BookEntry.getNext(15198.0, 800.0, 15000L)
			)
			initialBook.asks.set.addAll(asks)
			initialBook.asks.map.putAll(asks.associateBy { it.price })
			initialBook.bids.set.addAll(bids)
			initialBook.bids.map.putAll(bids.associateBy { it.price })
			
			instrument.book.timestamp shouldBeExactly Long.MIN_VALUE
			val dummyProcessEngine = instrument.engines.bookEngines.first() as DummyInstrumentProcessEngine
			dummyProcessEngine.tradesReceived shouldBeExactly 0L
			dummyProcessEngine.bidsReceived shouldBeExactly 0L
			dummyProcessEngine.asksReceived shouldBeExactly 0L
			dummyProcessEngine.booksReceived shouldBeExactly 0L
			dummyProcessEngine.trades.size shouldBeExactly 0
			dummyProcessEngine.bidOps.size shouldBeExactly 0
			dummyProcessEngine.askOps.size shouldBeExactly 0
			dummyProcessEngine.books.size shouldBeExactly 0
			
			instrument.processBook(initialBook)
			dummyProcessEngine.books.size shouldBeExactly 1
			dummyProcessEngine.booksReceived shouldBeExactly 1
			instrument.book.timestamp shouldBeExactly 15_000
			instrument.book.bids shouldNotBeSameInstanceAs initialBook.bids
			instrument.book.asks shouldNotBeSameInstanceAs initialBook.asks
			instrument.book.bids.set.size shouldBeExactly 3
			instrument.book.asks.set.size shouldBeExactly 3
			instrument.book.bids.set.first().price shouldBeExactly 15201.0
			instrument.book.bids.set.first().amount shouldBeExactly 150.0
			instrument.book.asks.set.first().price shouldBeExactly 15202.0
			instrument.book.asks.set.first().amount shouldBeExactly 250.0
			
			instrument.flush(15001L)
			dummyProcessEngine.books.size shouldBeExactly 0
			dummyProcessEngine.booksReceived shouldBeExactly 0
			
			
			// adding an asks on the 2nd level and updating the two lower ones
			val updateAsks = listOf(
				BookEntry.getNext(15202.4, 200.0, 15000L),
				BookEntry.getNext(15203.5, 420.0, 15000L), // +20 qty
				BookEntry.getNext(15204.0, 1155.0, 15000L) // -45 qty
			)
			// adding a higher bid and updating the two others
			val updateBids = listOf(
				BookEntry.getNext(15201.5, 200.0, 15004L),
				BookEntry.getNext(15199.5, 450.0, 15003L), // +50 qty
				BookEntry.getNext(15198.0, 720.0, 15003L) // -80 qty
			)
			val updateBook = instrument.exchange.instrumentComponent.book()
			updateBook.asks.engines = instrument.engines
			updateBook.asks.lastUpdated = updateAsks.maxOf { it.timestamp }
			updateBook.bids.engines = instrument.engines
			updateBook.bids.lastUpdated = updateBids.maxOf { it.timestamp }
			updateBook.asks.set.addAll(updateAsks)
			updateBook.asks.map.putAll(updateAsks.associateBy { it.price })
			updateBook.bids.set.addAll(updateBids)
			updateBook.bids.map.putAll(updateBids.associateBy { it.price })
			
			instrument.processBook(updateBook)
			dummyProcessEngine.books.size shouldBeExactly 0
			dummyProcessEngine.booksReceived shouldBeExactly 0
			dummyProcessEngine.asksReceived shouldBeExactly 4
			dummyProcessEngine.bidsReceived shouldBeExactly 4
			
			
			val askOpsIter = dummyProcessEngine.askOps.iterator()
			askOpsIter.next().let {
				it.type shouldBe OpType.DELETE
				it.price shouldBeExactly 15202.0
				it.amount shouldBeExactly 250.0
				it.survived shouldBeExactly 4
				it.timestamp shouldBeExactly 15_004L
			}
			askOpsIter.next().let {
				it.type shouldBe OpType.INSERT
				it.price shouldBeExactly 15202.4
				it.amount shouldBeExactly 200.0
			}
			askOpsIter.next().let {
				it.type shouldBe OpType.CHANGE
				it.price shouldBeExactly 15203.5
				it.amount shouldBeExactly 20.0
				it.survived shouldBeExactly 0
			}
			askOpsIter.next().let {
				it.type shouldBe OpType.CHANGE
				it.price shouldBeExactly 15204.0
				it.amount shouldBeExactly -45.0
				it.survived shouldBeExactly 0
			}
			askOpsIter.hasNext() shouldBe false
			
			val bidOpsIter = dummyProcessEngine.bidOps.iterator()
			bidOpsIter.next().let {
				it.type shouldBe OpType.DELETE
				it.price shouldBeExactly 15201.0
				it.amount shouldBeExactly 150.0
				it.timestamp shouldBeExactly 15_004L
			}
			bidOpsIter.next().let {
				it.type shouldBe OpType.INSERT
				it.price shouldBeExactly 15201.5
				it.amount shouldBeExactly 200.0
			}
			bidOpsIter.next().let {
				it.type shouldBe OpType.CHANGE
				it.price shouldBeExactly 15199.5
				it.amount shouldBeExactly 50.0
				it.survived shouldBeExactly 3
				it.timestamp shouldBeExactly 15_003L
			}
			bidOpsIter.next().let {
				it.type shouldBe OpType.CHANGE
				it.price shouldBeExactly 15198.0
				it.amount shouldBeExactly -80.0
				it.survived shouldBeExactly 3
				it.timestamp shouldBeExactly 15_003L
			}
			bidOpsIter.hasNext() shouldBe false
			
			dummyProcessEngine.bidsReceived shouldBeExactly 4
			instrument.book.timestamp shouldBeExactly 15_004
			instrument.book.bids.lastUpdated shouldBeExactly 15_004
			instrument.book.asks.lastUpdated shouldBeExactly 15_004 // because when we process the new book, we use the maximum of both sides
			instrument.book.bids shouldNotBeSameInstanceAs updateBook.bids
			instrument.book.asks shouldNotBeSameInstanceAs updateBook.asks
			instrument.book.bids.set.size shouldBeExactly 3
			instrument.book.asks.set.size shouldBeExactly 3
			instrument.book.bids.set.first().price shouldBeExactly 15201.5
			instrument.book.bids.set.last().amount shouldBeExactly 720.0
			instrument.book.asks.set.last().amount shouldBeExactly 1155.0
		}
	}
}
