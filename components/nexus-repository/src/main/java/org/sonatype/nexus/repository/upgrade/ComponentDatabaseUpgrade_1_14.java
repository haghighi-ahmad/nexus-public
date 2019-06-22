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
package org.sonatype.nexus.repository.upgrade;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.sonatype.nexus.common.upgrade.Upgrades;
import org.sonatype.nexus.orient.DatabaseInstance;
import org.sonatype.nexus.orient.DatabaseInstanceNames;
import org.sonatype.nexus.orient.DatabaseUpgradeSupport;
import org.sonatype.nexus.repository.config.internal.ConfigurationEntityAdapter;

import com.orientechnologies.orient.core.metadata.schema.OType;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Add routingRuleId link to Configuration
 *
 * @since 3.next
 */
@Named
@Singleton
@Upgrades(model = DatabaseInstanceNames.COMPONENT, from = "1.13", to = "1.14")
public class ComponentDatabaseUpgrade_1_14 // NOSONAR
    extends DatabaseUpgradeSupport
{
  private static final String P_ROUTING_RULE_ID = "routingRuleId";

  private Provider<DatabaseInstance> componentDatabaseInstance;

  @Inject
  public ComponentDatabaseUpgrade_1_14(@Named(DatabaseInstanceNames.COMPONENT) final Provider<DatabaseInstance> componentDatabaseInstance) {
    this.componentDatabaseInstance = checkNotNull(componentDatabaseInstance);
  }

  @Override
  public void apply() throws Exception {
    withDatabaseAndClass(componentDatabaseInstance, ConfigurationEntityAdapter.DB_NAME, (db, table) -> {
      if (!table.existsProperty(P_ROUTING_RULE_ID)) {
        table.createProperty(P_ROUTING_RULE_ID, OType.LINK);
      }
    });
  }
}
