/**
 * Created by ndrsh on 5/20/20
 */

@file:Suppress("UNCHECKED_CAST")

package com.totec.trading.engine.exchange.instrument.impls.bitmex

import com.totec.trading.core.instrument.InstrumentInfo
import com.totec.trading.core.instrument.Size
import com.totec.trading.core.instrument.currencies.CurrencyPair
import com.totec.trading.core.utils.DoubleList
import com.totec.trading.core.utils.LongList
import com.totec.trading.core.utils.removeAllButLast
import net.openhft.chronicle.bytes.BytesIn
import net.openhft.chronicle.bytes.BytesOut
import javax.inject.Inject

/**
 * Class that resembles the Bitmex State, the websocket instrument updates are applied to it. The [data] map contains the
 * properties mapped to its values (as lists). A unchanged, normal list should only have one value (the current value). If a property gets changed,
 * the id gets added to the [changed] set and the value appended to its list in the [data] entry.
 *
 * Timestamp should get set by JsonReader with [System.currentTimeMillis()] in live, after that by ReadFeed. Calculate the lags with
 * differences between [timestamp] and last entry of "timestamp" in the [data] map.
 */
class BitmexInfo @Inject constructor(@InfoTimestamp override var timestamp: Long) : InstrumentInfo {
	
	override var lotSize: Size = Size.Lot()
	override var tickSize: Size = Size.Tick()
	override var pair: CurrencyPair = CurrencyPair.invalid()
	
	override var symbol: String = ""
	var typ: String = ""
	
	val data = hashMapOf(
		// strings
		"state" to mutableListOf<String>(),
		"lastTickDirection" to mutableListOf<String>(),
		
		// longs
		"timestamp" to LongList(),
		"fundingTimestamp" to LongList(),
		"fundingInterval" to LongList(),
		"prevTotalVolume" to LongList(),
		"totalVolume" to LongList(),
		"volume" to LongList(),
		"volume24h" to LongList(),
		"openInterest" to LongList(),
		"turnover" to LongList(),
		"turnover24h" to LongList(),
		
		// doubles
		"prevPrice24h" to DoubleList(),
		"vwap" to DoubleList(),
		"lastPrice" to DoubleList(),
		"lastPriceProtected" to DoubleList(),
		"lastChangePcnt" to DoubleList(),
		"fundingRate" to DoubleList(),
		"indicativeFundingRate" to DoubleList(),
		"bidPrice" to DoubleList(),
		"midPrice" to DoubleList(),
		"askPrice" to DoubleList(),
		"impactBidPrice" to DoubleList(),
		"impactMidPrice" to DoubleList(),
		"impactAskPrice" to DoubleList(),
		"fairBasisRate" to DoubleList(),
		"markPrice" to DoubleList(),
		"indicativeSettlePrice" to DoubleList())
	
	val changed = mutableSetOf<String>()
	
	override fun replace(otherInfo: InstrumentInfo) {
		super.replace(otherInfo)
		(otherInfo as BitmexInfo)
		addAllEntries(otherInfo)
		typ = otherInfo.typ
		changed.clear()
		timestamp = otherInfo.timestamp
	}
	
	@Suppress("UNCHECKED_CAST", "RedundantIf")
	override fun update(otherInfo: InstrumentInfo): Boolean {
		(otherInfo as BitmexInfo)
		addAllEntries(otherInfo)
		if ((otherInfo.timestamp - timestamp).isTooLargeGap()) {
			changed.clear()
		}
		timestamp = otherInfo.timestamp
		return if (changed.isEmpty()) false else true
	}
	
	private fun addAllEntries(otherInfo: BitmexInfo) {
		for (otherDataEntry in otherInfo.data) {
			when (otherDataEntry.value) {
				is MutableList<*> -> {
					val stringList = otherDataEntry.value as List<String>
					if (stringList.isNotEmpty()) {
						addToStringList(data[otherDataEntry.key] as MutableList<String>, stringList.last())
						changed.add(otherDataEntry.key)
					}
				}
				is DoubleList -> {
					val doubleList = otherDataEntry.value as DoubleList
					if (doubleList.isNotEmpty()) {
						addToDoubleList(data[otherDataEntry.key] as DoubleList, doubleList.last())
						changed.add(otherDataEntry.key)
					}
				}
				is LongList -> {
					val longList = otherDataEntry.value as LongList
					if (longList.isNotEmpty()) {
						addToLongList(data[otherDataEntry.key] as LongList, longList.last())
						changed.add(otherDataEntry.key)
					}
				}
			}
		}
	}
	
	fun addToStringList(thisStringList: MutableList<String>, value: String) {
		if (thisStringList.isNotEmpty() && thisStringList.last() == value) return
		// if different, so update it
		thisStringList.add(value)
	}
	
