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
package org.sonatype.nexus.repository.browse.internal;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Named;

import org.sonatype.nexus.common.text.Strings2;
import org.sonatype.nexus.repository.browse.ComponentPathBrowseNodeGenerator;
import org.sonatype.nexus.repository.storage.Asset;
import org.sonatype.nexus.repository.storage.Component;

import com.google.inject.Singleton;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Component-led layout based on group, name, and version; places components one level above their assets.
 *
 * @since 3.6
 */
@Singleton
@Named
public class DefaultBrowseNodeGenerator
    extends ComponentPathBrowseNodeGenerator
{
  /**
   * @return componentPath/lastSegment(assetPath) if the component was not null, otherwise assetPath
   */
  @Override
  public List<String> computeAssetPath(final Asset asset, @Nullable final Component component) {
    checkNotNull(asset);

    if (component != null) {
      List<String> path = computeComponentPath(asset, component);

      // place asset just below component
      path.add(lastSegment(asset.name()));

      return path;
    }
    else {
      return super.computeAssetPath(asset, null);
    }
  }

  /**
   * @return [componentGroup]/componentName/[componentVersion]
   */
  @Override
  public List<String> computeComponentPath(final Asset asset, final Component component) {
    List<String> path = new ArrayList<>();

    if (!Strings2.isBlank(component.group())) {
      path.add(component.group());
    }

    path.add(component.name());

    if (!Strings2.isBlank(component.version())) {
      path.add(component.version());
    }

    return path;
  }
}
