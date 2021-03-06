/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-present Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.repository.golang.internal.hosted;

import org.sonatype.goodies.testsupport.TestSupport;
import org.sonatype.nexus.repository.golang.GolangFormat;
import org.sonatype.nexus.repository.types.HostedType;
import org.sonatype.nexus.repository.types.HostedType;
import org.sonatype.nexus.repository.view.handlers.HighAvailabilitySupportChecker;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class GolangHostedRecipeTest
    extends TestSupport
{
  @Mock
  private HighAvailabilitySupportChecker highAvailabilitySupportChecker;

  private String GO_NAME = "go";

  private GolangHostedRecipe underTest;

  @Before
  public void setUp() throws Exception {
    underTest = new GolangHostedRecipe(highAvailabilitySupportChecker, new HostedType(), new GolangFormat());
  }

  @After
  public void tearDown() throws Exception {
    System.getProperties().remove("nexus.golang.hosted");
  }

  @Test
  public void disabledByDefault() {
    when(highAvailabilitySupportChecker.isSupported(GO_NAME)).thenReturn(true);
    assertThat(underTest.isFeatureEnabled(), is(equalTo(false)));
    verify(highAvailabilitySupportChecker).isSupported(GO_NAME);
  }

  @Test
  public void enableGolang() {
    System.setProperty("nexus.golang.hosted", "true");
    when(highAvailabilitySupportChecker.isSupported(GO_NAME)).thenReturn(true);
    assertThat(underTest.isFeatureEnabled(), is(equalTo(true)));
    verify(highAvailabilitySupportChecker).isSupported(GO_NAME);
  }

  @Test
  public void disabledIfNexusIsClusteredAndGoHostedDisabled() {
    System.setProperty("nexus.golang.hosted", "false");
    when(highAvailabilitySupportChecker.isSupported(GO_NAME)).thenReturn(false);
    assertThat(underTest.isFeatureEnabled(), is(equalTo(false)));
    verify(highAvailabilitySupportChecker).isSupported(GO_NAME);
  }

  @Test
  public void disabledIfNexusIsClusteredAndGoHostedEnabled() {
    System.setProperty("nexus.golang.hosted", "true");
    when(highAvailabilitySupportChecker.isSupported(GO_NAME)).thenReturn(false);
    assertThat(underTest.isFeatureEnabled(), is(equalTo(false)));
    verify(highAvailabilitySupportChecker).isSupported(GO_NAME);
  }
}
