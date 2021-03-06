/*
 * Copyright (C) 2013 Universitat Pompeu Fabra
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.gwaspi.dao.jpa;

import java.sql.DriverManager;
import java.sql.SQLException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceException;
import org.gwaspi.global.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JPA utilities used internally by DAOs.
 * Uses abstracted DB access to store data,
 * see persistence.xml for DB settings.
 */
public class JPAUtil {

	private static final Logger LOG = LoggerFactory.getLogger(JPAUtil.class);

	private final EntityManagerFactory emf;

	public JPAUtil(final EntityManagerFactory emf) {

		this.emf = emf;
	}

	/**
	 * Delete and shutdown the (in-memory) database.
	 */
	public static void shutdown() {

		final boolean inMemoryDb = Config.getSingleton().getBoolean(
				Config.PROPERTY_STORAGE_IN_MEMORY,
				true);
		if (inMemoryDb) {
			try {
				// give the Derby DB backend a little time to finnish ongoing transactions and stuff
				Thread.sleep(1000);
			} catch (final InterruptedException ex) {
				LOG.debug("while giving time to the DB backend to cleanup: {}", ex.getMessage());
			}

			try {
				DriverManager.getConnection("jdbc:derby:memory:gwaspi;drop=true");
			} catch (final SQLException ex) {
				LOG.info("while shutting down in-memory Derby DB: {}", ex.getMessage());
			}
		}
	}


	public EntityManager open() {

		EntityManager em = emf.createEntityManager();
		return em;
	}

	public void begin(final EntityManager em) {
		em.getTransaction().begin();
	}

	public void commit(final EntityManager em) {
		em.getTransaction().commit();
	}

	public void rollback(final EntityManager em) {

		if (em == null) {
			LOG.error("Failed to create an entity manager");
		} else {
			try {
				if (em.isOpen() && em.getTransaction().isActive()) {
					em.getTransaction().rollback();
					close(em);
				} else {
					LOG.error("Failed to rollback a transaction: no active"
							+ " connection or transaction");
				}
			} catch (final PersistenceException ex) {
				LOG.error("Failed to rollback a transaction", ex);
			}
		}
	}

	public void close(final EntityManager em) {

		if (em == null) {
			LOG.error("Failed to create an entity manager");
		} else {
			try {
				if (em.isOpen()) {
					if (em.getTransaction().isActive()) {
						throw new IllegalStateException("Trying to close an EntityManager with an active transaction");
					}
					em.close();
				}
			} catch (final IllegalStateException ex) {
				LOG.error("Failed to close an entity manager", ex);
			}
		}
	}
}
