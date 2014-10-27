package com.mapgen;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Matrix {
    private Vector[] rows;

    Matrix(Vector... value) {
        this.rows = value;
    }

    int apply(int x, int y) {
        return rows[x - 1].apply(y);
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        String prefix = "";
        for (Vector row : rows) {
            result.append(prefix).append(row.toString());
            prefix = System.lineSeparator();
        }
        return result.toString();
    }
    
    static Matrix perms(Vector vector) {
        int[] indices = new int[vector.length()];
        for (int i = 0; i < vector.length(); i++)
            indices[i] = i;
        List<int[]> allPermuationIndices = new ArrayList<int[]>();
        permutation(new int[0], indices, allPermuationIndices);
        Vector[] perms = new Vector[allPermuationIndices.size()];
        for (int i = 0; i < perms.length; i++) {
            int[] permutationIndices = allPermuationIndices.get(i);
            int[] vectorValue = new int[permutationIndices.length];
            for (int j = 0; j < permutationIndices.length; j++)
                vectorValue[j] = vector.apply(permutationIndices[j] + 1);
            perms[i] = new Vector(vectorValue);
        }
        return new Matrix(perms);
    }

    private static void permutation(int[] prefix, int[] remaining, List<int[]> returnValue) {
        if (remaining.length == 0)
            returnValue.add(prefix);
        else {
            for (int i = 0; i < remaining.length; i++) {
                int elem = remaining[i];
                int[] newPrefix = Arrays.copyOf(prefix, prefix.length + 1);
                newPrefix[prefix.length] = elem;
                int[] newRemaining = new int[remaining.length - 1];
                System.arraycopy(remaining, 0, newRemaining, 0, i);
                System.arraycopy(remaining, i + 1, newRemaining, i + 1 - 1, remaining.length - (i + 1));
                permutation(newPrefix, newRemaining, returnValue);
            }
        }
    }
}