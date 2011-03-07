package eu.delving.core.util;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

/**
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class ConditionalBeanLoader implements BeanFactoryPostProcessor {

    private String className;

    public void setClassName(String className) {
        this.className = className;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        if (className != null && !className.isEmpty()) {
            try {
                beanFactory.registerSingleton(className, Class.forName(className).newInstance());
            }
            catch (Exception e) {
                throw new BeanCreationException("Unable to register " + className, e);
            }
        }
    }

}
