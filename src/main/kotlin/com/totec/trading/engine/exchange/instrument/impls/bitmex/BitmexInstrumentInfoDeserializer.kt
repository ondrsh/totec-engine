/**
 * Created by ndrsh on 7/7/20
 */

package com.totec.trading.engine.exchange.instrument.impls.bitmex

import com.dslplatform.json.JsonReader
import com.totec.trading.core.exchanges.SymbolConverter
import com.totec.trading.core.instrument.CurrenciesAllowed
import com.totec.trading.core.instrument.InstrumentInfo
import com.totec.trading.core.instrument.currencies.Currency
import com.totec.trading.core.networking.InstrumentInfoDeserializer
import com.totec.trading.core.utils.DoubleList
import com.totec.trading.core.utils.LongList
import com.totec.trading.core.utils.dsljson.*
import com.totec.trading.core.utils.isValid
import java.time.Instant
import javax.inject.Inject

class BitmexInstrumentInfoDeserializer @Inject constructor(override val currenciesAllowed: CurrenciesAllowed,
                                                           override val symbolConverter: SymbolConverter) : InstrumentInfoDeserializer {
	
	private val periodOffset = Instant.parse("2000-01-01T00:00:00.000Z").toEpochMilli()
	private fun JsonReader<*>.readNullablePeriod(): Long {
		val timestamp = readNullableTimestamp()
		return if (timestamp.isValid()) {
			timestamp - periodOffset
		} else Long.MIN_VALUE
	}
	
	override fun InstrumentInfo.passesFilter(): Boolean {
		if (isIndex()) return false
		return pair in currenciesAllowed && pair.base in currenciesAllowed
	}
	
	override fun JsonReader<*>.readObject(): BitmexInfo? {
		// we don't need startObject() because readSet() starts it for every object
		// but we need to do endObject()
		
		val info = BitmexInfo(System.currentTimeMillis())
		var token: Byte = '{'.toByte()
		nextAttribute@ while (token != '}'.toByte()) { // should be ,
			nextToken // "
			val string = readString()
			nextToken // :
			when {
				string == "symbol"              -> {
					nextToken // start symbol
					info.symbol = readString()
					if (info.symbol[0] == '.') {
						fillObjectOrArray('{')
						return null
					}
				}
				string == "typ"                 -> {
					nextToken // start typ
					when (val typ = readString()) {
						"FFWCSX", "FFCCSX" -> {
							info.typ = typ
						}
						else               -> {
							fillObjectOrArray('{')
							return null
						}
					}
				}
				string == "lotSize"             -> {
					if (isNumberOrCompleteNull()) {
						info.lotSize.initializeWith(readDouble())
					}
				}
				string == "tickSize"            -> {
					if (isNumberOrCompleteNull()) {
						info.tickSize.initializeWith(readDouble())
					}
				}
				string == "underlying"          -> {
					nextToken // start currencyString
					val currencyString = readString()
					if (currencyString.isNotEmpty()) {
						info.pair.base = symbolConverter.getCurrency(currencyString)
						if (info.pair.base == Currency.INVALID) {
							fillObjectOrArray('{')
							return null
						}
					}
				} // base currency
				string == "quoteCurrency"       -> {
					nextToken // start currencyString
					val currencyString = readString()
					if (currencyString.isNotEmpty()) {
						info.pair.quote = symbolConverter.getCurrency(currencyString)
						if (info.pair.quote == Currency.INVALID) {
							fillObjectOrArray('{')
							return null
						}
					}
				}  // quote currency
				string == "positionCurrency"    -> {
					nextToken // start currencyString
					val currencyString = readString()
					if (currencyString.isNotEmpty()) {
						info.pair.position = symbolConverter.getCurrency(currencyString)
						if (info.pair.position == Currency.INVALID) {
							fillObjectOrArray('{')
							return null
						}
					}
				}  // quote currency
				// a value that should be put into our bitmexInfo
				info.data.keys.contains(string) -> {
					when (val list = info.data[string]) {
						is LongList       -> {
							when {
								info.isTimestamp(string)  -> {
									val timestamp = readNullableTimestamp()
									if (timestamp.isValid()) {
										list.add(timestamp)
									}
								}
								info.isTimePeriod(string) -> {
									val period = readNullablePeriod()
									if (period.isValid()) {
										list.add(period)
									}
								}
								else                      -> {
									if (isNumberOrCompleteNull()) {
										list.add(readLong())
									}
								}
							}
						}
						is DoubleList     -> {
							if (isNumberOrCompleteNull()) {
								val dbl = readDouble()
								list.add(dbl)
							}
						}
						// if it's List<*>, it has to be List<String>, there is nothing else
						is MutableList<*> -> {
							nextToken // "
							val value = readString()
							@Suppress("UNCHECKED_CAST")
							(list as MutableList<String>).add(value)
						}
					}
				}
				// something we don't need - skip it
				else                            -> {
					token = toNextCommaOrClose()
					continue@nextAttribute
				}
				
			}
			token = nextToken // , or }
		}
		return info
	}
	
	private fun InstrumentInfo.isIndex() = symbol.startsWith('.')
}