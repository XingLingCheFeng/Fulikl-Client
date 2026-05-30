package missu.epsilon.client.management;

import lombok.Getter;
import missu.epsilon.client.Client;
import missu.epsilon.client.utils.Wrapper;

import java.util.ArrayList;
import java.util.List;

@Getter
public class Manager<T> implements Wrapper {

    protected List<T> elements;

    public Manager(List<T> elements) {
        this.elements = elements;
    }

    public Manager() {
        this.elements = new ArrayList<>();
    }

}
