package io.github.bobfrostman.zephyr.entity;

import java.util.List;
import java.util.Objects;

public class ZephyrTestScript {

    private Long id;
    private String type; //plain or bdd
    private List<String> steps;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<String> getSteps() {
        return steps;
    }

    public void setSteps(List<String> steps) {
        this.steps = steps;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ZephyrTestScript that = (ZephyrTestScript) o;
        return Objects.equals(id, that.id) && Objects.equals(type, that.type) && Objects.equals(steps, that.steps);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, type, steps);
    }

    @Override
    public String toString() {
        return "ZephyrTestCaseTestScript{" +
                "id=" + id +
                ", type='" + type + '\'' +
                ", steps=" + steps +
                '}';
    }
}
