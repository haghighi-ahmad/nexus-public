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
package org.sonatype.nexus.repository.routing.internal;

import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.common.entity.EntityId;
import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.manager.RepositoryManager;
import org.sonatype.nexus.repository.routing.RoutingMode;
import org.sonatype.nexus.repository.routing.RoutingRule;
import org.sonatype.nexus.repository.routing.RoutingRuleHelper;
import org.sonatype.nexus.repository.routing.RoutingRulesConfiguration;
import org.sonatype.nexus.repository.security.RepositoryPermissionChecker;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static org.sonatype.nexus.security.BreadActions.READ;

/**
 * @since 3.16
 */
@Named
@Singleton
public class RoutingRuleHelperImpl
    implements RoutingRuleHelper
{
  private final RoutingRuleCache routingRuleCache;

  private final RepositoryManager repositoryManager;

  private final RoutingRulesConfiguration configuration;

  private final RepositoryPermissionChecker repositoryPermissionChecker;

  @Inject
  public RoutingRuleHelperImpl(
      final RoutingRuleCache routingRuleCache,
      final RepositoryManager repositoryManager,
      final RoutingRulesConfiguration configuration,
      final RepositoryPermissionChecker repositoryPermissionChecker)
  {
    this.routingRuleCache = checkNotNull(routingRuleCache);
    this.repositoryManager = checkNotNull(repositoryManager);
    this.configuration = checkNotNull(configuration);
    this.repositoryPermissionChecker = checkNotNull(repositoryPermissionChecker);
  }

  @Override
  public boolean isAllowed(final Repository repository, final String path) {
    if (!configuration.isEnabled()) {
      return true;
    }

    RoutingRule routingRule = routingRuleCache.getRoutingRule(repository);

    if (routingRule == null) {
      return true;
    }

    return isAllowed(routingRule, path);
  }

  public boolean isAllowed(final RoutingRule routingRule, final String path) {
    return isAllowed(routingRule.mode(), routingRule.matchers(), path);
  }

  @Override
  public boolean isAllowed(final RoutingMode mode, final List<String> matchers, final String path) {
    boolean matches = matchers.stream().anyMatch(path::matches);
    return (!matches && mode == RoutingMode.BLOCK) || (matches && mode == RoutingMode.ALLOW);
  }

  @Override
  public Map<EntityId, List<Repository>> calculateAssignedRepositories() {
    return StreamSupport.stream(repositoryManager.browse().spliterator(), false)
        .filter(repository -> routingRuleCache.getRoutingRuleId(repository) != null)
        .collect(groupingBy(routingRuleCache::getRoutingRuleId, toList()));
  }

  @Override
  public void ensureUserHasPermissionToRead() {
    repositoryPermissionChecker.ensureUserHasAdminAccessToAny(READ, repositoryManager.browse());
  }
}
