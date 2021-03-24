/**
 * Created by ndrsh on 6/30/20
 */

package com.totec.trading.engine.exchange.instrument.impls.bitmex

import com.totec.trading.core.instrument.InstrumentInfo
import com.totec.trading.engine.exchange.instrument.impls.InstrumentModule
import dagger.Module
import dagger.Provides

@Module
class BitmexInstrumentModule : InstrumentModule() {
	@Provides
	fun info(bitmexInfo: BitmexInfo): InstrumentInfo = bitmexInfo
}
