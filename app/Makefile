setup:
	gradle wrapper --gradle-version 8.3

clean:
	./gradlew clean

build:
	./gradlew clean build

run-dist:
	./build/install/app/bin/app -h

install:
	./gradlew clean install

run-install-dist: install run-dist

run:
	./gradlew run

run-dist:
	./build/install/app/bin/app

stop:
	./gradlew stop

report:
	./gradlew jacocoTestReport

generate-migrations:
	./gradlew generateMigrations

.PHONY: build