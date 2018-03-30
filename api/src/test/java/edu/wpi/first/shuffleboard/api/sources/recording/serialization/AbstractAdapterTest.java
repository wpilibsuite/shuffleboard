package edu.wpi.first.shuffleboard.api.sources.recording.serialization;

import edu.wpi.first.shuffleboard.api.data.DataTypes;

import com.google.common.collect.ImmutableList;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import java.util.ArrayList;
import java.util.Collection;

public class AbstractAdapterTest<T> {

  private static Collection<TypeAdapter> adapters;
  protected final TypeAdapter<T> adapter;
  protected final ImmutableList<TypeAdapter> requirements;

  public AbstractAdapterTest(TypeAdapter<T> adapter, TypeAdapter... requirements) {
    this.adapter = adapter;
    this.requirements = ImmutableList.copyOf(requirements);
  }

  @BeforeAll
  public static void getOldAdapters() {
    adapters = new ArrayList<>(Serializers.getAdapters());
  }

  @AfterAll
  public static void resetAdapters() {
    adapters.forEach(Serializers::add);
  }

  @BeforeEach
  public final void registerAdapter() {
    DataTypes dataTypes = new DataTypes();
    dataTypes.registerIfAbsent(adapter.getDataType());
    requirements.forEach(r -> dataTypes.registerIfAbsent(r.getDataType()));
    DataTypes.setDefault(dataTypes);
    Serializers.add(adapter);
    requirements.forEach(Serializers::add);
  }

  @AfterEach
  public final void unregisterAdapter() {
    Serializers.remove(adapter);
    requirements.forEach(Serializers::remove);
    DataTypes.setDefault(new DataTypes());
  }

}
