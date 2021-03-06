/*
 * Copyright (c) 2020, Matthew Weis, Kansas State University
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.sireum.hamr.inspector.common;

import art.Bridge;
import art.UConnection;
import art.UPort;
import org.jetbrains.annotations.NotNull;
import scala.collection.Iterator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

/**
 * A helper class containing utility methods for querying and displaying information from an
 * {@link art.ArchitectureDescription}.
 *
 * This class is thread-safe and contains only pure methods. It is encouraged (but not required) for users to
 * create one instance of this class per {@link InspectionBlueprint} and to share the one instance across their project
 * as needed. (This class would be a singleton itself if {@link InspectionBlueprint} wasn't determined at runtime).
 *
 */
public final class ArtUtils {

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

    public static ArtUtils create(@NotNull InspectionBlueprint inspectionBlueprint) {
        return new ArtUtils(inspectionBlueprint);
    }

    ArtUtils(@NotNull InspectionBlueprint inspectionBlueprint) {
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
    public final List<Bridge> getBridges() {
        return bridges;
    }

    @NotNull
    public final List<UPort> getPorts() {
        return ports;
    }

    @NotNull
    public final List<UConnection> getConnections() {
        return connections;
    }

    /*
     * This method is called a LOT and uses a pre-allocated O(1) array as a result.
     */
    @NotNull
    public final Bridge getBridge(int bridgeId) {
        boundsCheck(bridgeId, ARCH_BRIDGES_BY_ID.length);
        return ARCH_BRIDGES_BY_ID[bridgeId];
    }

    /*
     * This method is called a LOT and uses a pre-allocated O(1) array as a result.
     */
    @NotNull
    public final Bridge getBridge(@NotNull UPort port) {
        boundsCheck(port.id().toInt(), PORT_ID_TO_BRIDGE.length);
        return PORT_ID_TO_BRIDGE[port.id().toInt()];
    }

    /*
     * This method is called a LOT and uses a pre-allocated O(1) array as a result.
     */
    @NotNull
    public final UPort getPort(int portId) {
        boundsCheck(portId, ARCH_PORTS_BY_ID.length);
        return ARCH_PORTS_BY_ID[portId];
    }

    public final String prettyPrint(@NotNull Bridge bridge) {
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

    public final String prettyPrint(@NotNull UPort port) {
        return prettyPrint(port, getBridge(port));
    }

    public final String informativePrettyPrint(@NotNull UPort port) {
        return String.format("%s (%s) [%s]", prettyPrint(port, getBridge(port)), port.mode().toString(), port.id().toString());
    }

    public final String informativePrettyPrint(@NotNull Bridge bridge) {
        return String.format("%s (%s) [%s]", prettyPrint(bridge), bridge.dispatchProtocol().toString(), bridge.id().toString());
    }

    private String findCommonBridgePrefix() {

        // no need to store outside method because method is called once at initialization
        final String[] bridgeNames =
                bridges.stream().map(Bridge::name).toArray(String[]::new);

        if (bridgeNames.length == 0) {
            log.warn("ArtUtils was unable to find any bridge names when finding common bridge prefix.");
            return "";
        }

        final int minNameLength = Stream.of(bridgeNames).mapToInt(String::length).min().orElse(0);

        final StringBuilder prefix = new StringBuilder(minNameLength);

        // for each char (up to the biggest prefix size)
        for (int ci=0; ci < minNameLength; ci++) {

            // get char of first bridge (safe because return if bridgeNames.length == 0 above)
            final char refChar = bridgeNames[0].charAt(ci);

            // check if all other bridges share the same common letter, if not then return what we have
            for (int bi=1; bi < bridgeNames.length; bi++) {
                if (refChar != bridgeNames[bi].charAt(ci)) {
                    return prefix.toString();
                }
            }

            // if all name have same character at index i, add the char to the string and continue
            prefix.append(refChar);
        }

        return prefix.toString();
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
            final String errorString = "Attempted to retrieve Arch data from illegal index " + index +
                    " for array of size " + arraySize + ".";
            final IllegalArgumentException ex = new IllegalArgumentException(errorString);
            log.error(errorString + " Is the target HAMR project up-to-date with the session being inspected?", ex);
            throw ex;
        }
    }

}
