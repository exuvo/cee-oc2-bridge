package se.exuvo.ceeoc2;

import com.george_vi.electroenergetics.content.gauge.ElectricGaugeBlockEntity;
import li.cil.oc2.api.bus.device.io.IOOutputStream;
import li.cil.oc2.api.bus.device.object.Callback;
import li.cil.oc2.api.bus.device.object.IOCallback;
import li.cil.oc2.api.bus.device.object.IODeviceDescription;
import li.cil.oc2.api.bus.device.object.RPCDeviceDescription;

import java.io.IOException;

// This being a record means equals() matches devices from repeated queries for the same
// block entity; otherwise the bus would remove and re-add the device on every scan.
//
// All methods are unsynchronized, so calls reply within the same VM step instead of waiting
// for the next server tick. They only read plain fields of the block entity, which the
// electrical simulation writes on the server thread once per tick, so the worst case is a
// reading that is one tick old.
@RPCDeviceDescription(typeName = "electric_gauge", description = """
    A Create Electro Energetics voltmeter or ammeter. Readings are the RMS value measured by the gauge, \
    in volts for voltmeters and amperes for ammeters.""")
@IODeviceDescription(name = "EGAUGE", description = """
    A Create Electro Energetics voltmeter or ammeter. Readings are the RMS value measured by the gauge, \
    in volts for voltmeters and amperes for ammeters.\n\
    All multi-byte values are low byte first. Fixed point readings are signed 32-bit integers in thousandths \
    of a unit (mV or mA), clamped to the signed 32-bit range. Exact readings are IEEE-754 doubles (8 bytes).""")
public record ElectricGaugeDevice(ElectricGaugeBlockEntity gauge) {
    // Ammeters measure the voltage across a 0.01 ohm shunt, see GaugeDevice.
    private static final double AMMETER_SHUNT_RESISTANCE = 0.01;

    private static final int GET_VALUE_CODE = 1;
    private static final int GET_RAW_VALUE_CODE = 2;
    private static final int GET_VALUE_EXACT_CODE = 3;
    private static final int GET_RAW_VALUE_EXACT_CODE = 4;
    private static final int GET_TYPE_CODE = 5;
    private static final int GET_SCALE_CODE = 6;

    // --------------------------------------------------------------------- //

    @Callback(synchronize = false, description = "Gets the reading of the gauge with its scale setting applied, as shown by goggles but unrounded.",
        returnValueDescription = "the signed reading, in volts or amperes.")
    public double getValue() {
        return rawValue() * scale();
    }

    @Callback(synchronize = false, description = "Gets the reading of the gauge without its scale setting applied.",
        returnValueDescription = "the signed reading, in volts or amperes.")
    public double getRawValue() {
        return rawValue();
    }

    @Callback(synchronize = false, description = "Gets the scale setting of the gauge.",
        returnValueDescription = "the factor readings are multiplied by, from 0.001 to 1000.")
    public double getScale() {
        return scale();
    }

    @Callback(synchronize = false, description = "Gets which kind of gauge this is.",
        returnValueDescription = "\"voltmeter\" or \"ammeter\".")
    public String getType() {
        return gauge.voltmeter ? "voltmeter" : "ammeter";
    }

    @Callback(synchronize = false, description = "Gets the unit of the readings.",
        returnValueDescription = "\"V\" or \"A\".")
    public String getUnit() {
        return gauge.voltmeter ? "V" : "A";
    }

    // --------------------------------------------------------------------- //

    @IOCallback(value = GET_VALUE_CODE, synchronize = false,
        description = "Gets the reading of the gauge with its scale setting applied.",
        resultsDescription = "four bytes, the signed reading in thousandths of a volt or ampere.")
    public void getValue(final IOOutputStream results) throws IOException {
        writeFixedPoint(results, rawValue() * scale());
    }

    @IOCallback(value = GET_RAW_VALUE_CODE, synchronize = false,
        description = "Gets the reading of the gauge without its scale setting applied.",
        resultsDescription = "four bytes, the signed reading in thousandths of a volt or ampere.")
    public void getRawValue(final IOOutputStream results) throws IOException {
        writeFixedPoint(results, rawValue());
    }

    @IOCallback(value = GET_VALUE_EXACT_CODE, synchronize = false,
        description = "Gets the exact reading of the gauge with its scale setting applied.",
        resultsDescription = "eight bytes, the reading in volts or amperes as an IEEE-754 double.")
    public void getValueExact(final IOOutputStream results) throws IOException {
        writeDouble(results, rawValue() * scale());
    }

    @IOCallback(value = GET_RAW_VALUE_EXACT_CODE, synchronize = false,
        description = "Gets the exact reading of the gauge without its scale setting applied.",
        resultsDescription = "eight bytes, the reading in volts or amperes as an IEEE-754 double.")
    public void getRawValueExact(final IOOutputStream results) throws IOException {
        writeDouble(results, rawValue());
    }

    @IOCallback(value = GET_TYPE_CODE, synchronize = false,
        description = "Gets which kind of gauge this is.",
        resultsDescription = "one byte, `0` for a voltmeter, `1` for an ammeter.")
    public void getType(final IOOutputStream results) throws IOException {
        results.writeU8(gauge.voltmeter ? 0 : 1);
    }

    @IOCallback(value = GET_SCALE_CODE, synchronize = false,
        description = "Gets the scale setting of the gauge.",
        resultsDescription = "one byte, the signed exponent `n` of the scale factor `10^n`, from `-3` to `3`.")
    public void getScale(final IOOutputStream results) throws IOException {
        results.writeU8((int) Math.round(Math.log10(scale())));
    }

    // --------------------------------------------------------------------- //

    private double rawValue() {
        return gauge.voltmeter ? gauge.voltage : gauge.voltage / AMMETER_SHUNT_RESISTANCE;
    }

    private double scale() {
        return gauge.scaling != null ? gauge.scaling.getScale() : 1;
    }

    private static void writeFixedPoint(final IOOutputStream results, final double value) throws IOException {
        final double milli = Math.rint(value * 1000);
        results.writeU32((int) Math.max(Integer.MIN_VALUE, Math.min(Integer.MAX_VALUE, milli)));
    }

    private static void writeDouble(final IOOutputStream results, final double value) throws IOException {
        final long bits = Double.doubleToRawLongBits(value);
        results.writeU32(bits);
        results.writeU32(bits >>> 32);
    }
}
