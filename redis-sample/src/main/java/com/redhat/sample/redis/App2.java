package com.redhat.sample.redis;

import java.util.Map;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.ClassPathResource;

import com.redhat.sample.redis.Person.Gender;

// https://examples.javacodegeeks.com/enterprise-java/spring/spring-data-redis-example/

public class App2 {
	public static void main(String[] args) {

		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
				new ClassPathResource("/spring/spring-cache-context.xml").getPath());
		PersonRepo personRepo = (PersonRepo) context.getBean("personRepo");

		for (int i = 0; i < 1 ; i++) {
			
			String name = System.getenv("hello.name");
			System.out.println(">>>>>>>" + name);
			System.out.println(System.getenv());
			
			name = context.getEnvironment().getProperty("hello.name");
		
			System.out.println(">>>>>>>" + name);
			
			
			
			Person person = new Person();
			person.setId("1");
			person.setAge(55);
			person.setGender(Gender.Female);
			person.setName("RedHatter");
			personRepo.save(person);

			Person person2 = new Person();
			person2.setId("2");
			person2.setAge(60);
			person2.setGender(Gender.Male);
			person2.setName("ShadowMan");
			personRepo.save(person2);

			Person person3 = new Person();
			person3.setId("3");
			person3.setAge(25);
			person3.setGender(Gender.Male);
			person3.setName("TheOne");
			personRepo.save(person3);

			System.out.println("Finding the One: " + personRepo.find("3"));

			Map<Object, Object> personMatrixMap = personRepo.findAll();

			System.out.println("Currently in the Redis Matrix");
			System.out.println(personMatrixMap);

			System.out.println("Deleting the ShadowMan");
			personRepo.delete("2");

			personMatrixMap = personRepo.findAll();

			System.out.println("Remnants .. : ");
			System.out.println(personMatrixMap);

			context.refresh();
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		context.close();
		
	}
}
