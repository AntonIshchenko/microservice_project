package com.example.storageservice.config;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

import java.util.LinkedHashMap;
import java.util.UUID;

import static org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_OFFSET_RESET_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.CLIENT_ID_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.GROUP_ID_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.MAX_POLL_RECORDS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG;

@Configuration
@RequiredArgsConstructor
@EnableKafka
public class ConsumerMessageConfig {

   private final KafkaProperties kafkaProperties;

   @Bean
   public ConcurrentKafkaListenerContainerFactory<String, String> idEntityConsumerFactory() {
      ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<String, String>();
      LinkedHashMap<String, Object>  settings = new LinkedHashMap<String, Object>();
      settings.put(BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
      settings.put(KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
      settings.put(VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
      settings.put(GROUP_ID_CONFIG, "search.entity-id-consumer");
      settings.put(CLIENT_ID_CONFIG, UUID.randomUUID().toString());
      settings.put(AUTO_OFFSET_RESET_CONFIG, "latest");
      settings.put(MAX_POLL_RECORDS_CONFIG, "5");
      settings.put(org.apache.kafka.clients.consumer.ConsumerConfig.FETCH_MIN_BYTES_CONFIG, "100485760");
      settings.put(org.apache.kafka.clients.consumer.ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, "5000");
      factory.setConsumerFactory(new DefaultKafkaConsumerFactory<String, String>(settings));
      factory.setBatchListener(true);
      return factory;
   }

   @Bean
   public ConcurrentKafkaListenerContainerFactory<String, String> jsonEntityConsumerFactory() {
      ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<String, String>();
      LinkedHashMap<String, Object> settings = new LinkedHashMap<String, Object>();
      settings.put(BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
      settings.put(KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
      settings.put(VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
      settings.put(GROUP_ID_CONFIG, "search.entity-json-consumer");
      settings.put(CLIENT_ID_CONFIG, UUID.randomUUID().toString());
      settings.put(AUTO_OFFSET_RESET_CONFIG, "latest");
      factory.setConsumerFactory(new DefaultKafkaConsumerFactory<String, String>(settings));
      return factory;
   }

   @Bean
   public NewTopic entityJson() {
      return TopicBuilder.name("storage.entityJson")
            .partitions(2)
            .replicas(1)
            .compact()
            .build();
   }

}

