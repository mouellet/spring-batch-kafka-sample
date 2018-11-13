package com.example.spring.batch.kafka;

import java.util.Collections;

import org.apache.kafka.common.TopicPartition;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.kafka.KafkaItemReader;
import org.springframework.batch.item.kafka.builder.KafkaItemReaderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.FileSystemResource;
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
	public Job kafkaWriterJob() {
		return jobs.get("kafkaWriterJob").start(kafkaWriterStep()).build();
	}

	@Bean
	public Step kafkaWriterStep() {
		return steps.get("kafkaWriterStep").<Player, Player> chunk(10).reader(itemReader()).writer(flatFileItemWriter())
				.build();
	}

	@Bean
	public KafkaItemReader<String, Player> itemReader() {
		return new KafkaItemReaderBuilder<String, Player>()
				.topicPartitions(Collections.singletonList(new TopicPartition("players", 0)))
				.consumerFactory(consumerFactory).pollTimeout(0L).build();
	}

	@Bean
	public FlatFileItemWriter<Player> flatFileItemWriter() {
		return new FlatFileItemWriterBuilder<Player>()
				.name("flatFileItemWriter")
				.resource(new FileSystemResource("processed_players.csv"))
				.delimited()
				.names(new String[] { "id", "lastName", "firstName", "position", "debutYear", "birthYear" })
				.build();
	}

	@Bean
	public ItemWriter<Player> fakeItemWriter() {
		return items -> items.forEach(System.out::println);
	}

}
