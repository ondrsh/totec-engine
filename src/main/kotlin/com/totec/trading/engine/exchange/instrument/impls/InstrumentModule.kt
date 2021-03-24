/**
 * Created by ndrsh on 7/12/20
 */

package com.totec.trading.engine.exchange.instrument.impls

import com.totec.trading.core.instrument.Instrument
import com.totec.trading.core.utils.getInvalid
import com.totec.trading.engine.exchange.instrument.InstrumentImpl
import com.totec.trading.engine.exchange.instrument.impls.bitmex.InfoTimestamp
import dagger.Module
import dagger.Provides

@Module
open class InstrumentModule {
	@Provides
	fun instrument(instrumentImpl: InstrumentImpl): Instrument = instrumentImpl
	
	@Provides
	@InfoTimestamp
	fun infoTimestamp(): Long = Long.getInvalid()
}
