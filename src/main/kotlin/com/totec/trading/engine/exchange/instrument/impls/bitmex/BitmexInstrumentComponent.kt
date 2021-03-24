/**
 * Created by ndrsh on 6/26/20
 */

package com.totec.trading.engine.exchange.instrument.impls.bitmex

import com.totec.trading.core.exchanges.Exchange
import com.totec.trading.core.instrument.CurrenciesAllowed
import com.totec.trading.core.instrument.InstrumentComponent
import com.totec.trading.core.instrument.InstrumentProcessEnginesComponent
import com.totec.trading.core.instrument.InstrumentScope
import com.totec.trading.engine.exchange.instrument.impls.BookModule
import dagger.BindsInstance
import dagger.Component

@InstrumentScope
@Component(modules = [BitmexInstrumentModule::class, BookModule::class],
           dependencies = [InstrumentProcessEnginesComponent::class])
interface BitmexInstrumentComponent : InstrumentComponent {
	
	@Component.Factory
	interface Factory {
		fun create(instrumentProcessEnginesComponent: InstrumentProcessEnginesComponent,
		           @BindsInstance currenciesAllowed: CurrenciesAllowed,
		           @BindsInstance exchange: Exchange,
		           bookModule: BookModule): BitmexInstrumentComponent
	}
}
