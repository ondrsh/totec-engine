/**
 * Created by ndrsh on 7/10/20
 */

package com.totec.trading.engine.exchange.instrument.impls.coinbase

import com.dslplatform.json.JsonReader
import com.totec.trading.core.exchanges.SymbolConverter
import com.totec.trading.core.instrument.InstrumentInfo
import com.totec.trading.core.instrument.CurrenciesAllowed
import com.totec.trading.core.networking.InstrumentInfoDeserializer
import javax.inject.Inject

class CoinbaseInstrumentInfoDeserializer @Inject constructor(override val currenciesAllowed: CurrenciesAllowed,
                                                             override val symbolConverter: SymbolConverter) : InstrumentInfoDeserializer {
	/**
	 * You don't have to start the object with [JsonReader.startObject], but you have to close it with [JsonReader.endObject]
	 */
	override fun JsonReader<*>.readObject(): InstrumentInfo? {
		TODO("Not yet implemented")
	}
	
	/**
	 * Care that you [currenciesAllowed] might get changed.
	 */
	override fun InstrumentInfo.passesFilter(): Boolean {
		TODO("Not yet implemented")
	}
}
