package pro.zackpollard.telegrambot.skypetotelegrambot.utils;

import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * @author aivanov
 */
public abstract class AbstractAutowiringFactoryBean<T> extends
        AbstractFactoryBean<T> implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(
            final ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    protected final T createInstance() throws Exception {
        final T instance = doCreateInstance();
        if (instance != null) {
            applicationContext
                    .getAutowireCapableBeanFactory()
                    .autowireBean(instance);
        }
        return instance;
    }

    /**
     * Create the bean instance.
     *
     * @see #createInstance()
     */
    protected abstract T doCreateInstance();

}