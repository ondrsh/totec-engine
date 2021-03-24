package com.totec.trading.engine.exchange.instrument.book

import com.totec.trading.core.instrument.CurrenciesAllowed
import com.totec.trading.core.instrument.DaggerDummyEngineComponent
import com.totec.trading.core.instrument.DaggerInstrumentProcessEnginesComponent
import com.totec.trading.core.instrument.book.BookEntry
import com.totec.trading.core.instrument.book.ops.BookOp
import com.totec.trading.core.instrument.book.ops.OpType
import com.totec.trading.core.networking.DaggerDummyRestClientComponent
import com.totec.trading.core.utils.roundAmountSafe
import com.totec.trading.core.utils.setUpLogger
import com.totec.trading.engine.exchange.impls.gdax.DaggerCoinbaseExchangeComponent
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.doubles.shouldBeExactly
import io.kotest.matchers.doubles.shouldBeGreaterThan
import io.kotest.matchers.doubles.shouldNotBeExactly
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.longs.shouldBeExactly
import io.kotest.matchers.maps.shouldNotContainKey
import io.kotest.matchers.maps.shouldNotContainValue
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.kotest.matchers.types.shouldNotBeSameInstanceAs
import kotlin.random.Random

class BookSideImplTest : StringSpec() {
	
	init {
		val instrumentInfoFilter = CurrenciesAllowed()
		val dummyEngineComponent = DaggerDummyEngineComponent.create()
		val enginesComponent = DaggerInstrumentProcessEnginesComponent.factory().create(listOf(dummyEngineComponent))
		val bitmexExchangeComponent = DaggerCoinbaseExchangeComponent.factory()
			.create(DaggerDummyRestClientComponent.create(), enginesComponent, instrumentInfoFilter)
		val instrument = bitmexExchangeComponent.exchange().instrumentComponent.instrument()
		
		val asks = instrument.book.asks
		val bids = instrument.book.bids
		setUpLogger()
		
		beforeSpec {
			Array(200) { it * 0.5 }.forEach {
				val bidInsert = BookOp.getInsert(price = 900 + it,
				                                 amount = Random.nextInt(0, 100_000).toDouble(),
				                                 timestamp = 1000)
				bids.processInsert(bidInsert)
				val askInsert = BookOp.getInsert(price = 1100 - it,
				                                 amount = Random.nextInt(0, 100_000).toDouble(),
				                                 timestamp = 1000)
				asks.processInsert(askInsert)
			}
		}
		
		"process insert" {
			bids.set.first().price shouldBeExactly 999.5
			bids.set.last().price shouldBeExactly 900.0
			bids.set.take(3).last().price shouldBeExactly 998.5
			
			val insertOp = BookOp.getInsert(price = 999.4,
			                                amount = 20.0,
			                                timestamp = 1000)
			val insertSuccess = bids.processInsert(insertOp)
			
			insertSuccess shouldBe true
			bids.set.take(2).last().price shouldBeExactly 999.4
			bids.set.take(2).last().amount shouldBeExactly 20.0
			asks.set.first().price shouldBeExactly 1000.5
			asks.set.last().price shouldBeExactly 1100.0
			asks.set.take(3).last().price shouldBeExactly 1001.5
			insertOp.distanceToTop shouldBeExactly 0.1
		}
		
		"process change" {
			// change the entry you inserted before
			@Suppress("BlockingMethodInNonBlockingContext")
			Thread.sleep(5)
			val changeOp = BookOp.getChange(price = 999.4,
			                                changeAmount = -4.5,
			                                timestamp = 1004)
			val changeResult = bids.processChange(changeOp)
			val oldEntry = bids.map[changeOp.price] ?: noEntryFoundError()
			
			// check general state
			changeResult shouldBe true
			bids.set.take(2).last() shouldBeSameInstanceAs oldEntry
			changeOp.distanceToTop shouldBeExactly 0.1
			// check amounts
			oldEntry.amount shouldBeExactly 15.5
			changeOp.amount shouldBeExactly -4.5
			// check timestamps
			changeOp.timestamp shouldBe 1004
			changeOp.survived shouldBe 4
			oldEntry.timestamp shouldBe 1004
		}
		
		"process faulty change by amount" {
			val beforeSet = bids.set.toList()
			val changeOp = BookOp.getChange(price = 999.4,
			                                changeAmount = -15.5,
			                                timestamp = 1010)
			val changeResult = bids.processChange(changeOp)
			val afterSet = bids.set.toList()
			
			changeResult shouldBe false
			beforeSet shouldHaveSameEntryValuesAs afterSet
			changeOp.distanceToTop shouldBe 0.1
			changeOp.timestamp shouldBeExactly 1010
			(bids.map[changeOp.price] ?: noEntryFoundError()).timestamp shouldBeExactly 1004
		}
		
		"process faulty change by price" {
			val allEntriesStart = bids.set.toList()
			// make a stupid change which should fail because there is no such entry
			val stupidChangeOp = BookOp.getChange(price = 999.929292,
			                                      changeAmount = -4.5,
			                                      timestamp = System.currentTimeMillis())
			val stupidChangeResult = bids.processChange(stupidChangeOp)
			val allEntriesEnd = bids.set.toList()
			stupidChangeResult shouldBe false
			allEntriesStart shouldHaveSameEntryValuesAs allEntriesEnd
		}
		
		"process change by insert" {
			val oldEntryAmount = (asks.map[1015.5] ?: noEntryFoundError()).amount
			val oldEntryTimestamp = (asks.map[1015.5] ?: noEntryFoundError()).timestamp
			val insertOp = BookOp.getInsert(price = 1015.5,
			                                amount = 10_500.0,
			                                timestamp = 1005)
			val insertSuccess = asks.processInsert(insertOp)
			val entry = asks.map[1015.5] ?: noEntryFoundError()
			
			// check amounts
			entry.amount shouldBeExactly 10_500.0
			insertOp.amount shouldBeExactly (entry.amount - oldEntryAmount).roundAmountSafe()
			// check timestamps
			oldEntryTimestamp shouldBeExactly 1000
			entry.timestamp shouldBeExactly 1005
			// check other
			insertSuccess shouldBe true
			insertOp.distanceToTop shouldBeExactly 15.0
			insertOp.type shouldBe OpType.CHANGE
		}
		
		"process change by insert fail" {
			val insert = BookOp.getInsert(price = 1005.0, amount = 0.0, timestamp = 1000L)
			val success = asks.processInsert(insert)
			
			success shouldBe false
			(asks.map[1005.0] ?: noEntryFoundError()).amount shouldBeGreaterThan 0.0
		}
		
		"process change by insert fail due to same amount" {
			val firstInsert = BookOp.getInsert(price = 990.12, amount = 1200.0, timestamp = 2000L)
			val firstSuccess = bids.processInsert(firstInsert)
			firstSuccess shouldBe true
			
			val secondInsert = BookOp.getInsert(price = 990.12, amount = 1200.0, timestamp = 2001L)
			val secondSuccess = bids.processInsert(secondInsert)
			if (secondSuccess == false) {
				secondInsert.decCount()
			}
			secondSuccess shouldBe false
			secondInsert.type shouldBe OpType.INSERT
			BookOp.getNext() shouldBeSameInstanceAs secondInsert
			
			val thirdInsert = BookOp.getInsert(price = 990.12, amount = 1100.0, timestamp = 2002L)
			val thirdSuccess = bids.processInsert(thirdInsert)
			if (thirdSuccess == false) {
				thirdInsert.decCount()
			}
			thirdSuccess shouldBe true
			BookOp.getNext() shouldNotBeSameInstanceAs thirdInsert
			thirdInsert.type shouldBe OpType.CHANGE
			thirdInsert.amount shouldBeExactly -100.0
			(bids.map[firstInsert.price] ?: noEntryFoundError()).amount shouldBeExactly 1100.0
		}
		
		"process delete" {
			val insertOp = BookOp.getInsert(price = 1010.2,
			                                amount = 50.0,
			                                timestamp = 1000)
			val insertSuccess = asks.processInsert(insertOp)
			val entry = asks.map[insertOp.price]
			val deleteOp = BookOp.getDelete(price = 1010.2,
			                                timestamp = 1003)
			val deleteSuccess = asks.processDelete(deleteOp)
			
			
			insertSuccess shouldBe true
			deleteSuccess shouldBe true
			deleteOp.distanceToTop shouldBeExactly 9.7
			deleteOp.type shouldBe OpType.DELETE
			deleteOp.timestamp shouldBeExactly 1003
			deleteOp.survived shouldBeExactly 3
			deleteOp.amount shouldBeExactly 50.0
			deleteOp.price shouldBeExactly 1010.2
			asks.set shouldNotContain entry
			asks.map shouldNotContainValue entry
		}
		
		"process faulty delete" {
			val deleteOp = BookOp.getDelete(price = 1010.3,
			                                timestamp = System.currentTimeMillis())
			val deleteSuccess = asks.processDelete(deleteOp)
			
			deleteSuccess shouldBe false
			asks.map shouldNotContainKey deleteOp.price
			asks.set.forEach {
				it.price shouldNotBeExactly deleteOp.price
			}  
		}
	}
	
	private fun noEntryFoundError(): Nothing = error("couldn't find entry in map")
	
	private infix fun List<BookEntry>.shouldHaveSameEntryValuesAs(other: List<BookEntry>) {
		size shouldBeExactly other.size
		forEachIndexed { index, bookEntry ->
			bookEntry.amount shouldBeExactly other[index].amount
			bookEntry.price shouldBeExactly other[index].price
			bookEntry.timestamp shouldBeExactly other[index].timestamp
		}
	}
}