package github.businessdirt.eurybium;

import github.businessdirt.eurybium.config.Config;
import github.businessdirt.eurybium.utils.Reference;
import github.businessdirt.eurybium.utils.Scheduler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Eurybium implements ModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger(Eurybium.class);

    public Eurybium() {

    }

    @Override
    public void onInitialize() {
        ClientTickEvents.END_CLIENT_TICK.register(this::tick);
        Config.init();

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal(Reference.MOD_ID)
                    .executes(context -> {
                        Config.get().display();
                        return 1;
                    }));
        });
    }

    public void tick(MinecraftClient client) {
        Scheduler.INSTANCE.tick();
    }
}
