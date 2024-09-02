package Parser;

public class Tuple<A, B> {
    private A first;
    private B second;

    public Tuple(A first, B second) {
        this.first = first;
        this.second = second;
    }

    public A get_first() {
        return first;
    }

    public void set_first(A first) {
        this.first = first;
    }

    public B get_second() {
        return second;
    }

    public void set_second(B second) {
        this.second = second;
    }

    @Override
    public String toString() {
        return "(" + first + ", " + second + ")";
    }
}