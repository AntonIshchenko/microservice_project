package org.example.config;

import java.util.LinkedHashMap;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

@Configuration
@RequiredArgsConstructor
@EnableKafka
public class ProducerMessageConfig {

   private final KafkaProperties kafkaProperties;

   @Bean
   public ProducerFactory<String, String> producerFactory() {
      LinkedHashMap<String, Object> settings = new LinkedHashMap<String, Object>();
      settings.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
      settings.put(ProducerConfig.ACKS_CONFIG, "all");
      settings.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, "1");
      settings.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "gzip");
      settings.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
      settings.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
      return new DefaultKafkaProducerFactory<>(settings);
   }

   @Bean
   public KafkaTemplate<String, String> kafkaTemplate(ProducerFactory<String, String> producerFactory) {
      return new KafkaTemplate<>(producerFactory);
   }

}
