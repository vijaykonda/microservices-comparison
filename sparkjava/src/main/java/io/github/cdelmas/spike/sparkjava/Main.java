/*
   Copyright 2015 Cyril Delmas

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package io.github.cdelmas.spike.sparkjava;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import io.github.cdelmas.spike.common.domain.Car;
import io.github.cdelmas.spike.common.domain.CarRepository;
import io.github.cdelmas.spike.common.hateoas.Link;
import io.github.cdelmas.spike.common.persistence.InMemoryCarRepository;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.List;

import static io.github.cdelmas.spike.common.hateoas.Link.self;
import static java.util.stream.Collectors.toList;
import static spark.Spark.get;
import static spark.Spark.post;
import static spark.SparkBase.port;

public class Main {
    public static void main(String[] args) {
        port(8099); // defaults on 4567
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        Unirest.setObjectMapper(new com.mashape.unirest.http.ObjectMapper() {
            @Override
            public <T> T readValue(String value, Class<T> valueType) {
                try {
                    return objectMapper.readValue(value, valueType);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }

            @Override
            public String writeValue(Object value) {
                try {
                    return objectMapper.writeValueAsString(value);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        get("/hello", (request, response) -> {
                    HttpResponse<Car[]> carHttpResponse = Unirest.get("http://localhost:8099/cars")
                            .header("Accept", "application/json")
                            .asObject(Car[].class);
                    Car[] cars = carHttpResponse.getBody();
                    List<String> carNames = Arrays.stream(cars)
                            .map(Car::getName)
                            .collect(toList());
                    return "We have these cars available: " + carNames;
                }
        );


        CarRepository carRepository = new InMemoryCarRepository();
        get("/cars", "application/json", (request, response) -> {
            List<Car> cars = carRepository.all();
            response.header("count", String.valueOf(cars.size()));
            response.type("application/json");
            return cars.stream().map(c -> {
                        CarRepresentation carRepresentation = new CarRepresentation(c);
                        carRepresentation.addLink(self(request.url() + "/" + c.getId()));
                        return carRepresentation;
                    }
            ).collect(toList());
        }, objectMapper::writeValueAsString);

        post("/cars", "application/json", (request, response) -> {
            Car car = null;
            try {
                car = objectMapper.readValue(request.body(), Car.class);
                carRepository.save(car);
                response.header("Location", request.url() + "/" + car.getId());
                response.status(201);
            } catch (IOException e) {
                response.status(400);
            }
            return "";
        });


    }
}