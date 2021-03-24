/**
 * Created by ndrsh on 10.11.20
 */

package com.totec.trading.engine.exchange.instrument.impls.coinbase

import com.totec.parser.json.FastJsonReader
import com.totec.trading.core.exchanges.SymbolConverter
import com.totec.trading.core.instrument.CurrenciesAllowed
import com.totec.trading.core.instrument.InfoDeserializer
import com.totec.trading.core.instrument.InstrumentInfo
import javax.inject.Inject

class CoinbaseInfoDeserializer @Inject constructor(override val currenciesAllowed: CurrenciesAllowed,
                                                   override val symbolConverter: SymbolConverter) : InfoDeserializer {
	/**
	 * You don't have to start the object with [FastJsonReader.startObject], but you have to close it with [FastJsonReader.endObject]
	 */
	override fun FastJsonReader.readObject(): InstrumentInfo? {
		TODO("Not yet implemented")
	}
	
	/**
	 * Care that you [currenciesAllowed] might get changed.
	 */
	override fun InstrumentInfo.passesFilter(): Boolean {
		TODO("Not yet implemented")
	}
}