	fun addToDoubleList(thisDoubleList: DoubleList, value: Double) {
		if (thisDoubleList.isNotEmpty() && thisDoubleList.last() == value) return
		// if different, so update it
		thisDoubleList.add(value)
	}
	
	fun addToLongList(thisLongList: LongList, value: Long) {
		if (thisLongList.isNotEmpty() && thisLongList.last() == value) return
		// if different, so update it
		thisLongList.add(value)
	}
	
	override fun writeInfoUpdate(bytes: BytesOut<*>) {
		for (changedKey in changed) {
			bytes.write8bit(changedKey)
			when (val list = data[changedKey]) {
				is DoubleList -> {
					bytes.writeDouble(list.last())
				}
				is LongList -> {
					bytes.writeLong(list.last())
				}
				// has to be String then
				is MutableList<*> -> {
					bytes.write8bit(list.last() as String)
				}
			}
		}
		bytes.write8bit("end")
	}
	
	override fun writeInfoFull(bytes: BytesOut<*>) {
		// symbol
		bytes.write8bit("symbol")
		bytes.write8bit(symbol)
		
		// pair
		bytes.write8bit("pair")
		bytes.writeEnum(pair.base)
		bytes.writeEnum(pair.quote)
		bytes.writeEnum(pair.position)
		
		
		//tickSize
		if (tickSize.isInitialized) {
			bytes.write8bit("tickSize")
			bytes.writeDouble(tickSize.value)
		}
		
		if (typ.isNotEmpty()) {
			bytes.write8bit("typ")
			bytes.write8bit(typ)
		}
		
		for (entry in data) {
			when (val list = entry.value) {
				is LongList -> if (list.isNotEmpty()) {
					bytes.write8bit(entry.key)
					bytes.writeLong(list.last())
				}
				is DoubleList -> if (list.isNotEmpty()) {
					bytes.write8bit(entry.key)
					bytes.writeDouble(list.last())
				}
				// has to be String then
				is MutableList<*> -> if (list.isNotEmpty()) {
					bytes.write8bit(entry.key)
					bytes.write8bit(list.last() as String)
				}
			}
		}
		
		bytes.write8bit("end")
	}
	
	override fun readInfo(bytes: BytesIn<*>, readTimestamp: Long) {
		timestamp = readTimestamp
		while (true) {
			when (val key = bytes.read8bit()) {
				"symbol" -> bytes.setSymbol()
				"pair" -> bytes.setPair()
				"lotSize" -> lotSize.initializeWith(bytes.readDouble())
				"tickSize" -> tickSize.initializeWith(bytes.readDouble())
				"typ" -> typ = bytes.read8bit() as String
				"end" -> return
				else       -> {
					when (val x = data[key]) {
						is DoubleList -> addToDoubleList(x, bytes.readDouble())
						is LongList -> addToLongList(x, bytes.readLong())
						is MutableList<*> -> addToStringList(x as MutableList<String>, bytes.read8bit() as String) // has to be String
					}
				}
			}
		}
	}
	
	override fun cleanAfterUpdate() {
		for (changedKey in changed) {
			when (val list = data[changedKey]) {
				is DoubleList -> {
					list.removeAllButLast()
				}
				is LongList -> {
					list.removeAllButLast()
				}
				is MutableList<*> -> {
					list.removeAllButLast()
				}
			}
		}
		changed.clear()
	}
	
	override fun appendTo(stringBuilder: StringBuilder) {
		val dataToAdd = mutableListOf<String>()
		if(typ.isNotEmpty()) dataToAdd.add("typ:$typ")
		if (lotSize.isInitialized) dataToAdd.add("lotSize:${lotSize.value}")
		if (tickSize.isInitialized) dataToAdd.add("tickSize:${tickSize.value}")
		for (entry in data) {
			val list = entry.value
			if (list is LongList && list.isNotEmpty()) {
				dataToAdd.add("${entry.key}:${list.last()}")
			} else if (list is DoubleList && list.isNotEmpty()) {
				dataToAdd.add("${entry.key}:${list.last()}")
			} else if (list is List<*> && list.isNotEmpty()) {
				// List<String>
				dataToAdd.add("${entry.key}:${list.last()}") //
			}
		}
		stringBuilder.append(dataToAdd.joinToString())
	}
	
	override fun calculateLag() = timestamp - (data["timestamp"]!! as LongList).last()
	
	private fun Long.isTooLargeGap() = this / 60_000 > maxMinutesConsideredUpdate // 5 minutes
	val maxMinutesConsideredUpdate = 5
	
	fun isTimestamp(s: String) = when (s) {
		"timestamp", "fundingTimestamp" -> true
		else                            -> false
	}
	
	fun isTimePeriod(s: String) = when (s) {
		"fundingInterval" -> true
		else              -> false
	}
}
