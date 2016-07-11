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
package org.sonatype.nexus.repository.security;

import org.sonatype.goodies.testsupport.TestSupport;
import org.sonatype.nexus.repository.Format;
import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.security.internal.SimpleVariableResolverAdapter;
import org.sonatype.nexus.repository.view.Request;
import org.sonatype.nexus.selector.VariableSource;

import org.junit.Test;
import org.mockito.Mock;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

public class SimpleVariableResolverAdapterTest
    extends TestSupport
{
  @Mock
  Request request;

  @Mock
  Repository repository;

  @Test
  public void testFromRequest() throws Exception {
    when(request.getPath()).thenReturn("/some/path.txt");
    when(repository.getName()).thenReturn("SimpleVariableResolverAdapterTest");
    when(repository.getFormat()).thenReturn(new Format("test") { });
    SimpleVariableResolverAdapter simpleVariableResolverAdapter = new SimpleVariableResolverAdapter();
    VariableSource source = simpleVariableResolverAdapter.fromRequest(request, repository);

    assertThat(source.getVariableSet(), containsInAnyOrder("format", "path"));
    assertThat(source.get("format").get(), is("test"));
    assertThat(source.get("path").get(), is("/some/path.txt"));
  }
}
