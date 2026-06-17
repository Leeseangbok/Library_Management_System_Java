package cls;

public class KeyValue {
    private final int key;
    private final String value;

    public KeyValue(int key, String value) {
        this.key = key;
        this.value = value;
    }

    public int getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}