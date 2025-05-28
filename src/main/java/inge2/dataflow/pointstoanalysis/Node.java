package inge2.dataflow.pointstoanalysis;

public class Node {
    public String name;
    public Node(String name) {
        this.name = name;
    }

    @Override
    public String toString(){
        return "(" + name + ")";
    }

    @Override
    public boolean equals(Object other){
        if (other == null) return false;
        if (other == this) return true;
        if (!(other instanceof Node))return false;
        Node otherMyClass = (Node)other;
        return this.name.equals(otherMyClass.name);
    }

    public int hashCode() {
        int result = 0;
        result = 31*result + (name !=null ? name.hashCode() : 0);
        return result;
    }
}
