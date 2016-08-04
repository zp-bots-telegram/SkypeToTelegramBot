package pro.zackpollard.telegrambot.skypetotelegrambot.skype.listeners;

import org.springframework.stereotype.Component;
import pro.zackpollard.telegrambot.skypetotelegrambot.utils.AbstractAutowiringFactoryBean;

/**
 * @author aivanov
 */
@Component
public class SkypeEventListenerFactoryBean extends AbstractAutowiringFactoryBean<SkypeEventsListener> {

    @Override
    protected SkypeEventsListener doCreateInstance() {
        setSingleton(false);
        return new SkypeEventsListener();
    }

    @Override
    public Class<?> getObjectType() {
        return SkypeEventsListener.class;
    }

    public SkypeEventsListener create(Long telegramID) throws Exception {
        SkypeEventsListener object = getObject();
        object.setTelegramID(telegramID);
        return object;
    }
}
