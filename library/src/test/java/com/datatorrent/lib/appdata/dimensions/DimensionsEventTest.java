/**
 * Copyright (c) 2015 DataTorrent, Inc.
 * All rights reserved.
 */
package com.datatorrent.lib.appdata.dimensions;

import java.util.Map;

import com.google.common.collect.Maps;

import org.junit.Assert;
import org.junit.Test;

import com.datatorrent.lib.appdata.gpo.GPOMutable;
import com.datatorrent.lib.appdata.schemas.FieldsDescriptor;
import com.datatorrent.lib.appdata.schemas.Type;
import com.datatorrent.lib.dimensions.DimensionsEvent.EventKey;

public class DimensionsEventTest
{
  @Test
  public void eventKeyEqualsHashCodeTest()
  {
    Map<String, Type> fieldToTypeA = Maps.newHashMap();
    fieldToTypeA.put("a", Type.LONG);
    fieldToTypeA.put("b", Type.STRING);

    FieldsDescriptor fdA = new FieldsDescriptor(fieldToTypeA);

    GPOMutable gpoA = new GPOMutable(fdA);
    gpoA.setField("a", 1L);
    gpoA.setField("b", "hello");

    EventKey eventKeyA = new EventKey(1, 1, 1, gpoA);

    Map<String, Type> fieldToTypeB = Maps.newHashMap();
    fieldToTypeB.put("a", Type.LONG);
    fieldToTypeB.put("b", Type.STRING);

    FieldsDescriptor fdB = new FieldsDescriptor(fieldToTypeB);

    GPOMutable gpoB = new GPOMutable(fdB);
    gpoB.setField("a", 1L);
    gpoB.setField("b", "hello");

    EventKey eventKeyB = new EventKey(1, 1, 1, gpoB);

    Assert.assertEquals("The two hashcodes should equal", eventKeyA.hashCode(), eventKeyB.hashCode());
    Assert.assertEquals("The two event keys should equal", eventKeyA, eventKeyB);
  }
}
