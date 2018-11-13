package com.example.spring.batch.kafka;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.SpELItemKeyMapper;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.kafka.builder.KafkaItemWriterBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.kafka.annotation.EnableKafka;

import com.example.spring.batch.kafka.domain.Player;
import com.example.spring.batch.kafka.domain.PlayerFieldSetMapper;
import org.springframework.kafka.core.KafkaTemplate;

@EnableKafka
@EnableBatchProcessing
@SpringBootApplication
public class SpringBatchKafkaApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringBatchKafkaApplication.class, args);
	}

	@Autowired
	private KafkaTemplate<String, Player>  kafkaTemplate;

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
		return steps.get("kafkaWriterStep")
				.<Player, Player> chunk(10)
				.reader(itemReader())
				.writer(itemWriter())
				.build();
	}

	@Bean
	public FlatFileItemReader<Player> itemReader() {
		return new FlatFileItemReaderBuilder<Player>()
				.saveState(false)
				.resource(new ClassPathResource("players.csv"))
				.delimited()
				.names(new String[] { "ID", "lastName", "firstName", "position", "debutYear", "birthYear" })
				.fieldSetMapper(new PlayerFieldSetMapper())
				.build();
	}

	 @Bean
	 public ItemWriter<Player> itemWriter() {
		return new KafkaItemWriterBuilder<String, Player>()
				.kafkaTemplate(kafkaTemplate)
				.itemKeyMapper(new SpELItemKeyMapper<>("id"))
				.build();
	 }

	@Bean
	public ItemWriter<Player> fakeItemWriter() {
		return items -> items.forEach(System.out::println);
	}

}
