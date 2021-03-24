/**
 * Created by ndrsh on 7/10/20
 */

package com.totec.trading.engine.exchange.impls.gdax

import com.totec.trading.core.exchanges.Exchange
import com.totec.trading.core.exchanges.SymbolConverter
import com.totec.trading.core.instrument.*
import com.totec.trading.core.networking.InstrumentInfoDeserializer
import com.totec.trading.core.networking.RestClient
import com.totec.trading.engine.exchange.InstrumentsMap
import com.totec.trading.engine.exchange.instrument.impls.BookModule
import com.totec.trading.engine.exchange.instrument.impls.coinbase.DaggerCoinbaseInstrumentComponent
import javax.inject.Inject

class CoinbaseExchange @Inject constructor(override val restClient: RestClient,
                                           override val instrumentInfoDeserializer: InstrumentInfoDeserializer,
                                           override val infoDeserializer: InfoDeserializer,
                                           @InstrumentsMap override val instruments: MutableMap<String, Instrument>) : Exchange {
	
	override val name: Exchange.Name = Exchange.Name.COINBASE
	override var symbolConverter: SymbolConverter = instrumentInfoDeserializer.symbolConverter
	override lateinit var instrumentComponent: InstrumentComponent
	
	@Inject
	fun injectComponent(instrumentProcessEnginesComponent: InstrumentProcessEnginesComponent,
	                    instrumentInfoFilter: CurrenciesAllowed,
	                    bookModule: BookModule) {
		this.instrumentComponent = DaggerCoinbaseInstrumentComponent.factory()
			.create(instrumentProcessEnginesComponent, instrumentInfoFilter, this, bookModule)
	}
}
