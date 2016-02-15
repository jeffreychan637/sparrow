// Code generated by Wire protocol buffer compiler, do not edit.
// Source file: sparrow.proto at 8:1
package edu.berkeley.cs194;

import com.squareup.wire.ProtoAdapter;
import com.squareup.wire.WireEnum;
import java.lang.Override;

/**
 * Specifies a feature that a Sparrow implementation supports.
 */
public enum Feature implements WireEnum {
  /**
   * All compliant implementations must support BASIC.
   */
  BASIC(1);

  public static final ProtoAdapter<Feature> ADAPTER = ProtoAdapter.newEnumAdapter(Feature.class);

  private final int value;

  Feature(int value) {
    this.value = value;
  }

  /**
   * Return the constant for {@code value} or null.
   */
  public static Feature fromValue(int value) {
    switch (value) {
      case 1: return BASIC;
      default: return null;
    }
  }

  @Override
  public int getValue() {
    return value;
  }
}