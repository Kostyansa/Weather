package org.example.weather.configuration;

import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.GroupActor;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VKConfiguration {

    @Bean
    public VkApiClient vkApiClient(){
        return new VkApiClient(HttpTransportClient.getInstance());
    }

    @Bean
    public GroupActor groupActor(
            @Value("${vkontakte.group_id}") Integer group_id,
            @Value("${vkontakte.access_token}") String access_token){
        return new GroupActor(group_id, access_token);
    }
}
