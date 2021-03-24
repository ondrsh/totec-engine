/**
 * Created by ndrsh on 6/19/20
 */

package com.totec.trading.engine.exchange.impls.bitmex

import com.totec.trading.core.exchanges.Exchange
import com.totec.trading.core.exchanges.SymbolConverter
import com.totec.trading.core.instrument.InfoDeserializer
import com.totec.trading.core.networking.InstrumentInfoDeserializer
import com.totec.trading.engine.exchange.instrument.impls.bitmex.BitmexInfoDeserializer
import com.totec.trading.engine.exchange.instrument.impls.bitmex.BitmexInstrumentInfoDeserializer
import dagger.Module
import dagger.Provides

@Module
class BitmexExchangeModule {
	@Provides fun provideExchange(bitmexExchangeImpl: BitmexExchangeImpl): Exchange = bitmexExchangeImpl
	@Provides fun provideInstrumentInfoDeserializer(bIID: BitmexInstrumentInfoDeserializer): InstrumentInfoDeserializer = bIID
	@Provides fun provideInfoDeserializer(bid: BitmexInfoDeserializer): InfoDeserializer = bid
	@Provides fun provideSymbolConverter(bitmexSymbolConverter: BitmexSymbolConverter): SymbolConverter = bitmexSymbolConverter
}
