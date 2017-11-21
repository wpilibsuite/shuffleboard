package edu.wpi.first.shuffleboard.api.sources.recording.serialization;

import edu.wpi.first.shuffleboard.api.data.DataTypes;

import com.google.common.collect.ImmutableList;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

public class AbstractAdapterTest<T> {

  protected final TypeAdapter<T> adapter;
  protected final ImmutableList<TypeAdapter> requirements;

  public AbstractAdapterTest(TypeAdapter<T> adapter, TypeAdapter... requirements) {
    this.adapter = adapter;
    this.requirements = ImmutableList.copyOf(requirements);
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
