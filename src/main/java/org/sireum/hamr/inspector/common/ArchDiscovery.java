package org.sireum.hamr.inspector.common;

import art.ArchitectureDescription;
import art.DataContent;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public interface ArchDiscovery {

    @NotNull
    ArchitectureDescription ad();

    @NotNull
    Function<String, DataContent> deserializeFn();

    @NotNull
    Function<DataContent, String> serializeFn();

}
