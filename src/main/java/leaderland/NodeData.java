package leaderland;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

public class NodeData {
    private String name;
    private String publicKeyPem;

    @JsonCreator
    public NodeData(@JsonProperty("name") String name, @JsonProperty("publicKey") String publicKeyPem) {
        this.name = name;
        this.publicKeyPem = publicKeyPem;
    }

    public String getName() {
        return name;
    }

    public String getPublicKeyPem() {
        return publicKeyPem;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("name", name)
                .add("publicKeyPem", publicKeyPem)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NodeData)) return false;
        NodeData nodeData = (NodeData) o;
        return Objects.equal(getName(), nodeData.getName()) &&
                Objects.equal(getPublicKeyPem(), nodeData.getPublicKeyPem());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getName(), getPublicKeyPem());
    }
}
