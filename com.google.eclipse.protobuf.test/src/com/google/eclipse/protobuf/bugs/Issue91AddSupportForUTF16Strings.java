/*
 * Copyright (c) 2011 Google Inc.
 *
 * All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.google.eclipse.protobuf.bugs;

import static com.google.eclipse.protobuf.junit.core.Setups.unitTestSetup;
import static com.google.eclipse.protobuf.junit.core.XtextRule.createWith;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

import com.google.eclipse.protobuf.junit.core.XtextRule;
import com.google.eclipse.protobuf.protobuf.Protobuf;

import org.junit.*;

/**
 * Tests fix for <a href="http://code.google.com/p/protobuf-dt/issues/detail?id=91">Issue 91</a>.
 * 
 * @author alruiz@google.com (Alex Ruiz)
 */
public class Issue91AddSupportForUTF16Strings {

  @Rule public XtextRule xtext = createWith(unitTestSetup());
  
  private Protobuf root;

  @Before public void setUp() {
    root = xtext.root();
  }
  
  //  message Foo {
  //    optional string bar = 1 [default="\\302\\265"];
  //  }
  @Test public void should_recognize_UTF16_strings() {
    assertThat(root, notNullValue());
  }
}
