/**
 * Created by ndrsh on 7/10/20
 */

package com.totec.trading.engine.exchange.impls.bitmex

import com.totec.trading.core.exchanges.Exchange
import com.totec.trading.core.exchanges.SymbolConverter
import com.totec.trading.core.instrument.CurrenciesAllowed
import com.totec.trading.core.instrument.InfoDeserializer
import com.totec.trading.core.instrument.Instrument
import com.totec.trading.core.instrument.InstrumentProcessEnginesComponent
import com.totec.trading.core.networking.InstrumentInfoDeserializer
import com.totec.trading.core.networking.RestClient
import com.totec.trading.engine.exchange.InstrumentsMap
import com.totec.trading.engine.exchange.instrument.impls.BookModule
import com.totec.trading.engine.exchange.instrument.impls.bitmex.BitmexInstrumentComponent
import com.totec.trading.engine.exchange.instrument.impls.bitmex.DaggerBitmexInstrumentComponent
import javax.inject.Inject

class BitmexExchangeImpl @Inject constructor(override val restClient: RestClient,
                                             override val instrumentInfoDeserializer: InstrumentInfoDeserializer,
                                             override val infoDeserializer: InfoDeserializer,
                                             @InstrumentsMap override val instruments: MutableMap<String, Instrument>) : Exchange {
	
	override val name: Exchange.Name = Exchange.Name.BITMEX
	override var symbolConverter: SymbolConverter = instrumentInfoDeserializer.symbolConverter
	override lateinit var instrumentComponent: BitmexInstrumentComponent
	
	@Inject
	fun injectComponent(instrumentProcessEnginesComponent: InstrumentProcessEnginesComponent,
	                    currenciesAllowed: CurrenciesAllowed,
	                    bookModule: BookModule) {
		this.instrumentComponent = DaggerBitmexInstrumentComponent.factory()
			.create(instrumentProcessEnginesComponent, currenciesAllowed, this, bookModule)
	}
}
