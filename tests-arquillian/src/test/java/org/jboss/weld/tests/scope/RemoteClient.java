package org.jboss.weld.tests.scope;

import java.io.IOException;
import java.lang.annotation.Annotation;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.weld.test.Utils;
import org.junit.Assert;

@WebServlet("/")
public class RemoteClient extends HttpServlet
{

   private static Annotation USELESS_LITERAL = new AnnotationLiteral<Useless>()
   {
   };
   private static Annotation SPECIAL_LITERAL = new AnnotationLiteral<Special>()
   {
   };

   @Inject
   BeanManager beanManager;

   @Override
   protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
   {
      try
      {
         Bean<Temp> specialTempBean = Utils.getBean(beanManager, Temp.class, SPECIAL_LITERAL);
         Bean<Temp> uselessTempBean = Utils.getBean(beanManager, Temp.class, USELESS_LITERAL);
         TempConsumer tempConsumer = Utils.getReference(beanManager, TempConsumer.class);
         if (req.getPathInfo().equals("/request1"))
         {

            Assert.assertEquals(RequestScoped.class, specialTempBean.getScope());
            Assert.assertEquals(RequestScoped.class, uselessTempBean.getScope());
            Assert.assertEquals(10, Utils.getReference(beanManager, specialTempBean).getNumber());
            Assert.assertEquals(11, Utils.getReference(beanManager, uselessTempBean).getNumber());

            tempConsumer.getSpecialTemp().setNumber(101);
            tempConsumer.getUselessTemp().setNumber(102);

            Assert.assertEquals(101, tempConsumer.getSpecialTemp().getNumber());
            Assert.assertEquals(102, tempConsumer.getUselessTemp().getNumber());
            Assert.assertEquals(101, Utils.getReference(beanManager, specialTempBean).getNumber());
            Assert.assertEquals(102, Utils.getReference(beanManager, uselessTempBean).getNumber());
            return;
         }
         else if (req.getPathInfo().equals("/request2"))
         {

            Assert.assertEquals(10, tempConsumer.getSpecialTemp().getNumber());
            Assert.assertEquals(102, tempConsumer.getUselessTemp().getNumber());
            Assert.assertEquals(10, Utils.getReference(beanManager, specialTempBean).getNumber());
            Assert.assertEquals(102, Utils.getReference(beanManager, uselessTempBean).getNumber());
            return;
         }
      }
      catch (AssertionError e)
      {
         resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
      }
      resp.sendError(HttpServletResponse.SC_NOT_FOUND);
   }

}
