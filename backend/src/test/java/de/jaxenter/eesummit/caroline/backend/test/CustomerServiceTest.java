/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package de.jaxenter.eesummit.caroline.backend.test;

import de.jaxenter.eesummit.caroline.backend.api.CustomerService;
import de.jaxenter.eesummit.caroline.backend.api.UserService;
import de.jaxenter.eesummit.caroline.entities.CaroLineUser;
import de.jaxenter.eesummit.caroline.entities.Customer;
import org.apache.deltaspike.core.api.projectstage.ProjectStage;
import org.apache.deltaspike.jpa.api.transaction.Transactional;
import org.apache.deltaspike.testcontrol.api.TestControl;
import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;

/**
 * @author <a href="mailto:struberg@yahoo.de">Mark Struberg</a>
 */
@RunWith(CdiTestRunner.class)
@TestControl(projectStage = ProjectStage.UnitTest.class, // is the default anyway, just like to show..
             startScopes = {ApplicationScoped.class, RequestScoped.class}) // in case you need special scopes
public class CustomerServiceTest
{
    private @Inject CustomerService custSvc;
    private @Inject UserService usrSvc;

    private @Inject CleanUp cleanUp;

    @Before
    public void cleanUpDb() throws Exception {
        cleanUp.cleanUpDb();
    }


    @ApplicationScoped
    @Transactional
    public static class CleanUp
    {
        private @Inject EntityManager em;

        public void cleanUpDb() throws Exception
        {
            Query q = em.createQuery("DELETE from Customer AS c where c.lastName LIKE :name", Customer.class);
            q.setParameter("name", "UTEST_%");
            q.executeUpdate();
        }
    }

    /**
     * This unit test covers the whole lifecycle of a user creation.
     */
    @Test
    public void testCustomerCreation() throws Exception {

        System.out.println("\n\nHUHUUUU \n\n");
        // step 1: create the customer(s)
        {
            Customer cust1 = new Customer();
            cust1.setActive(true);
            cust1.setFirstName("Hans");
            cust1.setLastName("UTEST_Hinz");
            cust1.setEmail("hans.hinz@invalid.invalid");
            cust1.setLoginId("hans");
            cust1.setLoginHash(usrSvc.getPasswordHash("hans"));

            custSvc.createCustomer(cust1);

            Customer cust2 = new Customer();
            cust2.setActive(true);
            cust2.setFirstName("Josefine");
            cust2.setLastName("UTEST_Kunz");
            cust2.setEmail("josefine.kunz@invalid.invalid");
            cust2.setLoginId("josefine");
            cust2.setLoginHash(usrSvc.getPasswordHash("josefine"));

            custSvc.createCustomer(cust2);
        }

        // try to login with a wrong user
        {
            CaroLineUser usr = usrSvc.login("unknownUserId", "1111");
            Assert.assertNull(usr);
        }

        // try to login with a wrong password
        {
            CaroLineUser usr = usrSvc.login("hans", "nixda");
            Assert.assertNull(usr);
        }

        // step 2: login
        {
            CaroLineUser usr = usrSvc.login("hans", "hans");
            Assert.assertNotNull(usr);
            Assert.assertTrue(usr.isActive());
            Assert.assertTrue(usr instanceof Customer);
        }
    }
}
