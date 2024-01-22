package src.commons;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class CustomIterator implements Iterator<Integer> {
    private int currentValue;
    private final int start;
    private final int end;

    public CustomIterator() {
        this.currentValue = 0;
        this.start = 0;
        this.end = Integer.MAX_VALUE;
    }

    public CustomIterator(int end) {
        this.currentValue = 0; // Ajuste necesario
        this.start = 0;
        this.end = end;
    }

    public CustomIterator(int start, int end) {
        this.currentValue = start; // Ajuste necesario
        this.start = start;
        this.end = end;
    }

    @Override
    public boolean hasNext() {
        return currentValue < end;
    }

    @Override
    public Integer next() {
        if (!hasNext()) {
            throw new NoSuchElementException("No hay más elementos");
        }
        return currentValue++; // Cambio aquí
    }

    public int get() {
        return currentValue;
    }
}
