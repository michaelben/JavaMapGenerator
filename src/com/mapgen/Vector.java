package com.mapgen;

public class Vector {
    private int[] value;

    Vector(int... value) {
        this.value = value;
    }

    int apply(int i) {
        return value[i - 1];
    }

    int length() {
        return value.length;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        String prefix = "";
        for (int entry : value) {
            result.append(prefix).append(entry);
            prefix = " ";
        }
        return result.toString();
    }
}