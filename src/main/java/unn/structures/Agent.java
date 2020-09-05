package unn.structures;

import java.util.Objects;
import java.util.UUID;

public class Agent {
    String uuid;
    String type;
    String protocol;
    String host;
    int port;
    int id;

    public Agent() {
        this.uuid = UUID.randomUUID().toString();
    }

    public String getType() {
        return type;
    }

    public Agent withType(String type) {
        this.type = type;
        return this;
    }

    public String getProtocol() {
        return protocol;
    }

    public Agent withProtocol(String protocol) {
        this.protocol = protocol;
        return this;
    }

    public String getHost() {
        return host;
    }

    public Agent withHost(String host) {
        this.host = host;
        return this;
    }

    public int getPort() {
        return port;
    }

    public Agent withPort(int port) {
        this.port = port;
        return this;
    }

    public int getId() {
        return id;
    }

    public Agent withId(int id) {
        this.id = id;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Agent agent = (Agent) o;
        return port == agent.port &&
                id == agent.id &&
                type.equals(agent.type) &&
                protocol.equals(agent.protocol) &&
                host.equals(agent.host);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, protocol, host, port, id);
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
