/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.weld.tests.observers.transactional;

import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.manager.api.ExecutorServices;
import org.jboss.weld.tests.category.Integration;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import javax.transaction.*;
import java.util.concurrent.*;

/**
 * WELD-936 Tests that transaction observer notifications on EJB's work correctly when no request scope is active
 */
@Category(Integration.class)
@RunWith(Arquillian.class)
public class TransactionObserverOnEjbTest
{
   @Deployment
   public static Archive<?> deploy()
   {
      return ShrinkWrap.create(BeanArchive.class)
            .addPackage(TransactionObserverOnEjbTest.class.getPackage());
   }

   @Inject
   private UserTransaction userTransaction;

   @Inject
   private Ostrich ostrich;


   @Test
   public void testTransactionalObserver() throws  ExecutionException, TimeoutException, InterruptedException
   {
      final UserTransaction userTransaction = this.userTransaction;
      final Ostrich ostrich = this.ostrich;

      //We have to run this is a different thread
      //to make sure that the request scope is not active to test the issue properly
      ExecutorService executor = Executors.newSingleThreadExecutor();
      Future<?> future = executor.submit(new Runnable()
      {
         public void run()
         {
            try
            {
               userTransaction.begin();
               Assert.assertFalse(ostrich.isHeadInSand());
               ostrich.foxNearby();
               Assert.assertTrue(ostrich.isHeadInSand());
               userTransaction.commit();
               Assert.assertFalse(ostrich.isHeadInSand());
            } catch (Exception e)
            {
               throw new RuntimeException(e);
            }
         }
      });
      future.get(3, TimeUnit.SECONDS);

   }
}