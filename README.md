# 날씨 일기 프로젝트

---

## 프로젝트 소개

- 지정한 날짜에 일기를 작성하면, 작성한 일기의 해당되는 날의 날씨데이터를 가져와 기록하는 프로젝트입니다.
- Spring Boot 기반의 Back-End 프로젝트입니다.

## 프로젝트 초기 설정

- https://openweathermap.org/ 에서 회원가입 => 이메일 인증 => API KEY 발급
- src/main/resources/application.properties 에서 openweathermap.key에 API키 지정.
- MySQL 설치 => Server 및 접근 계정 생성
- src/main/resources/application.properties 에서 spring.datasource에 대한 정보 지정(MySQL 접근 계정 지정)

MySQL 초기 설정 SQL문
```roomsql
CREATE DATABASE `project`;

CREATE TABLE `date_weather` (
  `date` date NOT NULL,
  `weather` varchar(50) NOT NULL,
  `icon` varchar(50) NOT NULL,
  `temperature` double NOT NULL,
  PRIMARY KEY (`date`)
);

CREATE TABLE `diary` (
  `id` int NOT NULL AUTO_INCREMENT,
  `weather` varchar(50) NOT NULL,
  `icon` varchar(50) NOT NULL,
  `temperature` double NOT NULL,
  `text` varchar(500) NOT NULL,
  `date` date NOT NULL,
  PRIMARY KEY (`id`)
);

CREATE TABLE `memo` (
  `id` int NOT NULL AUTO_INCREMENT,
  `text` varchar(50) NOT NULL,
  PRIMARY KEY (`id`)
);

```

## API

- 프로젝트내 Swagger Documentation 참조.

## 기술 스택

- Spring Boot
- MySQL

## 사용된 라이브러리, 프레임워크

- Spring-Boot v.2.7.11
- JPA
- Swagger
- JSON-Simple
- Lombok