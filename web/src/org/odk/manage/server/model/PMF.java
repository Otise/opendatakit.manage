package org.odk.manage.server.model;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManagerFactory;

/**
 * @see <a href="http://code.google.com/appengine/docs/java/datastore/usingjdo.html#Getting_a_PersistenceManager_Instance">Persistence Manager Factories</a>.
 * @author alerer@google.com (Adam Lerer)
 *
 */
public final class PMF {
    private static final PersistenceManagerFactory pmfInstance =
        JDOHelper.getPersistenceManagerFactory("transactions-optional");

    private PMF() {}

    /**
     * This should only be called by DbAdapter.
     * @return The persistence manager factory.
     */
    protected static PersistenceManagerFactory get() {
        return pmfInstance;
    }
}
