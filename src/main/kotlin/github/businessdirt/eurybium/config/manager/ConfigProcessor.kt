package github.businessdirt.eurybium.config.manager

import github.businessdirt.eurybium.EurybiumMod
import github.businessdirt.eurybium.config.EurybiumConfig
import io.github.notenoughupdates.moulconfig.processor.MoulConfigProcessor

class ConfigProcessor : MoulConfigProcessor<EurybiumConfig>(EurybiumMod.config)