package com.totec.trading.engine.exchange.impls.bitmex

import com.totec.trading.core.instrument.currencies.Currency
import com.totec.trading.core.instrument.currencies.CurrencyPair
import com.totec.trading.core.utils.DoubleList
import com.totec.trading.core.utils.LongList
import com.totec.trading.core.utils.getInvalid
import com.totec.trading.engine.exchange.instrument.impls.bitmex.BitmexInfo
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.doubles.shouldBeExactly
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.longs.shouldBeExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldMatch
import java.time.Instant

@Suppress("UNCHECKED_CAST")
class BitmexInfoTest : StringSpec() {
	
	init {
		
		val info1 = BitmexInfo(15_000_000_000)
		// string
		info1.addToStringList(info1.data["state"] as MutableList<String>, "Open")
		// timestamp
		info1.addToLongList(info1.data["timestamp"] as LongList, Instant.parse("2020-07-03T16:45:00.000Z").toEpochMilli())
		// long
		info1.addToLongList(info1.data["volume"] as LongList, 23998835299235L)
		info1.addToLongList(info1.data["volume24h"] as LongList, 835299235L)
		// double
		info1.addToDoubleList(info1.data["midPrice"] as DoubleList, 9026.5)
		info1.addToDoubleList(info1.data["askPrice"] as DoubleList, 9027.0)
		
		val info2 = BitmexInfo(15_000_000_005)
		// string
		info2.addToStringList(info2.data["state"] as MutableList<String>, "Closed")
		// timestamp
		info2.addToLongList(info2.data["timestamp"] as LongList, Instant.parse("2020-07-12T17:45:00.000Z").toEpochMilli())
		// long
		info2.addToLongList(info2.data["volume24h"] as LongList, 9252528252L)
		// double
		info2.addToDoubleList(info2.data["midPrice"] as DoubleList, 9025.0)
		
		"update bitmexInfo" {
			val wasLiveUpdate = info1.update(info2)
			wasLiveUpdate shouldBe true
			
			// string
			val stateList = info1.data["state"] as MutableList<String>
			stateList.first() shouldMatch "Open"
			stateList.last() shouldMatch "Closed"
			stateList.size shouldBeExactly 2
			
			// timestamp
			val timestampList = info1.data["timestamp"] as LongList
			timestampList.last() shouldBeExactly Instant.parse("2020-07-12T17:45:00.000Z").toEpochMilli()
			timestampList[0] shouldBeExactly Instant.parse("2020-07-03T16:45:00.000Z").toEpochMilli()
			timestampList.index shouldBeExactly 2
			
			// long
			val volumeList = info1.data["volume"] as LongList
			val volume24hList = info1.data["volume24h"] as LongList
			volumeList.last() shouldBeExactly 23998835299235L
			volumeList.index shouldBeExactly 1
			volume24hList.last() shouldBeExactly 9252528252L
			volume24hList[0] shouldBeExactly 835299235L
			volume24hList.index shouldBeExactly 2
			
			// double
			val midPriceList = info1.data["midPrice"] as DoubleList
			val askPriceList = info1.data["askPrice"] as DoubleList
			askPriceList.last() shouldBeExactly 9027.0
			midPriceList.last() shouldBeExactly 9025.0
			midPriceList[0] shouldBeExactly 9026.5
			midPriceList.index shouldBeExactly 2
			
			// changed
			info1.changed shouldContainExactly setOf("state", "timestamp", "volume24h", "midPrice")
			
			// lastUpdate
			info1.timestamp shouldBeExactly 15_000_000_005L
		}
		
		"replace" {
			val infoFirst = BitmexInfo(Long.getInvalid())
			val infoSecond = BitmexInfo(15_000_000_005).apply {
				symbol = "ETHXBT"
				typ = "LALA_TYP"
				pair = CurrencyPair(Currency.ETH, Currency.BTC, Currency.BTC)
				tickSize.initializeWith(6.333)
				addToDoubleList(data["midPrice"] as DoubleList, 8025.0)
			}
			
			infoFirst.replace(infoSecond)
			infoFirst.symbol shouldMatch "ETHXBT"
			infoFirst.typ shouldMatch "LALA_TYP"
			infoFirst.pair.base shouldBe Currency.ETH
			infoFirst.pair.quote shouldBe Currency.BTC
			infoFirst.pair.position shouldBe Currency.BTC
			infoFirst.tickSize.isInitialized shouldBe true
			infoFirst.tickSize.value shouldBeExactly 6.333
			infoFirst.lotSize.isInitialized shouldBe false
			val midPriceList = infoFirst.data["midPrice"] as DoubleList
			midPriceList.index shouldBeExactly 1
			midPriceList.last() shouldBeExactly 8025.0
			infoFirst.changed.size shouldBeExactly 0
		}
		
		"update fail partly because timestamp too large" {
			info1.changed.clear()
			val currentTime = info1.timestamp
			
			val info3 = BitmexInfo(currentTime + (info1.maxMinutesConsideredUpdate + 1) * 60_000)
			info3.addToDoubleList(info3.data["bidPrice"] as DoubleList, 9020.0)
			val isLiveUpdate1 = info1.update(info3)  // 1 minute too much, no update, just set
			isLiveUpdate1 shouldBe false
			val bidPriceList1 = info1.data["bidPrice"] as DoubleList
			bidPriceList1.last() shouldBeExactly 9020.0
			info1.changed shouldContainExactly setOf()
			
			val info4 = BitmexInfo(currentTime + (info1.maxMinutesConsideredUpdate -1) * 60_000)
			info4.addToDoubleList(info4.data["bidPrice"] as DoubleList, 9018.0)
			val isLiveUpdate2 = info1.update(info4)  // 1 minute less, so update, set as changed
			isLiveUpdate2 shouldBe true
			val bidPriceList2 = info1.data["bidPrice"] as DoubleList
			bidPriceList2.last() shouldBeExactly 9018.0
			info1.changed shouldContainExactly setOf("bidPrice")
		}
	}
	
}