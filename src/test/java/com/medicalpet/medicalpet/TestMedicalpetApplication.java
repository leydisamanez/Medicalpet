package com.medicalpet.medicalpet;

import org.springframework.boot.SpringApplication;

public class TestMedicalpetApplication {

	public static void main(String[] args) {
		SpringApplication.from(MedicalpetApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
