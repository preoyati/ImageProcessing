package thinning;

public class Coordinates {

    public Coordinates(int X, int Y) {
            this.X = X;
            this.Y = Y;
    }
    
    public int getX() {
            return this.X;
    }
    
    public int getY() {
            return this.Y;
    }

    public void setX(int X) {
            this.X = X;
    }

    public void setY(int Y) {
            this.Y = Y;
    }

    public boolean equals(Object obj) {
            if(obj instanceof Coordinates) {
                    Coordinates temp = (Coordinates)obj;
                    return ((this.X==temp.X) && (this.Y==temp.Y));
            }
            return false;
    }

    public int hashCode() {
            String str = new String(String.valueOf(X));
            str.concat(String.valueOf(Y));
            return str.hashCode();
                    
    }
    
    private int X, Y;
}

