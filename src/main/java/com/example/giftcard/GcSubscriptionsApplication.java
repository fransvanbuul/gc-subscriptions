package com.example.giftcard;

import com.thoughtworks.xstream.XStream;
import org.axonframework.config.EventProcessingConfiguration;
import org.axonframework.serialization.Serializer;
import org.axonframework.serialization.xml.XStreamSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class GcSubscriptionsApplication {

    public static void main(String[] args) {
        SpringApplication.run(GcSubscriptionsApplication.class, args);
    }

    @Autowired
    public void configure(EventProcessingConfiguration configuration) {
        configuration.usingTrackingProcessors();
    }

    @Autowired
    public void configure(Serializer serializer) {
        if(serializer instanceof XStreamSerializer) {
            XStream xStream = ((XStreamSerializer)serializer).getXStream();
            XStream.setupDefaultSecurity(xStream);
            xStream.allowTypesByWildcard(new String[] { "com.example.**", "org.axonframework.**" });
        }
    }

}
