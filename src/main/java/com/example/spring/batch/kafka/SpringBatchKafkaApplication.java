package com.example.spring.batch.kafka;

import java.util.Collections;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.kafka.builder.KafkaItemReaderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.ConsumerFactory;

import com.example.spring.batch.kafka.domain.Player;

@EnableKafka
@EnableBatchProcessing
@SpringBootApplication
public class SpringBatchKafkaApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringBatchKafkaApplication.class, args);
	}

	@Autowired
	private ConsumerFactory<String, Player> consumerFactory;

	@Autowired
	private JobBuilderFactory jobs;

	@Autowired
	private StepBuilderFactory steps;

	@Bean
	public Job kafkaJob() {
		return jobs.get("kafkaJob").start(kafkaReaderStep()).build();
	}

	@Bean
	public Step kafkaReaderStep() {
		return steps.get("kafkaReaderStep")
				.<Player, Player> chunk(10)
				.reader(itemReader())
				.writer(fakeItemWriter())
				.build();
	}

	@Bean
	public ItemReader<Player> itemReader() {
		return new KafkaItemReaderBuilder<String, Player>()
				.topics(Collections.singletonList("players"))
				.consumerFactory(consumerFactory)
				.maxItemCount(50)
				.saveState(true)
				.name("playersItemReader")
				.build();
	}

	@Bean
	public ItemWriter<Player> fakeItemWriter() {
		return items -> items.forEach(System.out::println);
	}

}
