/**
 * Created by ndrsh on 6/9/20
 */

package com.totec.trading.engine.exchange.impls.gdax

import com.totec.trading.core.exchanges.ExchangeComponent
import com.totec.trading.core.instrument.CurrenciesAllowed
import com.totec.trading.core.instrument.Instrument
import com.totec.trading.core.instrument.InstrumentProcessEnginesComponent
import com.totec.trading.core.networking.RestClientComponent
import com.totec.trading.core.partition.PartitionScope
import com.totec.trading.engine.exchange.InstrumentsMap
import com.totec.trading.engine.exchange.instrument.impls.BookModule
import dagger.BindsInstance
import dagger.Component

@Component(modules = [CoinbaseExchangeModule::class],
           dependencies = [RestClientComponent::class, InstrumentProcessEnginesComponent::class])
@PartitionScope
interface CoinbaseExchangeComponent : ExchangeComponent {
	
	@Component.Factory
	interface Factory {
		fun create(restClientComponent: RestClientComponent,
		           instrumentProcessEnginesComponent: InstrumentProcessEnginesComponent,
		           @BindsInstance currenciesAllowed: CurrenciesAllowed,
		           @BindsInstance bookModule: BookModule = BookModule(),
		           @BindsInstance @InstrumentsMap instruments: MutableMap<String, Instrument> = hashMapOf()): CoinbaseExchangeComponent
	}
}
