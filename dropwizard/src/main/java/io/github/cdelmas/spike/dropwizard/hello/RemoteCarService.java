package io.github.cdelmas.spike.dropwizard.hello;

import io.github.cdelmas.spike.common.domain.Car;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.util.List;

import static java.util.Arrays.asList;

public class RemoteCarService implements CarService {

    @Override
    public List<Car> getAllCars() {
        ClientBuilder clientBuilder = ClientBuilder.newBuilder();
        Client client = clientBuilder.build();
        WebTarget target = client.target("https://localhost:8443/cars");
        Invocation invocation = target.request(MediaType.APPLICATION_JSON).build(HttpMethod.GET);
        Car[] cars = invocation.invoke(Car[].class);
        return asList(cars);
    }

}