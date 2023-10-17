package Basics;


import java.util.Objects;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class Edge {
    public int vertex1;
    public int vertex2;

    public Edge(int vertex1, int vertex2) {
        this.vertex1 = min(vertex1, vertex2);
        this.vertex2 = max(vertex1, vertex2);
    }

    public boolean has(int v) {
        return v == vertex1 || v == vertex2;
    }

    public boolean isConnected(Edge e) {
        return vertex1 == e.vertex1 || vertex1 == e.vertex2 || vertex2 == e.vertex1 || vertex2 == e.vertex2;
    }

    public int other(int v) {
        if (has(v)) throw new IllegalArgumentException(v + " is not a vertex in this edge: " + this);
        if (v == vertex1) return vertex2;
        return vertex1;
    }

    @Override
    public int hashCode() {
        return Objects.hash(vertex1, vertex2);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Edge otherEdge = (Edge) obj;
        return (vertex1 == otherEdge.vertex1 && vertex2 == otherEdge.vertex2);
    }

    @Override
    public String toString() {
        return "(" + vertex1 + ", " + vertex2 + ")";
    }
}
