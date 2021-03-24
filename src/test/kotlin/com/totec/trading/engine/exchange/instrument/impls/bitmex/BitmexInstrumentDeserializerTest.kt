@file:Suppress("BlockingMethodInNonBlockingContext")

package com.totec.trading.engine.exchange.instrument.impls.bitmex

import com.dslplatform.json.DslJson
import com.totec.trading.core.instrument.*
import com.totec.trading.core.instrument.currencies.Currency
import com.totec.trading.core.networking.DaggerDummyRestClientComponent
import com.totec.trading.core.utils.DoubleList
import com.totec.trading.core.utils.LongList
import com.totec.trading.engine.exchange.impls.bitmex.BitmexSymbolConverter
import com.totec.trading.engine.exchange.impls.bitmex.DaggerBitmexExchangeComponent
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.doubles.shouldBeExactly
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.longs.shouldBeExactly
import io.kotest.matchers.shouldBe
import java.io.ByteArrayInputStream
import java.io.File

class BitmexInstrumentDeserializerTest : StringSpec() {
	
	val dsl = DslJson(DslJson.Settings<Any>().includeServiceLoader())
	val reader = dsl.newReader()
	val input: List<ByteArrayInputStream> = File("../totec-feed/feed-samples/bitmex/public.txt")
		.readLines()
		.map { it.byteInputStream() }
	val dummyEngineComponent: DummyEngineComponent = DaggerDummyEngineComponent.create()
	val enginesComponent = DaggerInstrumentProcessEnginesComponent.factory().create(listOf(dummyEngineComponent))
	val instrumentComponent = DaggerBitmexExchangeComponent.factory()
		.create(DaggerDummyRestClientComponent.create(), enginesComponent, CurrenciesAllowed())
	
	init {
		val instrumentLine = input[21]
		reader.process(instrumentLine)
		reader.startObject()
		reader.startAttribute("data")
		reader.nextToken shouldBe '['.toByte()
		
		"bitmex instrument deserializer test" {
			val infos = with(BitmexInstrumentInfoDeserializer(CurrenciesAllowed(), BitmexSymbolConverter())) { reader.readSet() }!!
			infos.size shouldBeExactly 14
			var swaps = 0
			var futures = 0
			infos.forEach {
				if (it.isBitmexFuture()) futures++
				if (it.isBitmexSwap()) swaps++
			}
			swaps shouldBeExactly 4
			futures shouldBeExactly 10
			
			val xbtInfo = infos.find { it.symbol == "XBTUSD" }!! as BitmexInfo
			val lastPrice = (xbtInfo.data["lastPrice"] as DoubleList).last()
			lastPrice shouldBeExactly 9085.5
			val impactMidPrice = (xbtInfo.data["impactMidPrice"] as DoubleList).last()
			impactMidPrice shouldBeExactly 9085.25
			val openInterest = (xbtInfo.data["openInterest"] as LongList).last()
			openInterest shouldBeExactly 676895327
			xbtInfo.pair.position shouldBe Currency.USD
			xbtInfo.pair.base shouldBe Currency.BTC
			xbtInfo.pair.quote shouldBe Currency.USD
		}
	}
	
}

fun InstrumentInfo.isBitmexSwap(): Boolean {
	return if (this is BitmexInfo) {
		typ == "FFWCSX"
	} else false
}

fun InstrumentInfo.isBitmexFuture(): Boolean {
	return if (this is BitmexInfo) {
		typ == "FFCCSX"
	} else false
}
