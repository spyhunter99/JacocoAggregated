# Jacoco Aggregated Maven Site Report

A simple plugin to aggregate all IT and UT Jacoco test results at the parent maven project


## Usage

````xml
	<project>
	...
	<properties>
        ....
        <jacoco.it.execution.data.file>${project.build.directory}/coverage-reports/jacoco-it.exec</jacoco.it.execution.data.file>
        <jacoco.ut.execution.data.file>${project.build.directory}/coverage-reports/jacoco-ut.exec</jacoco.ut.execution.data.file>
		....
    </properties>
	
	<build>
		<plugins>
			....
			<plugin>
					<groupId>org.jacoco</groupId>
					<artifactId>jacoco-maven-plugin</artifactId>
					<version>0.7.9</version>
					<executions>
						<!--
							Prepares the property pointing to the JaCoCo runtime agent which
							is passed as VM argument when Maven the Surefire plugin is executed.
						-->
						<execution>
							<id>pre-unit-test</id>
							<goals>
								<goal>prepare-agent</goal>
							</goals>
							<configuration>
								<!-- Sets the path to the file which contains the execution data. -->
								<destFile>${jacoco.ut.execution.data.file}</destFile>
								<!--
									Sets the name of the property containing the settings
									for JaCoCo runtime agent.
								-->
								<propertyName>surefireArgLine</propertyName>
							</configuration>
						</execution>
						<!--
							Ensures that the code coverage report for unit tests is created after
							unit tests have been run.
						-->
						<execution>
							<id>post-unit-test</id>
							<phase>test</phase>
							<goals>
								<goal>report</goal>
							</goals>
							<configuration>
								<!-- Sets the path to the file which contains the execution data. -->
								<dataFile>${jacoco.ut.execution.data.file}</dataFile>
								<!-- Sets the output directory for the code coverage report. -->
								<outputDirectory>${project.reporting.outputDirectory}/jacoco-ut</outputDirectory>
							</configuration>
						</execution>
						<!--
							Prepares the property pointing to the JaCoCo runtime agent which
							is passed as VM argument when Maven the Failsafe plugin is executed.
						-->
						<execution>
							<id>pre-integration-test</id>
							<phase>pre-integration-test</phase>
							<goals>
								<goal>prepare-agent</goal>
							</goals>
							<configuration>
								<!-- Sets the path to the file which contains the execution data. -->
								<destFile>${jacoco.it.execution.data.file}</destFile>
								<!--
									Sets the name of the property containing the settings
									for JaCoCo runtime agent.
								-->
								<propertyName>failsafeArgLine</propertyName>
							</configuration>
						</execution>
						<!--
							Ensures that the code coverage report for integration tests after
							integration tests have been run.
						-->
						<execution>
							<id>post-integration-test</id>
							<phase>post-integration-test</phase>
							<goals>
								<goal>report</goal>
							</goals>
							<configuration>
								<!-- Sets the path to the file which contains the execution data. -->
								<dataFile>${jacoco.it.execution.data.file}</dataFile>
								<!-- Sets the output directory for the code coverage report. -->
								<outputDirectory>${project.reporting.outputDirectory}/jacoco-it</outputDirectory>
							</configuration>
						</execution>
					 
					</executions>
				</plugin>
				
				<!-- Used for unit tests -->
				<plugin>
					<groupId>org.apache.maven.plugins</groupId>
					<artifactId>maven-surefire-plugin</artifactId>
					<version>2.15</version>
					<configuration>
						<!-- Sets the VM argument line used when unit tests are run. -->
						<argLine>${surefireArgLine}</argLine>
						<!-- Skips unit tests if the value of skip.unit.tests property is true -->
						<skipTests>${skip.unit.tests}</skipTests>
						<!-- Excludes integration tests when unit tests are run. -->
						
					</configuration>
				</plugin>
				....
           </plugins>
		   
		   
		   ....
		   
		   <reporting>
				<plugins>
					
					<plugin>
						<groupId>com.github.spyhunter99</groupId>
						<artifactId>jacoco-report-plugin</artifactId>
						<version>1.0.0</version>
						<reportSets>
							<reportSet>
								<reports>
									<report>jacoco-aggregate</report>
								</reports>
							</reportSet>
						</reportSets>
					</plugin>
					
				 
				</plugins>
			</reporting>
		   
		   .....
	</build>

````