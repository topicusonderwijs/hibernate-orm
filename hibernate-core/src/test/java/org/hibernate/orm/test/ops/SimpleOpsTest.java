/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.orm.test.ops;

import org.hibernate.Hibernate;

import org.hibernate.testing.orm.junit.DomainModel;
import org.hibernate.testing.orm.junit.SessionFactoryScope;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Gail Badner
 */
@DomainModel(
		xmlMappings = "org/hibernate/orm/test/ops/SimpleEntity.hbm.xml"
)
public class SimpleOpsTest extends AbstractOperationTestCase {

	public String[] getMappings() {
		return new String[] {};
	}

	@Test
	public void testBasicOperations(SessionFactoryScope scope) {
		clearCounts( scope );

		Long id = 1L;
		scope.inTransaction(
				session -> {
					SimpleEntity entity = new SimpleEntity();
					entity.setId( id );
					entity.setName( "name" );
					session.save( entity );
				}
		);

		assertInsertCount( 1, scope );
		assertUpdateCount( 0, scope );
		assertDeleteCount( 0, scope );

		clearCounts( scope );

		scope.inTransaction(
				session -> {
					SimpleEntity entity = session.get( SimpleEntity.class, id );
					assertThat( entity.getId(), is( 1L ) );
					assertThat( entity.getName(), is( "name" ) );
					entity.setName( "new name" );
				}
		);

		assertInsertCount( 0, scope );
		assertUpdateCount( 1, scope );
		assertDeleteCount( 0, scope );

		clearCounts( scope );

		SimpleEntity simpleEntity = scope.fromTransaction(
				session -> {
					SimpleEntity entity = session.load( SimpleEntity.class, id );
					assertFalse( Hibernate.isInitialized( entity ) );
					assertThat( entity.getId(), is( 1L ) );
					assertThat( entity.getName(), is( "new name" ) );
					assertTrue( Hibernate.isInitialized( entity ) );
					return entity;
				}
		);

		assertInsertCount( 0, scope );
		assertUpdateCount( 0, scope );
		assertDeleteCount( 0, scope );

		simpleEntity.setName( "another new name" );

		scope.inTransaction(
				session ->
						session.merge( simpleEntity )
		);


		assertInsertCount( 0, scope );
		assertUpdateCount( 1, scope );
		assertDeleteCount( 0, scope );

		clearCounts( scope );

		scope.inTransaction(
				session -> {
					SimpleEntity entity = session.get( SimpleEntity.class, id );
					assertThat( entity.getId(), is( 1L ) );
					assertThat( entity.getName(), is( "another new name" ) );
					session.delete( entity );
				}
		);

		assertInsertCount( 0, scope );
		assertUpdateCount( 0, scope );
		assertDeleteCount( 1, scope );
	}
}

