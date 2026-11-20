package se.exuvo.ceeoc2;

import com.george_vi.electroenergetics.content.gauge.ElectricGaugeBlockEntity;
import li.cil.oc2.api.bus.device.Device;
import li.cil.oc2.api.bus.device.object.ObjectDevice;
import li.cil.oc2.api.bus.device.provider.BlockDeviceProvider;
import li.cil.oc2.api.bus.device.provider.BlockDeviceQuery;

import java.util.Optional;

public final class ElectricGaugeDeviceProvider implements BlockDeviceProvider {
    @Override
    public Optional<Device> getDevice(final BlockDeviceQuery query) {
        if (query.getLevel().getBlockEntity(query.getQueryPosition()) instanceof final ElectricGaugeBlockEntity gauge) {
            return Optional.of(new ObjectDevice(new ElectricGaugeDevice(gauge)));
        }
        return Optional.empty();
    }
}
