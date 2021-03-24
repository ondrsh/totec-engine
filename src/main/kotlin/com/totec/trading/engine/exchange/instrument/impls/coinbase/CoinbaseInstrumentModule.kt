/**
 * Created by ndrsh on 6/26/20
 */

package com.totec.trading.engine.exchange.instrument.impls.coinbase

import com.totec.trading.core.instrument.InstrumentInfo
import com.totec.trading.engine.exchange.impls.InstrumentInfoImpl
import com.totec.trading.engine.exchange.instrument.impls.InstrumentModule
import dagger.Module
import dagger.Provides

@Module
class CoinbaseInstrumentModule : InstrumentModule() {
	@Provides fun info(instrumentInfoImpl: InstrumentInfoImpl): InstrumentInfo = instrumentInfoImpl
}
