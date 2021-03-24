/**
 * Created by ndrsh on 7/7/20
 */

package com.totec.trading.engine.exchange.impls.bitmex

import com.totec.trading.core.exchanges.SymbolConverter
import com.totec.trading.core.instrument.InstrumentInfo
import com.totec.trading.core.instrument.currencies.Currency
import com.totec.trading.core.instrument.currencies.CurrencyPair
import com.totec.trading.core.utils.logger
import org.apache.logging.log4j.Level
import javax.inject.Inject

class BitmexSymbolConverter @Inject constructor() : SymbolConverter {
	val map: HashMap<String, CurrencyPair> = hashMapOf()
	override fun getCurrencyPair(info: InstrumentInfo): CurrencyPair {
		return info.pair
	}
	
	override fun getCurrency(symbol: String) = when (symbol) {
		"XBT" -> Currency.BTC
		else  -> try {
			Currency.valueOf(symbol)
		} catch (ex: Exception) {
			logger.log(Level.ERROR, "BitmexSymbolConverter cannot convert currency $symbol")
			Currency.INVALID
		}
	}
}
