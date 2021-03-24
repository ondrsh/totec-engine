/**
 * Created by ndrsh on 7/7/20
 */

package com.totec.trading.engine.exchange.impls.gdax

import com.totec.trading.core.exchanges.SymbolConverter
import com.totec.trading.core.instrument.InstrumentInfo
import com.totec.trading.core.instrument.currencies.Currency
import com.totec.trading.core.instrument.currencies.CurrencyPair
import javax.inject.Inject

class CoinbaseSymbolConverter @Inject constructor() : SymbolConverter{
	override fun getCurrencyPair(info: InstrumentInfo): CurrencyPair {
		TODO("Not yet implemented")
	}
	
	override fun getCurrency(symbol: String): Currency {
		TODO("Not yet implemented")
	}
}
