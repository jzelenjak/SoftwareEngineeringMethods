package nl.tudelft.sem.heartbeat;

import com.google.gson.JsonObject;
import java.net.InetAddress;
import java.net.UnknownHostException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

/**
 * The HeartbeatTask class is a class that periodically emits a heartbeat to a remote gateway server
 * to ensure that the sender registration does not expire.
 *
 * <p><b>Important:</b> to enable the task, set the <i>heartbeat.microserviceName</i> in your
 * application.properties file. This will enable the heartbeat task. The host and port names default
 * to 'localhost' and '8080'. Override these by changing the <i>heartbeat.gatewayHost</i> and/or
 * <i>heartbeat.gatewayPort</i> properties.
 *
 * <p>Uses default scheduling time of 55 seconds. Override this by changing the
 * <i>heartbeat.intervalMillis</i> property.
 *
 * <p>To register this task, do the following;
 * <pre>{@code
 * @Import(HeartbeatConfig.class) // <-- Include the configuration (+ local component scan)
 * public class MyMicroserviceApplication {
 *     ...
 * }
 * }</pre>
 */
@Component
@ConditionalOnProperty("heartbeat.microserviceName")
public class HeartbeatTask {
    // Logger for reporting errors related to the remote gateway
    private static final Logger logger = LoggerFactory.getLogger(HeartbeatTask.class);

    // Create web client for executing asynchronous requests
    private final transient WebClient webClient;

    // Configuration for the heartbeat
    private final transient HeartbeatConfig heartbeatConfig;

    // Environment reference
    private final transient Environment environment;

    /**
     * Constructs the GatewayController class.
     */
    @Autowired
    public HeartbeatTask(HeartbeatConfig heartbeatConfig, Environment environment) {
        this.heartbeatConfig = heartbeatConfig;
        this.environment = environment;
        this.webClient = WebClient.create();
    }

    /**
     * Send a heartbeat to the discovery server every <i>heartbeat.interval.milliseconds</i> ms.
     * Can be configured.
     */
    @Scheduled(fixedRateString = "${heartbeat.intervalMillis}",
            initialDelayString = "${heartbeat.initialDelay:0}")
    public void sendHeartbeat() throws UnknownHostException {
        JsonObject clientInfo = new JsonObject();
        clientInfo.addProperty("host", InetAddress.getLocalHost().getHostName());
        clientInfo.addProperty("port", environment.getProperty("server.port"));

        // Send a scheduled request to the gateway registry server
        webClient.post().uri(UriComponentsBuilder.newInstance()
                        .scheme("http")
                        .host(heartbeatConfig.getGatewayHost())
                        .port(heartbeatConfig.getGatewayPort())
                        .pathSegment("discovery", "register", heartbeatConfig.getMicroserviceName())
                        .toUriString())
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(clientInfo.toString()))
                .exchange()
                .onErrorResume(clientResponse -> {
                    logger.error("Unable to register to discovery server at: {}:{}",
                            heartbeatConfig.getGatewayHost(), heartbeatConfig.getGatewayPort());
                    return Mono.empty();
                }).block();
    }
}
