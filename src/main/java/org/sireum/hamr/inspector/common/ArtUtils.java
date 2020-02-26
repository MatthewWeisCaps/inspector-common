package org.sireum.hamr.inspector.common;

import art.Bridge;
import art.UConnection;
import art.UPort;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import scala.collection.Iterator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Lazy
@Component("artUtils")
public class ArtUtils {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ArtUtils.class);

    private final List<Bridge> bridges;
    private final List<UPort> ports;
    private final List<UConnection> connections;

    /*
     * Since port and bridge ids are unique across the two categories, pre-allocated arrays can provide fast lookups.
     * Note that ARCH_BRIDGES_BY_ID and ARCH_PORTS_BY_ID could be replaced with one single Object[] array.
     */
    private final Bridge[] ARCH_BRIDGES_BY_ID;
    private final UPort[] ARCH_PORTS_BY_ID;
    private final Bridge[] PORT_ID_TO_BRIDGE;

    private final String commonBridgePrefix;

    public ArtUtils(InspectionBlueprint inspectionBlueprint) {
        this.bridges = initBridges(inspectionBlueprint);
        this.ports = initPorts(bridges);
        this.connections = initConnections(inspectionBlueprint);

        this.commonBridgePrefix = findCommonBridgePrefix();

        ARCH_BRIDGES_BY_ID = new Bridge[ports.size() + bridges.size()];
        ARCH_PORTS_BY_ID = new UPort[ports.size() + bridges.size()];
        PORT_ID_TO_BRIDGE = new Bridge[ports.size() + bridges.size()];

        for (Bridge bridge : bridges) {
            final int id = bridge.id().toInt();
            ARCH_BRIDGES_BY_ID[id] = bridge;
        }

        for (UPort port : ports) {
            final int id = port.id().toInt();
            ARCH_PORTS_BY_ID[id] = port;
            Bridge b = inspectionBlueprint.ad().components().elements().find(it -> it.ports().all().elements().contains(port)).get();
            PORT_ID_TO_BRIDGE[id] = b;
        }
    }

    private static List<Bridge> initBridges(InspectionBlueprint inspectionBlueprint) {
        final int size = inspectionBlueprint.ad().components().elements().size();
        final List<Bridge> bridges = new ArrayList<>(size);
        final Iterator<Bridge> bridgeIterator = inspectionBlueprint.ad().components().elements().toIterator();

        while (bridgeIterator.hasNext()) {
            bridges.add(bridgeIterator.next());
        }

        return Collections.unmodifiableList(bridges);
    }

    private static List<UPort> initPorts(List<Bridge> bridges) {
        final List<UPort> ports = new ArrayList<>();

        for (Bridge bridge : bridges) {
            final Iterator<UPort> portIterator = bridge.ports().all().elements().toIterator();
            while (portIterator.hasNext()) {
                ports.add(portIterator.next());
            }
        }

        return Collections.unmodifiableList(ports);
    }

    private static List<UConnection> initConnections(InspectionBlueprint inspectionBlueprint) {
        final int size = inspectionBlueprint.ad().connections().elements().size();
        final List<UConnection> connections = new ArrayList<>(size);
        final Iterator<UConnection> connectionIterator = inspectionBlueprint.ad().connections().elements().toIterator();

        while (connectionIterator.hasNext()) {
            connections.add(connectionIterator.next());
        }

        return Collections.unmodifiableList(connections);
    }

    @NotNull
    public List<Bridge> getBridges() {
        return bridges;
    }

    @NotNull
    public List<UPort> getPorts() {
        return ports;
    }

    @NotNull
    public List<UConnection> getConnections() {
        return connections;
    }

    /*
     * This method is called a LOT and uses a pre-allocated O(1) array as a result.
     */
    @NotNull
    public Bridge getBridge(int bridgeId) {
        boundsCheck(bridgeId, ARCH_BRIDGES_BY_ID.length);
        return ARCH_BRIDGES_BY_ID[bridgeId];
    }

    /*
     * This method is called a LOT and uses a pre-allocated O(1) array as a result.
     */
    @NotNull
    public Bridge getBridge(@NotNull UPort port) {
        boundsCheck(port.id().toInt(), PORT_ID_TO_BRIDGE.length);
        return PORT_ID_TO_BRIDGE[port.id().toInt()];
    }

    /*
     * This method is called a LOT and uses a pre-allocated O(1) array as a result.
     */
    @NotNull
    public UPort getPort(int portId) {
        boundsCheck(portId, ARCH_PORTS_BY_ID.length);
        return ARCH_PORTS_BY_ID[portId];
    }

    public String prettyPrint(@NotNull Bridge bridge) {
        return bridge.name().substring(commonBridgePrefix.length());
    }

    public static String prettyPrint(@NotNull UPort port, @NotNull Bridge bridge) {
        if (bridge.name().length() >= port.name().length()) {
            log.warn("Could not pretty print port {} for bridge {}, it is expected that the port's name contains " +
                    "the bridge's name as a prefix", port.name(), bridge.name());
            return port.name();
        }
        return port.name().substring(bridge.name().length() + 1); // commonPortPrefixMap.get(bridge).length()
    }

    public String prettyPrint(@NotNull UPort port) {
        return prettyPrint(port, getBridge(port));
    }

    public String informativePrettyPrint(@NotNull UPort port) {
        return String.format("%s [%s]", prettyPrint(port, getBridge(port)), port.mode().toString());
    }

    public String informativePrettyPrint(@NotNull Bridge bridge) {
        return String.format("%s [%s]", prettyPrint(bridge), bridge.dispatchProtocol().toString());
    }

    private String findCommonBridgePrefix() {

        // no need to store outside method because method is called once at initialization
        final String[] bridgeNames =
                bridges.stream().map(Bridge::name).toArray(String[]::new);

        if (bridgeNames.length == 0) {
            log.warn("ArtUtils was unable to find any bridge names when finding common bridge prefix.");
            return "";
        }

        return StringUtils.getCommonPrefix(bridgeNames);
    }

    /**
     * (Inspired by: https://stackoverflow.com/questions/625433/how-to-convert-milliseconds-to-x-mins-x-seconds-in-java)
     */
    public static String formatTime(long timeInMillis) {
        final StringBuilder timeBuilder = new StringBuilder();

        final long days = TimeUnit.MILLISECONDS.toDays(timeInMillis);
        timeInMillis -= TimeUnit.DAYS.toMillis(days);

        final long hours = TimeUnit.MILLISECONDS.toHours(timeInMillis);
        timeInMillis -= TimeUnit.HOURS.toMillis(hours);

        final long minutes = TimeUnit.MILLISECONDS.toMinutes(timeInMillis);
        timeInMillis -= TimeUnit.MINUTES.toMillis(minutes);

        final long seconds = TimeUnit.MILLISECONDS.toSeconds(timeInMillis);
        timeInMillis -= TimeUnit.SECONDS.toMillis(seconds);

        final long millis = TimeUnit.MILLISECONDS.toMillis(timeInMillis);
        timeInMillis -= TimeUnit.MILLISECONDS.toMillis(millis);

        boolean hasAnythingBeenWrittenYet = false;
        if (days > 0) {
            timeBuilder.append(String.format("%d d ", days));
            hasAnythingBeenWrittenYet = true;
        }

        if (hours > 0 || hasAnythingBeenWrittenYet) {
            timeBuilder.append(String.format("%d h ", hours));
            hasAnythingBeenWrittenYet = true;
        }

        if (minutes > 0 || hasAnythingBeenWrittenYet) {
            timeBuilder.append(String.format("%d m ", minutes));
            hasAnythingBeenWrittenYet = true;
        }

        if (seconds > 0 || hasAnythingBeenWrittenYet) {
            timeBuilder.append(String.format("%d s ", seconds));
            hasAnythingBeenWrittenYet = true;
        }

        timeBuilder.append(String.format("%d ms", millis));

        return timeBuilder.toString();
    }

    private static void boundsCheck(int index, int arraySize) throws IllegalArgumentException {
        if (index < 0 || arraySize < index) {
            log.error("Attempted to retrieve Arch data from illegal index. " +
                    "Is the target HAMR project up-to-date with the session being inspected?");
            throw new IllegalArgumentException("");
        }
    }

}
