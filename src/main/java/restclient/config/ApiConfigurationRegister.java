package restclient.config;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import restclient.model.ApiHostMap;

/**
 * Created by chanwook on 2014. 6. 30..
 */
public interface ApiConfigurationRegister {

    void registerApiConfiguration(ConfigurableListableBeanFactory beanFactory, ApiHostMap hostMap);

}
