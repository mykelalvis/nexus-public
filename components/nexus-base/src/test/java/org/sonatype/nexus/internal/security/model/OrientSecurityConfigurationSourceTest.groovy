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
package org.sonatype.nexus.internal.security.model

import org.sonatype.nexus.orient.testsupport.DatabaseInstanceRule
import org.sonatype.nexus.security.config.CPrivilege
import org.sonatype.nexus.security.config.CRole
import org.sonatype.nexus.security.config.CUser
import org.sonatype.nexus.security.config.CUserRoleMapping
import org.sonatype.nexus.security.config.StaticSecurityConfigurationSource
import org.sonatype.nexus.security.privilege.NoSuchPrivilegeException
import org.sonatype.nexus.security.role.NoSuchRoleException
import org.sonatype.nexus.security.user.NoSuchRoleMappingException
import org.sonatype.nexus.security.user.UserNotFoundException

import org.junit.Rule
import spock.lang.Specification

/**
 * Tests for {@link OrientSecurityConfigurationSource}.
 */
class OrientSecurityConfigurationSourceTest
    extends Specification
{
  @Rule
  public DatabaseInstanceRule database = DatabaseInstanceRule.inMemory('security')

  OrientSecurityConfigurationSource source

  def setup() {
    source = new OrientSecurityConfigurationSource(
        database.instanceProvider,
        new StaticSecurityConfigurationSource(),
        new CUserEntityAdapter(),
        new CRoleEntityAdapter(),
        new CPrivilegeEntityAdapter(),
        new CUserRoleMappingEntityAdapter()
    )
    source.start()
    source.loadConfiguration()
  }

  def 'updateUser should persist user and prevent concurrent modification'() {
    given:
      def admin = source.configuration.getUser('admin')
      def newUser = new CUser(id: 'new')

    when: 'updateUser is called'
      admin.firstName = 'foo'
      source.configuration.updateUser(admin, [] as Set)
    
    then: 'user is persisted'
      source.configuration.getUser('admin').firstName == 'foo'

    when: 'updateUser is called again on old version'
      admin.firstName = 'bar'
      source.configuration.updateUser(admin, [] as Set)

    then: 'exception is thrown'
      thrown(ConcurrentModificationException)
      source.configuration.getUser('admin').firstName == 'foo'

    when: 'updateUser is called on user that doesnt hasnt been saved'
      source.configuration.updateUser(newUser, [] as Set)

    then: 'exception is thrown'
      thrown(UserNotFoundException)
  }

  def 'updatePrivilege should persist privilege and prevent concurrent modification'() {
    given:
      source.configuration.addPrivilege(new CPrivilege(id: 'test', name: 'test', type: 'test'))
      def privilege = source.configuration.getPrivilege('test')
      def newPrivilege = new CPrivilege(id: 'new', name: 'new', type: 'test')
     
    when: 'updatePrivilege is called'
      privilege.name = 'foo'
      source.configuration.updatePrivilege(privilege)

    then: 'privilege is persisted'
      source.configuration.getPrivilege('test').name == 'foo'
      
    when: 'updatePrivilege is called again on old version'
      privilege.name = 'bar'
      source.configuration.updatePrivilege(privilege)

    then: 'exception is thrown'
      thrown(ConcurrentModificationException)
      source.configuration.getPrivilege('test').name == 'foo'

    when: 'updatePrivilege is called on privilege that hasnt been saved'
      source.configuration.updatePrivilege(newPrivilege)

    then: 'exception is thrown'
      thrown(NoSuchPrivilegeException)
  }

  def 'updateRole should persist role and prevent concurrent modification'() {
    given:
      source.configuration.addRole(new CRole(id: 'test', name: 'test'))
      def role = source.configuration.getRole('test')
      def newRole = new CRole(id: 'new', name: 'new')

    when: 'updateRole is called'
      role.name = 'foo'
      source.configuration.updateRole(role)

    then: 'role is persisted'
      source.configuration.getRole('test').name == 'foo'

    when: 'updateRole is called again on old version'
      role.name = 'bar'
      source.configuration.updateRole(role)

    then: 'exception is thrown'
      thrown(ConcurrentModificationException)
      source.configuration.getRole('test').name == 'foo'

    when: 'updateRole is called on role that hasnt been saved'
      source.configuration.updateRole(newRole)

    then:
      thrown(NoSuchRoleException)
  }

  def 'updateUserRoleMapping should persist user role mapping and prevent concurrent modification'() {
    given:
      def adminMapping = source.configuration.getUserRoleMapping('admin', 'default')
      assert adminMapping.roles == ['nx-admin'] as Set
      def newUserRoleMapping = new CUserRoleMapping(userId: 'badid', source: 'badsource', roles: [] as Set)

    when: 'updateUserRoleMapping is called'
      adminMapping.roles = [] as Set
      source.configuration.updateUserRoleMapping(adminMapping)

    then: 'user role mapping is persisted'
      source.configuration.getUserRoleMapping('admin', 'default').roles == [] as Set

    when: 'updateUserRoleMapping is called again on old version'
      adminMapping.roles = ['nx-admin'] as Set
      source.configuration.updateUserRoleMapping(adminMapping)

    then: 'exception is thrown'
      thrown(ConcurrentModificationException)
      source.configuration.getUserRoleMapping('admin', 'default').roles == [] as Set

    when: 'updateUserRoleMapping is called on user role mapping that hasnt been saved'
      source.configuration.updateUserRoleMapping(newUserRoleMapping)

    then: 'exception is thrown'
      thrown(NoSuchRoleMappingException)
  }
}
