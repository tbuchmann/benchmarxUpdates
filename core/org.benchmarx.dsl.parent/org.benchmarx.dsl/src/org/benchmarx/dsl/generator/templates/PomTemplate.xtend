package org.benchmarx.dsl.generator.templates

import org.benchmarx.dsl.benchmarxDSL.ProblemDecl

class PomTemplate {

	def pomContent(ProblemDecl p) '''
		<?xml version="1.0" encoding="UTF-8"?>
		<project xmlns="http://maven.apache.org/POM/4.0.0"
		         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
		         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
		                             http://maven.apache.org/xsd/maven-4.0.0.xsd">
		  <modelVersion>4.0.0</modelVersion>

		  <parent>
		    <groupId>org.benchmarx</groupId>
		    <artifactId>benchmarx-parent</artifactId>
		    <version>0.0.1-SNAPSHOT</version>
		    <relativePath>../../pom.xml</relativePath>
		  </parent>

		  <artifactId>benchmarx-«p.name.toLowerCase»</artifactId>
		  <name>Benchmarx «p.name» Test Module</name>

		  <build>
		    <sourceDirectory>src</sourceDirectory>
		    <testSourceDirectory>src</testSourceDirectory>
		    <plugins>
		      <plugin>
		        <artifactId>maven-surefire-plugin</artifactId>
		        <configuration>
		          <testClassesDirectory>${project.build.outputDirectory}</testClassesDirectory>
		        </configuration>
		      </plugin>
		    </plugins>
		  </build>

		  <dependencies>
		    <dependency>
		      <groupId>org.benchmarx</groupId>
		      <artifactId>benchmarx-core</artifactId>
		      <version>${project.version}</version>
		    </dependency>
		    <!-- TODO: add metamodel JAR dependencies for «p.name» -->
		    <!-- TODO: add BX tool JAR dependencies -->
		  </dependencies>
		</project>
	'''
}
