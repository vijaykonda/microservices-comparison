package io.github.cdelmas.spike.restlet;

import io.github.cdelmas.spike.restlet.car.Car;
import io.github.cdelmas.spike.restlet.hello.Hello;
import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Restlet;
import org.restlet.Server;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Parameter;
import org.restlet.data.Protocol;
import org.restlet.security.ChallengeAuthenticator;
import org.restlet.security.Verifier;
import org.restlet.util.Series;

import javax.inject.Inject;

public class RestComponent extends Component {

    @Inject
    public RestComponent(@Hello Application helloApp, @Car Application carApp, Verifier authTokenVerifier) {
        getClients().add(Protocol.HTTPS);
        Server secureServer = getServers().add(Protocol.HTTPS, 8043);
        Series<Parameter> parameters = secureServer.getContext().getParameters();
        parameters.add("sslContextFactory", "org.restlet.engine.ssl.DefaultSslContextFactory");
        parameters.add("keyStorePath", System.getProperty("javax.net.ssl.keyStorePath"));
        getDefaultHost().attach("/api/hello", secure(helloApp, authTokenVerifier, "Hello"));
        getDefaultHost().attach("/api/cars", secure(carApp, authTokenVerifier, "Cars"));
    }

    private Restlet secure(Application app, Verifier verifier, String realm) {
        ChallengeAuthenticator guard = new ChallengeAuthenticator(getContext().createChildContext(),
                ChallengeScheme.CUSTOM, realm);
        guard.setVerifier(verifier);
        guard.setNext(app);
        return guard;
    }

}
