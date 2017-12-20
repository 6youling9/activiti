package common.util;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ApplicationObjectSupport;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class SpringUtils extends ApplicationObjectSupport{

    private static ApplicationContext applicationContext;

    private static ApplicationContext getContext() {

        if (applicationContext == null) {
        	applicationContext =  new ClassPathXmlApplicationContext(new String[]{"classpath*:/spring-mvc.xml","classpath*:/spring-context.xml"});
        }
        return applicationContext;

    }

    public static  <T> T getBean(Class<T> clazz, String beanName) {

        return getContext().getBean(beanName, clazz);

    }
   
}
