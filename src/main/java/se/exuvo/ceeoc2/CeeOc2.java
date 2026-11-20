package se.exuvo.ceeoc2;

import li.cil.oc2.api.bus.device.provider.BlockDeviceProvider;
import li.cil.oc2.api.util.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(CeeOc2.MOD_ID)
public final class CeeOc2 {
    public static final String MOD_ID = "cee_oc2";

    private static final DeferredRegister<BlockDeviceProvider> BLOCK_DEVICE_PROVIDERS =
        DeferredRegister.create(Registries.BLOCK_DEVICE_PROVIDER, MOD_ID);

    public CeeOc2(final IEventBus modEventBus) {
        BLOCK_DEVICE_PROVIDERS.register("electric_gauge", ElectricGaugeDeviceProvider::new);
        BLOCK_DEVICE_PROVIDERS.register(modEventBus);
    }
}
